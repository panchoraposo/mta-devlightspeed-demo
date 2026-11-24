#!/usr/bin/env bash
# postStart bootstrap: must always exit 0 so Dev Spaces can open the IDE.
set -uo pipefail

ROOT="${PROJECT_SOURCE:-}"
if [[ -z "${ROOT}" ]]; then
  ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
fi
SRC="${ROOT}/demo/devspaces"

echo "Bootstrapping Dev Spaces workspace from ${SRC}"

if [[ ! -d "${SRC}" ]]; then
  echo "WARN: ${SRC} not found yet; skipping bootstrap copy" >&2
  exit 0
fi

# VS Code / Che-Code settings for MTA (Linux analyzer path)
mkdir -p "${ROOT}/.vscode"
if [[ -f "${SRC}/.vscode/extensions.json" ]]; then
  cp -f "${SRC}/.vscode/extensions.json" "${ROOT}/.vscode/extensions.json" || true
fi
if [[ -f "${SRC}/.vscode/settings.json" ]]; then
  cp -f "${SRC}/.vscode/settings.json" "${ROOT}/.vscode/settings.json" || true
fi

# Best-effort CLI install (never fail the workspace)
bash "${SRC}/scripts/install-mta-cli.sh" || true

echo "Workspace bootstrap finished (MTA CLI may need a manual install if download was blocked)."
exit 0
