#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_INVENTORY_HOST

RUN_DIR="${1:-}"
if [[ -z "${RUN_DIR}" ]]; then
  echo "Usage: $0 <run_dir>" >&2
  echo "Example: $0 demo/runs/20260420T185828" >&2
  exit 1
fi

APP_DIR="${ROOT}/${RUN_DIR}/apps/fsi-inventory-springboot2-legacy"
if [[ ! -f "${APP_DIR}/pom.xml" ]]; then
  echo "Run dir does not look valid: ${APP_DIR}" >&2
  exit 1
fi

echo "Building inventory JAR locally from: ${APP_DIR}"
mvn -q -f "${APP_DIR}/pom.xml" test

JAR="$(ls "${APP_DIR}"/target/*.jar | head -n 1)"
if [[ ! -f "${JAR}" ]]; then
  echo "JAR not found in target/: ${APP_DIR}/target" >&2
  exit 1
fi

HOST="${VM_INVENTORY_HOST}"

echo "Uploading JAR to VM: ${HOST}"
scp_to "${JAR}" "${HOST}" "/tmp/app.jar"

ssh_cmd "${HOST}" "set -euo pipefail
sudo mv -f /tmp/app.jar /opt/inventory/app/app.jar
sudo chown inventory:inventory /opt/inventory/app/app.jar
sudo systemctl restart fsi-inventory-legacy.service
sudo systemctl --no-pager --full status fsi-inventory-legacy.service | head -n 30 || true
"

cat <<EOF
Done.

Inventory (VM):
  GET http://${HOST}:8082/api/v1/inventory/products
EOF

