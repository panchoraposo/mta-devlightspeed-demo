#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS="$(date +"%Y%m%dT%H%M%S")"

RUN_DIR="${ROOT}/demo/runs/${TS}"
LEGACY_DIR="${RUN_DIR}/apps"
MODERN_DIR="${RUN_DIR}/modern-apps"
OUT_DIR="${RUN_DIR}/mta-output"

mkdir -p "${LEGACY_DIR}" "${MODERN_DIR}" "${OUT_DIR}"

# Legacy golden source (EAP7/SB2/Camel/etc.)
rsync -a --exclude 'target' --exclude '.vscode' --exclude '.idea' --exclude '.settings' --exclude '.classpath' --exclude '.project' \
  "${ROOT}/demo/source-apps/" \
  "${LEGACY_DIR}/"

# Modern golden source (Quarkus-ready apps)
if [[ -d "${ROOT}/demo/source-modern-apps" ]]; then
  rsync -a --exclude 'target' --exclude '.vscode' --exclude '.idea' \
    "${ROOT}/demo/source-modern-apps/" \
    "${MODERN_DIR}/"
fi

cat <<EOF
Created demo run:
  ${RUN_DIR}

Legacy apps (fresh copy):
  ${LEGACY_DIR}

Modern apps (fresh copy):
  ${MODERN_DIR}

Recommended MTA output dir:
  ${OUT_DIR}
EOF

