#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_INVENTORY_HOST

HOST="${VM_INVENTORY_HOST}"

echo "Provisioning Spring Boot 2 VM on ${HOST} (RHEL 8)"

ssh_cmd "${HOST}" "set -euo pipefail
sudo dnf -y install java-11-openjdk-headless firewalld
sudo systemctl enable --now firewalld

id -u inventory >/dev/null 2>&1 || sudo useradd --system --create-home --home-dir /opt/inventory --shell /sbin/nologin inventory
sudo mkdir -p /opt/inventory/app
sudo chown -R inventory:inventory /opt/inventory

sudo tee /etc/systemd/system/fsi-inventory-legacy.service >/dev/null <<'UNIT'
[Unit]
Description=FSI Inventory Spring Boot 2 (legacy)
After=network.target

[Service]
Type=simple
User=inventory
Group=inventory
WorkingDirectory=/opt/inventory/app
ExecStart=/usr/bin/java -jar /opt/inventory/app/app.jar
Restart=always
RestartSec=2
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
UNIT

sudo systemctl daemon-reload
sudo systemctl enable fsi-inventory-legacy.service

sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload
"

cat <<EOF
Done.

Next: deploy the JAR with:
  ./demo/vm/rhel8/deploy-sb2-inventory.sh <run_dir>
EOF

