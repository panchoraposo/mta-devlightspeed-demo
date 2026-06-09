#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_EAP7_HOST

RUN_DIR="${1:-}"
if [[ -z "${RUN_DIR}" ]]; then
  echo "Usage: $0 <run_dir>" >&2
  echo "Example: $0 demo/runs/20260420T185828" >&2
  exit 1
fi

APP_DIR="${ROOT}/${RUN_DIR}/apps/fsi-payments-eap7-legacy"
if [[ ! -f "${APP_DIR}/pom.xml" ]]; then
  echo "Run dir does not look valid: ${APP_DIR}" >&2
  exit 1
fi

echo "Building payments WAR locally from: ${APP_DIR}"
mvn -q -f "${APP_DIR}/pom.xml" package

WAR="${APP_DIR}/target/fsi-payments.war"
if [[ ! -f "${WAR}" ]]; then
  echo "WAR not found: ${WAR}" >&2
  exit 1
fi

HOST="${VM_EAP7_HOST}"
PG_DB="${PG_DB:-payments}"
PG_USER="${PG_USER:-payments}"
PG_PASSWORD="${PG_PASSWORD:-payments}"

echo "WAR ready: $(ls -lh "${WAR}" | awk '{print $5 " " $9}')"
echo "Uploading WAR to EAP7 VM: ${HOST}"
scp_to "${WAR}" "${HOST}" "/tmp/fsi-payments.war"

echo "Configuring JMS queue + deploying..."
ssh_cmd "${HOST}" "set -euo pipefail
JBOSS_HOME=/opt/eap/instances/eap7

# Install PostgreSQL JDBC driver as an EAP module (idempotent)
if [[ ! -f \"\${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/postgresql.jar\" ]]; then
  sudo mkdir -p \"\${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main\"
  sudo curl -fsSL -o \"\${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/postgresql.jar\" \"https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar\"
  sudo tee \"\${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/module.xml\" >/dev/null <<'XML'
<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<module xmlns=\"urn:jboss:module:1.9\" name=\"org.postgresql\">
  <resources>
    <resource-root path=\"postgresql.jar\"/>
  </resources>
  <dependencies>
    <module name=\"javax.api\"/>
    <module name=\"javax.transaction.api\"/>
  </dependencies>
</module>
XML
  sudo chown -R eap:eap \"\${JBOSS_HOME}/modules/system/layers/base/org/postgresql\"
fi

sudo -u eap bash -lc '
  JBOSS_HOME=/opt/eap/instances/eap7
  CLI=\${JBOSS_HOME}/bin/jboss-cli.sh

  # wait for server
  for i in \$(seq 1 60); do
    \${CLI} -c \":read-attribute(name=server-state)\" >/dev/null 2>&1 && break
    sleep 1
  done

  # If the server is in reload-required state (previous config), reload first
  PROC_STATE=\$(\${CLI} -c \":read-attribute(name=process-state)\" 2>/dev/null | tr -d \"\\r\" || true)
  if echo \"\${PROC_STATE}\" | grep -q \"reload-required\"; then
    \${CLI} -c \":reload\" || true
    for i in \$(seq 1 90); do
      \${CLI} -c \":read-attribute(name=server-state)\" >/dev/null 2>&1 && break
      sleep 1
    done
  fi

  # Ensure previous deployment is removed before touching JMS/DS resources (idempotent)
  \${CLI} -c \"/deployment=fsi-payments.war:undeploy\" >/dev/null 2>&1 || true
  \${CLI} -c \"/deployment=fsi-payments.war:remove\" >/dev/null 2>&1 || true

  # Clean any previous deployment scanner markers/content
  rm -f \${JBOSS_HOME}/standalone/deployments/fsi-payments.war*

  # Ensure EJB3 has a default resource adapter for MDBs (required by @MessageDriven)
  # Without this, deployments with MDBs can fail with:
  #   ServiceNotFoundException: service jboss.ejb.default-resource-adapter-name-service not found
  \${CLI} -c \"/subsystem=ejb3:write-attribute(name=default-resource-adapter-name,value=activemq-ra.rar)\" || true

  # Remove previous resources if they exist (idempotent deploy)
  \${CLI} -c \"/subsystem=datasources/data-source=PaymentsDS:remove\" || true
  \${CLI} -c \"/subsystem=messaging-activemq/server=default/jms-queue=Payments:remove\" || true

  # Create datasource referenced by persistence.xml (ignore if already exists)
  # PostgreSQL on same VM (127.0.0.1). Requires driver module + provision-postgres.sh.
  \${CLI} -c \"/subsystem=datasources/jdbc-driver=postgresql:remove\" || true
  \${CLI} -c \"/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)\" || true
  \${CLI} -c \"/subsystem=datasources/data-source=PaymentsDS:add(jndi-name=java:jboss/datasources/PaymentsDS,enabled=true,use-java-context=true,connection-url=jdbc:postgresql://127.0.0.1:5432/${PG_DB},driver-name=postgresql,user-name=${PG_USER},password=${PG_PASSWORD})\" || true

  # Create queue used by the app (ignore if already exists)
  \${CLI} -c \"/subsystem=messaging-activemq/server=default/jms-queue=Payments:add(entries=[\\\"java:/jms/queue/Payments\\\",\\\"queue/Payments\\\"])\" || true

  # Apply any config changes that require reload before deploying
  PROC_STATE=\$(\${CLI} -c \":read-attribute(name=process-state)\" 2>/dev/null | tr -d \"\\r\" || true)
  if echo \"\${PROC_STATE}\" | grep -q \"reload-required\"; then
    \${CLI} -c \":reload\" || true
    for i in \$(seq 1 90); do
      \${CLI} -c \":read-attribute(name=server-state)\" >/dev/null 2>&1 && break
      sleep 1
    done
  fi

  # Deploy
  cp -f /tmp/fsi-payments.war \${JBOSS_HOME}/standalone/deployments/fsi-payments.war
  touch \${JBOSS_HOME}/standalone/deployments/fsi-payments.war.dodeploy

  # Wait for deployment marker
  for i in \$(seq 1 60); do
    test -f \${JBOSS_HOME}/standalone/deployments/fsi-payments.war.deployed && break
    test -f \${JBOSS_HOME}/standalone/deployments/fsi-payments.war.failed && break
    sleep 1
  done

  if test -f \${JBOSS_HOME}/standalone/deployments/fsi-payments.war.failed; then
    echo \"Deployment failed: \${JBOSS_HOME}/standalone/deployments/fsi-payments.war.failed\" >&2
    echo \"---- server.log (filtered) ----\" >&2
    tail -n 320 \${JBOSS_HOME}/standalone/log/server.log | egrep -i \"fsi-payments|Caused by|ERROR|FAILED|MessageListener|PaymentsDS|jms\\.queue\\.Payments|cfg\\.xml|postgres|SQL Error\" || true >&2
    exit 1
  fi
'
"

cat <<EOF
Done.

Payments (EAP7 VM):
  POST http://${HOST}:8080/fsi-payments/api/payments
  GET  http://${HOST}:8080/fsi-payments/api/payments/<paymentId>
EOF

