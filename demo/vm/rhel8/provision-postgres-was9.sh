#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_WAS9_HOST

HOST="${VM_WAS9_HOST}"

ACCOUNTS_DB="${PG_ACCOUNTS_DB:-accounts}"
ACCOUNTS_USER="${PG_ACCOUNTS_USER:-accounts}"
ACCOUNTS_PASSWORD="${PG_ACCOUNTS_PASSWORD:-accounts}"
CARDS_DB="${PG_CARDS_DB:-cards}"
CARDS_USER="${PG_CARDS_USER:-cards}"
CARDS_PASSWORD="${PG_CARDS_PASSWORD:-cards}"

echo "Provisioning PostgreSQL roles/DBs for WAS9 Liberty apps on ${HOST}"

# Ensure PostgreSQL itself is installed (reuse EAP-style bootstrap if needed)
ssh_cmd "${HOST}" "set -euo pipefail
if ! systemctl is-active --quiet postgresql; then
  sudo dnf -y install postgresql-server postgresql
  sudo postgresql-setup --initdb || true
  sudo systemctl enable --now postgresql
  sudo sed -i.bak \"s/^#listen_addresses =.*/listen_addresses = '127.0.0.1'/\" /var/lib/pgsql/data/postgresql.conf || true
  PGHBA=/var/lib/pgsql/data/pg_hba.conf
  sudo cp -n \"\${PGHBA}\" \"\${PGHBA}.bak\" || true
  sudo sed -i '/^[[:space:]]*host[[:space:]]\\+all[[:space:]]\\+all[[:space:]]\\+127\\.0\\.0\\.1\\/32/d' \"\${PGHBA}\"
  sudo sed -i '/^[[:space:]]*host[[:space:]]\\+all[[:space:]]\\+all[[:space:]]\\+::1\\/128/d' \"\${PGHBA}\"
  sudo sed -i \"1ihost\\tall\\tall\\t127.0.0.1/32\\tscram-sha-256\" \"\${PGHBA}\"
  sudo sed -i \"1ihost\\tall\\tall\\t::1/128\\tscram-sha-256\" \"\${PGHBA}\"
  sudo systemctl restart postgresql
  sudo -u postgres psql -c \"ALTER SYSTEM SET password_encryption='scram-sha-256';\" || true
  sudo -u postgres psql -c \"SELECT pg_reload_conf();\" || true
fi

ensure_db() {
  local user=\"\$1\" password=\"\$2\" db=\"\$3\"
  sudo -u postgres psql -tc \"SELECT 1 FROM pg_roles WHERE rolname='\${user}'\" | grep -q 1 \\
    || sudo -u postgres psql -c \"CREATE USER \${user} WITH PASSWORD '\${password}';\"
  sudo -u postgres psql -c \"ALTER USER \${user} WITH PASSWORD '\${password}';\" || true
  sudo -u postgres psql -tc \"SELECT 1 FROM pg_database WHERE datname='\${db}'\" | grep -q 1 \\
    || sudo -u postgres createdb -O \${user} \${db}
}

ensure_db '${ACCOUNTS_USER}' '${ACCOUNTS_PASSWORD}' '${ACCOUNTS_DB}'
ensure_db '${CARDS_USER}' '${CARDS_PASSWORD}' '${CARDS_DB}'

# Required for Liberty XA datasources (JPA + JMS in one EJB tx)
sudo -u postgres psql -c \"ALTER SYSTEM SET max_prepared_transactions = 100;\" || true
sudo systemctl restart postgresql
"

cat <<EOF
Done.

Accounts DB: ${ACCOUNTS_DB} / ${ACCOUNTS_USER}@127.0.0.1:5432
Cards DB:    ${CARDS_DB} / ${CARDS_USER}@127.0.0.1:5432
EOF
