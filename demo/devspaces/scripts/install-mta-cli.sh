#!/usr/bin/env bash
# Best-effort MTA/Kantra CLI install for Dev Spaces. Soft-fail so postStart never blocks.
set -uo pipefail

MTA_HOME="${MTA_HOME:-/home/user/mta}"
# Official Konveyor zip (~700MB) includes analyzer + providers for containerless mode.
DEFAULT_URL="${MTA_DEFAULT_URL:-https://github.com/konveyor/kantra/releases/download/v0.11.0/kantra.linux.amd64.zip}"
MTA_CLI_URL="${MTA_CLI_URL:-}"

mkdir -p "${MTA_HOME}" /home/user/.local/bin /home/user/.kantra

if [[ -x "${MTA_HOME}/linux-mta-cli" ]] || [[ -x "${MTA_HOME}/mta-cli" ]] || [[ -x "${MTA_HOME}/kantra" ]]; then
  echo "MTA CLI already present under ${MTA_HOME}"
  ls -la "${MTA_HOME}" | head -n 20
  exit 0
fi

install_from_archive() {
  local url="$1"
  local tmp
  tmp="$(mktemp -d)"
  echo "Downloading MTA/Kantra CLI: ${url}"
  # Large archive; allow up to 20 minutes.
  if ! curl -fL --retry 3 --connect-timeout 20 --max-time 1200 -o "${tmp}/cli.zip" "${url}"; then
    echo "WARN: download failed (network / auth / URL). Set MTA_CLI_URL to an internal mirror." >&2
    rm -rf "${tmp}"
    return 1
  fi
  mkdir -p "${tmp}/extract"
  if ! unzip -q "${tmp}/cli.zip" -d "${tmp}/extract"; then
    # Some mirrors ship tar.gz
    if ! tar -xzf "${tmp}/cli.zip" -C "${tmp}/extract" 2>/dev/null; then
      echo "WARN: failed to extract CLI archive" >&2
      rm -rf "${tmp}"
      return 1
    fi
  fi

  local bin
  bin="$(find "${tmp}/extract" -type f \( -name 'linux-mta-cli' -o -name 'mta-cli' -o -name 'kantra' -o -name 'kantra.linux.amd64' \) | head -n 1)"
  if [[ -z "${bin}" ]]; then
    echo "WARN: archive downloaded but no CLI binary found" >&2
    find "${tmp}/extract" -maxdepth 3 -type f | head -n 40 >&2 || true
    rm -rf "${tmp}"
    return 1
  fi

  # Prefer keeping full extract under ~/.kantra (providers/rules live next to binary).
  local root
  root="$(cd "$(dirname "${bin}")" && pwd)"
  if [[ -d "${root}" ]]; then
    rm -rf /home/user/.kantra/kantra-dist
    mkdir -p /home/user/.kantra
    cp -a "${root}/." /home/user/.kantra/kantra-dist/
    bin="/home/user/.kantra/kantra-dist/$(basename "${bin}")"
  fi

  cp -f "${bin}" "${MTA_HOME}/linux-mta-cli"
  cp -f "${bin}" "${MTA_HOME}/kantra"
  chmod +x "${MTA_HOME}/linux-mta-cli" "${MTA_HOME}/kantra"
  ln -sfn "${MTA_HOME}/linux-mta-cli" /home/user/.local/bin/linux-mta-cli
  ln -sfn "${MTA_HOME}/kantra" /home/user/.local/bin/kantra
  rm -rf "${tmp}"
  return 0
}

if [[ -n "${MTA_CLI_URL}" ]]; then
  install_from_archive "${MTA_CLI_URL}" || true
else
  install_from_archive "${DEFAULT_URL}" || true
fi

if [[ -x "${MTA_HOME}/linux-mta-cli" ]]; then
  echo "Installed:"
  ls -la "${MTA_HOME}/linux-mta-cli" "${MTA_HOME}/kantra" 2>/dev/null || true
  "${MTA_HOME}/linux-mta-cli" version 2>&1 | head -n 5 || true
  echo "Configure VS Code setting mta-core.analyzerPath=${MTA_HOME}/linux-mta-cli"
  exit 0
fi

echo "MTA CLI not installed yet. In the IDE terminal run:"
echo "  bash demo/devspaces/scripts/install-mta-cli.sh"
echo "Or set env MTA_CLI_URL to a reachable archive URL and re-run."
exit 0
