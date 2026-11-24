#!/usr/bin/env bash
set -euo pipefail

ROOT="${PROJECT_SOURCE:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
SRC="${ROOT}/demo/devspaces"

echo "Bootstrapping Dev Spaces workspace from ${SRC}"

# VS Code / Che-Code settings for MTA (Linux analyzer path)
mkdir -p "${ROOT}/.vscode"
cp -f "${SRC}/.vscode/extensions.json" "${ROOT}/.vscode/extensions.json"
cp -f "${SRC}/.vscode/settings.json" "${ROOT}/.vscode/settings.json"

bash "${SRC}/scripts/install-mta-cli.sh"

echo "Workspace ready for MTA + Developer Lightspeed (source=websphere → quarkus)."
