#!/usr/bin/env bash
set -euo pipefail

RHEL8_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${RHEL8_DIR}/../_lib.sh"   # note: _lib.sh overwrites SCRIPT_DIR → demo/vm
# shellcheck disable=SC1091
source "${RHEL8_DIR}/_deploy-liberty-common.sh"

require_env VM_WAS9_HOST

RUN_DIR="${1:-}"
if [[ -z "${RUN_DIR}" ]]; then
  echo "Usage: $0 <run_dir>" >&2
  exit 1
fi

APP_DIR="${ROOT}/${RUN_DIR}/apps/fsi-cards-was9-legacy"
if [[ ! -f "${APP_DIR}/pom.xml" ]]; then
  APP_DIR="${RUN_DIR}/apps/fsi-cards-was9-legacy"
fi
if [[ ! -f "${APP_DIR}/pom.xml" ]]; then
  echo "Run dir does not look valid (missing fsi-cards-was9-legacy): ${RUN_DIR}" >&2
  exit 1
fi

HOST="${VM_WAS9_HOST}"
HTTP_PORT="${VM_WAS9_HTTP_PORT:-9080}"
ACCOUNTS_DB="${PG_ACCOUNTS_DB:-accounts}"
ACCOUNTS_USER="${PG_ACCOUNTS_USER:-accounts}"
ACCOUNTS_PASSWORD="${PG_ACCOUNTS_PASSWORD:-accounts}"
CARDS_DB="${PG_CARDS_DB:-cards}"
CARDS_USER="${PG_CARDS_USER:-cards}"
CARDS_PASSWORD="${PG_CARDS_PASSWORD:-cards}"

echo "Building cards WAR from: ${APP_DIR}"
mvn -q -f "${APP_DIR}/pom.xml" package
WAR="${APP_DIR}/target/fsi-cards.war"
[[ -f "${WAR}" ]] || { echo "WAR not found: ${WAR}" >&2; exit 1; }

echo "Uploading WAR to Liberty VM: ${HOST}"
scp_to "${WAR}" "${HOST}" "/tmp/fsi-cards.war"
ssh_cmd "${HOST}" "set -euo pipefail
sudo mkdir -p /opt/liberty/instances/was9/apps
sudo mv -f /tmp/fsi-cards.war /opt/liberty/instances/was9/apps/fsi-cards.war
sudo chown liberty:liberty /opt/liberty/instances/was9/apps/fsi-cards.war
sudo restorecon -v /opt/liberty/instances/was9/apps/fsi-cards.war || sudo chcon -t lib_t /opt/liberty/instances/was9/apps/fsi-cards.war || true
"

ensure_postgres_driver "${HOST}"
detect_deployed_flags "${HOST}"
DEPLOY_CARDS=true
write_server_xml "${HOST}" "${HTTP_PORT}" \
  "${ACCOUNTS_DB}" "${ACCOUNTS_USER}" "${ACCOUNTS_PASSWORD}" \
  "${CARDS_DB}" "${CARDS_USER}" "${CARDS_PASSWORD}" \
  "${DEPLOY_ACCOUNTS}" "${DEPLOY_CARDS}" "${DEPLOY_KYC}"

cat <<EOF
Done.

Cards / Fraud (Liberty WAS9 VM):
  UI:   http://${HOST}:${HTTP_PORT}/fsi-cards/
  POST  http://${HOST}:${HTTP_PORT}/fsi-cards/api/auths
  GET   http://${HOST}:${HTTP_PORT}/fsi-cards/api/auths/<authId>
EOF
