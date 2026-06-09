#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS="$(date +"%Y%m%dT%H%M%S")"

RUN_DIR="${ROOT}/demo/runs/${TS}"
APPS_DIR="${RUN_DIR}/apps"
OUT_DIR="${RUN_DIR}/mta-output"

mkdir -p "${APPS_DIR}" "${OUT_DIR}"

rsync -a --exclude 'target' --exclude '.vscode' --exclude '.idea' --exclude '.settings' --exclude '.classpath' --exclude '.project' \
  "${ROOT}/demo/source-apps/" \
  "${APPS_DIR}/"

cat <<EOF
Created demo run:
  ${RUN_DIR}

Apps (fresh copy):
  ${APPS_DIR}

Recommended MTA output dir:
  ${OUT_DIR}
EOF

