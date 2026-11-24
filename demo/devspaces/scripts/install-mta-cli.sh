#!/usr/bin/env bash
# Best-effort MTA CLI install for Dev Spaces. Do not fail the workspace if download is blocked.
set -uo pipefail

MTA_HOME="${MTA_HOME:-/home/user/mta}"
DEFAULT_URL="${MTA_DEFAULT_URL:-https://github.com/konveyor/analyzer-lsp/releases/download/v0.6.0/kantra.linux.amd64}"
MTA_CLI_URL="${MTA_CLI_URL:-}"

mkdir -p "${MTA_HOME}"

if [[ -x "${MTA_HOME}/linux-mta-cli" ]] || [[ -x "${MTA_HOME}/mta-cli" ]]; then
  echo "MTA CLI already present under ${MTA_HOME}"
  ls -la "${MTA_HOME}" | head -n 20
  exit 0
fi

download() {
  local url="$1"
  local out="$2"
  echo "Downloading: ${url}"
  # --fail-with-body gives curl exit 22 on HTTP errors; capture and soft-fail
  if ! curl -fL --retry 3 --connect-timeout 20 --max-time 180 -o "${out}.partial" "${url}"; then
    echo "WARN: download failed (network policy / URL). Install MTA CLI manually later." >&2
    rm -f "${out}.partial"
    return 1
  fi
  mv "${out}.partial" "${out}"
  chmod +x "${out}"
  return 0
}

if [[ -n "${MTA_CLI_URL}" ]]; then
  TMP="$(mktemp -d)"
  if curl -fL --retry 3 --connect-timeout 20 --max-time 180 -o "${TMP}/mta.tgz" "${MTA_CLI_URL}"; then
    tar -xzf "${TMP}/mta.tgz" -C "${TMP}" || true
    BIN="$(find "${TMP}" -type f \( -name 'linux-mta-cli' -o -name 'mta-cli' -o -name 'kantra*' \) | head -n 1)"
    if [[ -n "${BIN}" ]]; then
      cp "${BIN}" "${MTA_HOME}/linux-mta-cli"
      chmod +x "${MTA_HOME}/linux-mta-cli"
    else
      echo "WARN: archive downloaded but no CLI binary found" >&2
    fi
  else
    echo "WARN: MTA_CLI_URL download failed" >&2
  fi
  rm -rf "${TMP}"
else
  download "${DEFAULT_URL}" "${MTA_HOME}/linux-mta-cli" || true
fi

if [[ -x "${MTA_HOME}/linux-mta-cli" ]]; then
  echo "Installed:"
  ls -la "${MTA_HOME}/linux-mta-cli"
  echo "Configure VS Code setting mta-core.analyzerPath=${MTA_HOME}/linux-mta-cli"
  exit 0
fi

echo "MTA CLI not installed yet. In the IDE terminal run:"
echo "  bash demo/devspaces/scripts/install-mta-cli.sh"
echo "Or set env MTA_CLI_URL to a reachable archive URL and re-run."
exit 0
