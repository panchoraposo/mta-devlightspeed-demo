#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

if [[ -f "${SCRIPT_DIR}/.env" ]]; then
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/.env"
fi

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "Missing env var: ${name}" >&2
    exit 1
  fi
}

ssh_cmd() {
  require_env VM_USER
  require_env VM_SSH_KEY
  local host="$1"
  shift
  ssh -i "${VM_SSH_KEY}" -o StrictHostKeyChecking=accept-new "${VM_USER}@${host}" "$@"
}

scp_to() {
  require_env VM_USER
  require_env VM_SSH_KEY
  local src="$1"
  local host="$2"
  local dst="$3"
  scp -i "${VM_SSH_KEY}" -o StrictHostKeyChecking=accept-new "${src}" "${VM_USER}@${host}:${dst}"
}

