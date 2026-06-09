#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_EAP7_HOST

HOST="${VM_EAP7_HOST}"

echo "Switching EAP7 systemd service to standalone-full.xml on ${HOST}"

ssh_cmd "${HOST}" "set -euo pipefail
sudo sed -i.bak 's|ExecStart=.*/standalone.sh .*|ExecStart=/opt/eap/instances/eap7/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0 -bmanagement 0.0.0.0|' /etc/systemd/system/eap7-standalone.service
sudo systemctl daemon-reload
sudo systemctl restart eap7-standalone.service
sudo systemctl --no-pager --full status eap7-standalone.service | head -n 30 || true
"

echo "Done."

