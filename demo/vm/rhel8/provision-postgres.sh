#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/../_lib.sh"

require_env VM_EAP7_HOST

HOST="${VM_EAP7_HOST}"

PG_DB="${PG_DB:-payments}"
PG_USER="${PG_USER:-payments}"
PG_PASSWORD="${PG_PASSWORD:-payments}"

echo "Provisioning PostgreSQL on ${HOST} (RHEL 8)"

ssh_cmd "${HOST}" "set -euo pipefail
sudo dnf -y install postgresql-server postgresql
sudo postgresql-setup --initdb || true
sudo systemctl enable --now postgresql

# Local-only access for demo
sudo sed -i.bak 's/^#listen_addresses =.*/listen_addresses = '\\''127.0.0.1'\\''/' /var/lib/pgsql/data/postgresql.conf

# Force password auth on localhost (fixes: Ident authentication failed)
PGHBA=/var/lib/pgsql/data/pg_hba.conf
sudo cp -n \"\${PGHBA}\" \"\${PGHBA}.bak\" || true
# Remove any existing host rules for 127.0.0.1/32 so we can insert ours first
sudo sed -i '/^[[:space:]]*host[[:space:]]\\+all[[:space:]]\\+all[[:space:]]\\+127\\.0\\.0\\.1\\/32/d' \"\${PGHBA}\"
sudo sed -i '/^[[:space:]]*host[[:space:]]\\+all[[:space:]]\\+all[[:space:]]\\+::1\\/128/d' \"\${PGHBA}\"
sudo sed -i \"1ihost\\tall\\tall\\t127.0.0.1/32\\tscram-sha-256\" \"\${PGHBA}\"
sudo sed -i \"1ihost\\tall\\tall\\t::1/128\\tscram-sha-256\" \"\${PGHBA}\"

sudo systemctl restart postgresql

sudo -u postgres psql -c \"ALTER SYSTEM SET password_encryption='scram-sha-256';\" || true
sudo -u postgres psql -c \"SELECT pg_reload_conf();\" || true
sudo -u postgres psql -tc \"SELECT 1 FROM pg_roles WHERE rolname='${PG_USER}'\" | grep -q 1 || sudo -u postgres psql -c \"CREATE USER ${PG_USER} WITH PASSWORD '${PG_PASSWORD}';\"
sudo -u postgres psql -c \"ALTER USER ${PG_USER} WITH PASSWORD '${PG_PASSWORD}';\" || true
sudo -u postgres psql -tc \"SELECT 1 FROM pg_database WHERE datname='${PG_DB}'\" | grep -q 1 || sudo -u postgres createdb -O ${PG_USER} ${PG_DB}
"

cat <<EOF
Done.

DB:
  name: ${PG_DB}
  user: ${PG_USER}
  host: 127.0.0.1
  port: 5432
EOF

