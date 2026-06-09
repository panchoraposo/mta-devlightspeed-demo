#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/_lib.sh"

require_env VM_TRADING_HOST

RUN_DIR="${1:-}"
if [[ -z "${RUN_DIR}" ]]; then
  echo "Usage: $0 <run_dir>" >&2
  echo "Example: $0 demo/runs/20260420T185828" >&2
  exit 1
fi

APP_DIR="${ROOT}/${RUN_DIR}/apps/fsi-trading-eap6-legacy"
if [[ ! -f "${APP_DIR}/pom.xml" ]]; then
  echo "Run dir does not look valid: ${APP_DIR}" >&2
  exit 1
fi

echo "Building WAR locally from: ${APP_DIR}"
mvn -q -f "${APP_DIR}/pom.xml" package

WAR="${APP_DIR}/target/fsi-trading.war"
if [[ ! -f "${WAR}" ]]; then
  echo "WAR not found: ${WAR}" >&2
  exit 1
fi

HOST="${VM_TRADING_HOST}"

echo "Ensuring WildFly container is running on ${HOST}"
ssh_cmd "${HOST}" "command -v podman >/dev/null 2>&1 || command -v docker >/dev/null 2>&1"

ssh_cmd "${HOST}" "mkdir -p ~/legacy/trading/deployments"
scp_to "${WAR}" "${HOST}" "~/legacy/trading/deployments/fsi-trading.war"

ssh_cmd "${HOST}" "set -euo pipefail
RUNTIME=\$(command -v podman || command -v docker)
\${RUNTIME} rm -f legacy-wildfly-trading >/dev/null 2>&1 || true
\${RUNTIME} run -d --name legacy-wildfly-trading -p 8080:8080 \\
  -v \$HOME/legacy/trading/deployments:/opt/jboss/wildfly/standalone/deployments:Z \\
  quay.io/wildfly/wildfly:26.1.3.Final
echo 'WildFly started. Trading app will auto-deploy from deployments/.'"

cat <<EOF
Done.

Trading (VM):
  http://${HOST}:8080/fsi-trading/api/trades
EOF

