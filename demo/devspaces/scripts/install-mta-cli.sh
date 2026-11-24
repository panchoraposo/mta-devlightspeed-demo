#!/usr/bin/env bash
set -euo pipefail

MTA_HOME="${MTA_HOME:-/home/user/mta}"
MTA_VERSION="${MTA_VERSION:-8.1.0}"
# Official CLI archives follow the pattern used by MTA releases on developers.redhat.com / mirror.
# Override MTA_CLI_URL if your cluster cannot reach the default host.
DEFAULT_URL="https://github.com/konveyor/analyzer-lsp/releases/download/v0.6.0/kantra.linux.amd64"
# Prefer a full MTA CLI tarball when provided; otherwise install kantra-compatible binary name expected by the extension.
MTA_CLI_URL="${MTA_CLI_URL:-}"

mkdir -p "${MTA_HOME}"

if [[ -x "${MTA_HOME}/linux-mta-cli" ]] || [[ -x "${MTA_HOME}/mta-cli" ]]; then
  echo "MTA CLI already present under ${MTA_HOME}"
  ls -la "${MTA_HOME}" | head -n 20
  exit 0
fi

if [[ -n "${MTA_CLI_URL}" ]]; then
  echo "Downloading MTA CLI from ${MTA_CLI_URL}"
  TMP="$(mktemp -d)"
  curl -fL --retry 3 -o "${TMP}/mta.tgz" "${MTA_CLI_URL}"
  tar -xzf "${TMP}/mta.tgz" -C "${TMP}"
  # Find a binary named *mta-cli* or kantra and install as linux-mta-cli
  BIN="$(find "${TMP}" -type f \( -name 'linux-mta-cli' -o -name 'mta-cli' -o -name 'kantra*' \) | head -n 1)"
  if [[ -z "${BIN}" ]]; then
    echo "Could not find CLI binary in archive" >&2
    exit 1
  fi
  cp "${BIN}" "${MTA_HOME}/linux-mta-cli"
  chmod +x "${MTA_HOME}/linux-mta-cli"
  rm -rf "${TMP}"
else
  echo "No MTA_CLI_URL set — installing analyzer binary as ${MTA_HOME}/linux-mta-cli from ${DEFAULT_URL}"
  echo "For the full Red Hat MTA CLI, set MTA_CLI_URL to your licensed/downloadable archive."
  curl -fL --retry 3 -o "${MTA_HOME}/linux-mta-cli" "${DEFAULT_URL}"
  chmod +x "${MTA_HOME}/linux-mta-cli"
fi

echo "Installed:"
ls -la "${MTA_HOME}/linux-mta-cli"
echo "Configure VS Code setting mta-core.analyzerPath=${MTA_HOME}/linux-mta-cli"
