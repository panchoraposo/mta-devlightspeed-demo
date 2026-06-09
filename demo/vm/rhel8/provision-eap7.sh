#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_EAP7_HOST
require_env EAP7_ZIP_PATH

HOST="${VM_EAP7_HOST}"

if [[ ! -f "${EAP7_ZIP_PATH}" ]]; then
  echo "EAP7_ZIP_PATH not found: ${EAP7_ZIP_PATH}" >&2
  exit 1
fi

echo "Provisioning EAP 7 on ${HOST} (RHEL 8)"

ssh_cmd "${HOST}" "set -euo pipefail
sudo dnf -y install java-11-openjdk-headless unzip firewalld
sudo systemctl enable --now firewalld
id -u eap >/dev/null 2>&1 || sudo useradd --system --create-home --home-dir /opt/eap --shell /sbin/nologin eap
sudo mkdir -p /opt/eap/install /opt/eap/instances/eap7 /opt/eap/artifacts
sudo chown -R eap:eap /opt/eap
"

echo "Uploading EAP zip..."
ZIP_BASENAME="$(basename "${EAP7_ZIP_PATH}")"
scp_to "${EAP7_ZIP_PATH}" "${HOST}" "/tmp/${ZIP_BASENAME}"

ssh_cmd "${HOST}" "set -euo pipefail
sudo mv -f /tmp/${ZIP_BASENAME} /opt/eap/artifacts/${ZIP_BASENAME}
sudo chown eap:eap /opt/eap/artifacts/${ZIP_BASENAME}
"

ssh_cmd "${HOST}" "set -euo pipefail
sudo -u eap bash -lc '
  cd /opt/eap/install
  rm -rf jboss-eap-7* || true
  unzip -q /opt/eap/artifacts/${ZIP_BASENAME}
  EAP_DIR=\$(ls -d jboss-eap-7* | head -n 1)
  rm -rf /opt/eap/instances/eap7/*
  cp -a \"\${EAP_DIR}\"/* /opt/eap/instances/eap7/
  mkdir -p /opt/eap/instances/eap7/standalone/deployments
'

sudo tee /etc/systemd/system/eap7-standalone.service >/dev/null <<'UNIT'
[Unit]
Description=JBoss EAP 7 - standalone
After=network.target

[Service]
Type=simple
User=eap
Group=eap
WorkingDirectory=/opt/eap/instances/eap7
Environment=JAVA_HOME=/usr/lib/jvm/jre-11
# Use standalone-full to enable messaging (JMS/MDB) like real legacy EAP stacks
ExecStart=/opt/eap/instances/eap7/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0 -bmanagement 0.0.0.0
Restart=always
RestartSec=3
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
UNIT

sudo systemctl daemon-reload
sudo systemctl enable --now eap7-standalone.service

sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=9990/tcp
sudo firewall-cmd --reload

sudo systemctl --no-pager --full status eap7-standalone.service | head -n 30 || true
"

cat <<EOF
Done.

EAP 7 should be up on:
  http://${HOST}:8080/
Management:
  http://${HOST}:9990/

Next: deploy a WAR with:
  ./demo/vm/rhel8/deploy-eap7-payments.sh <run_dir>
EOF

