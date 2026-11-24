#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_WAS9_HOST

HOST="${VM_WAS9_HOST}"
HTTP_PORT="${VM_WAS9_HTTP_PORT:-9080}"
OL_VERSION="${OPENLIBERTY_VERSION:-23.0.0.12}"
DEFAULT_URL="https://repo1.maven.org/maven2/io/openliberty/openliberty-runtime/${OL_VERSION}/openliberty-runtime-${OL_VERSION}.zip"
DOWNLOAD_URL="${OPENLIBERTY_DOWNLOAD_URL:-${DEFAULT_URL}}"

LOCAL_ZIP=""
if [[ -n "${OPENLIBERTY_ZIP_PATH:-}" && -f "${OPENLIBERTY_ZIP_PATH}" ]]; then
  LOCAL_ZIP="${OPENLIBERTY_ZIP_PATH}"
else
  CACHE_DIR="${ROOT}/demo/vm/.cache"
  mkdir -p "${CACHE_DIR}"
  LOCAL_ZIP="${CACHE_DIR}/openliberty-runtime-${OL_VERSION}.zip"
  if [[ ! -f "${LOCAL_ZIP}" ]]; then
    echo "Downloading Open Liberty ${OL_VERSION}..."
    curl -fL --retry 3 -o "${LOCAL_ZIP}.partial" "${DOWNLOAD_URL}"
    mv "${LOCAL_ZIP}.partial" "${LOCAL_ZIP}"
  fi
fi

echo "Provisioning Open Liberty (WAS9 track) on ${HOST}"

ssh_cmd "${HOST}" "set -euo pipefail
sudo dnf -y install java-11-openjdk-headless unzip firewalld
sudo systemctl enable --now firewalld
id -u liberty >/dev/null 2>&1 || sudo useradd --system --create-home --home-dir /opt/liberty --shell /sbin/nologin liberty
sudo mkdir -p /opt/liberty/install /opt/liberty/instances/was9 /opt/liberty/artifacts
sudo chown -R liberty:liberty /opt/liberty
"

ZIP_BASENAME="$(basename "${LOCAL_ZIP}")"
echo "Uploading Open Liberty zip (${ZIP_BASENAME})..."
scp_to "${LOCAL_ZIP}" "${HOST}" "/tmp/${ZIP_BASENAME}"

ssh_cmd "${HOST}" "set -euo pipefail
sudo mv -f /tmp/${ZIP_BASENAME} /opt/liberty/artifacts/${ZIP_BASENAME}
sudo chown liberty:liberty /opt/liberty/artifacts/${ZIP_BASENAME}

sudo -u liberty bash -lc '
  set -euo pipefail
  cd /opt/liberty/install
  rm -rf wlp || true
  unzip -q /opt/liberty/artifacts/${ZIP_BASENAME}
  rm -rf /opt/liberty/instances/was9/*
  mkdir -p /opt/liberty/instances/was9/apps /opt/liberty/instances/was9/lib
  /opt/liberty/install/wlp/bin/server create was9 || true
  if [[ -d /opt/liberty/install/wlp/usr/servers/was9 ]]; then
    cp -a /opt/liberty/install/wlp/usr/servers/was9/. /opt/liberty/instances/was9/
  fi
  mkdir -p /opt/liberty/instances/was9/apps /opt/liberty/instances/was9/lib
  mkdir -p /opt/liberty/install/wlp/usr/servers
  rm -rf /opt/liberty/install/wlp/usr/servers/was9
  ln -sfn /opt/liberty/instances/was9 /opt/liberty/install/wlp/usr/servers/was9
'

sudo tee /opt/liberty/instances/was9/server.xml >/dev/null <<EOF
<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<server description=\"was9-fsi-demo\">
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

  <httpEndpoint id=\"defaultHttpEndpoint\" host=\"*\" httpPort=\"${HTTP_PORT}\" httpsPort=\"9443\"/>

  <applicationMonitor updateTrigger=\"mbean\"/>
</server>
EOF
sudo chown liberty:liberty /opt/liberty/instances/was9/server.xml

sudo tee /etc/systemd/system/liberty-was9.service >/dev/null <<'UNIT'
[Unit]
Description=Open Liberty WAS9 FSI demo
After=network.target

[Service]
Type=simple
User=liberty
Group=liberty
Environment=JAVA_HOME=/usr/lib/jvm/jre-11
Environment=WLP_USER_DIR=/opt/liberty/install/wlp/usr
ExecStart=/opt/liberty/install/wlp/bin/server run was9
Restart=always
RestartSec=3
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
UNIT

sudo systemctl daemon-reload
sudo systemctl enable --now liberty-was9.service

sudo firewall-cmd --permanent --add-port=${HTTP_PORT}/tcp
sudo firewall-cmd --reload

sudo systemctl --no-pager --full status liberty-was9.service | head -n 30 || true
"

cat <<EOF
Done.

Open Liberty (WAS9 track) should be up on:
  http://${HOST}:${HTTP_PORT}/

Next:
  ./demo/vm/rhel8/provision-postgres-was9.sh
  ./demo/vm/rhel8/deploy-liberty-accounts.sh <run_dir>
  ./demo/vm/rhel8/deploy-liberty-cards.sh <run_dir>
  ./demo/vm/rhel8/deploy-liberty-kyc.sh <run_dir>
EOF
