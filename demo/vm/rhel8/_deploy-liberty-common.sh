#!/usr/bin/env bash
# Shared helpers for Liberty WAS9 deploys. Sourced by deploy-liberty-*.sh

ensure_postgres_driver() {
  local host="$1"
  ssh_cmd "${host}" "set -euo pipefail
SERVER_DIR=/opt/liberty/instances/was9
if [[ ! -f \"\${SERVER_DIR}/lib/postgresql.jar\" ]]; then
  sudo -u liberty mkdir -p \"\${SERVER_DIR}/lib\"
  sudo -u liberty curl -fsSL -o \"\${SERVER_DIR}/lib/postgresql.jar\" \\
    https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar
fi
"
}

write_server_xml() {
  local host="$1"
  local http_port="$2"
  local accounts_db="$3" accounts_user="$4" accounts_password="$5"
  local cards_db="$6" cards_user="$7" cards_password="$8"
  local deploy_accounts="$9" deploy_cards="${10}" deploy_kyc="${11}"

  local apps_xml=""
  # Absolute paths avoid Liberty+symlink+SELinux "could not be found" races
  if [[ "${deploy_accounts}" == "true" ]]; then
    apps_xml+='  <webApplication id="fsi-accounts" location="/opt/liberty/instances/was9/apps/fsi-accounts.war" context-root="fsi-accounts"/>'$'\n'
  fi
  if [[ "${deploy_cards}" == "true" ]]; then
    apps_xml+='  <webApplication id="fsi-cards" location="/opt/liberty/instances/was9/apps/fsi-cards.war" context-root="fsi-cards"/>'$'\n'
  fi
  if [[ "${deploy_kyc}" == "true" ]]; then
    apps_xml+='  <webApplication id="fsi-kyc" location="/opt/liberty/instances/was9/apps/fsi-kyc.war" context-root="fsi-kyc"/>'$'\n'
  fi

  # Build server.xml locally then upload (avoids remote heredoc / Liberty ${} clashes)
  local tmp
  tmp="$(mktemp)"
  cat > "${tmp}" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<server description="was9-fsi-demo">
  <featureManager>
    <feature>servlet-3.1</feature>
    <feature>jaxrs-2.0</feature>
    <feature>jsonp-1.0</feature>
    <feature>ejbLite-3.2</feature>
    <feature>jpa-2.1</feature>
    <feature>jdbc-4.2</feature>
    <feature>jndi-1.0</feature>
    <feature>jms-2.0</feature>
    <feature>mdb-3.2</feature>
    <feature>wasJmsServer-1.0</feature>
    <feature>wasJmsClient-2.0</feature>
    <feature>jaxws-2.2</feature>
    <feature>localConnector-1.0</feature>
  </featureManager>

  <httpEndpoint id="defaultHttpEndpoint" host="*" httpPort="${http_port}" httpsPort="9443"/>

  <library id="postgresLib">
    <fileset dir="\${server.config.dir}/lib" includes="postgresql.jar"/>
  </library>

  <dataSource id="AccountsDS" jndiName="jdbc/AccountsDS" type="javax.sql.XADataSource">
    <jdbcDriver libraryRef="postgresLib" javax.sql.XADataSource="org.postgresql.xa.PGXADataSource"/>
    <properties.postgresql serverName="127.0.0.1" portNumber="5432"
        databaseName="${accounts_db}" user="${accounts_user}" password="${accounts_password}"/>
  </dataSource>

  <dataSource id="CardsDS" jndiName="jdbc/CardsDS" type="javax.sql.XADataSource">
    <jdbcDriver libraryRef="postgresLib" javax.sql.XADataSource="org.postgresql.xa.PGXADataSource"/>
    <properties.postgresql serverName="127.0.0.1" portNumber="5432"
        databaseName="${cards_db}" user="${cards_user}" password="${cards_password}"/>
  </dataSource>

  <messagingEngine id="defaultME">
    <queue id="FraudQueue"/>
  </messagingEngine>

  <jmsQueueConnectionFactory jndiName="jms/FraudConnectionFactory">
    <properties.wasJms/>
  </jmsQueueConnectionFactory>

  <jmsQueue id="FraudQueue" jndiName="jms/FraudQueue">
    <properties.wasJms queueName="FraudQueue"/>
  </jmsQueue>

  <!-- Bind MDB in fsi-cards.war to the Fraud queue -->
  <jmsActivationSpec id="fsi-cards/FraudProcessorMDB">
    <properties.wasJms destinationRef="FraudQueue"/>
  </jmsActivationSpec>

  <applicationMonitor updateTrigger="mbean" dropinsEnabled="false"/>

${apps_xml}</server>
EOF

  scp_to "${tmp}" "${host}" "/tmp/was9-server.xml"
  rm -f "${tmp}"

  ssh_cmd "${host}" "set -euo pipefail
sudo mv -f /tmp/was9-server.xml /opt/liberty/instances/was9/server.xml
sudo chown liberty:liberty /opt/liberty/instances/was9/server.xml
sudo systemctl restart liberty-was9.service
for i in \$(seq 1 60); do
  curl -sf -o /dev/null \"http://127.0.0.1:${http_port}/\" && break || true
  sleep 2
done
"
}

detect_deployed_flags() {
  local host="$1"
  DEPLOY_ACCOUNTS=false
  DEPLOY_CARDS=false
  DEPLOY_KYC=false
  # apps/ is liberty-owned; probe with sudo
  if ssh_cmd "${host}" "sudo test -f /opt/liberty/instances/was9/apps/fsi-accounts.war"; then
    DEPLOY_ACCOUNTS=true
  fi
  if ssh_cmd "${host}" "sudo test -f /opt/liberty/instances/was9/apps/fsi-cards.war"; then
    DEPLOY_CARDS=true
  fi
  if ssh_cmd "${host}" "sudo test -f /opt/liberty/instances/was9/apps/fsi-kyc.war"; then
    DEPLOY_KYC=true
  fi
}
