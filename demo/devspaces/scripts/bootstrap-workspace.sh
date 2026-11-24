#!/usr/bin/env bash
# postStart bootstrap: must always exit 0 so Dev Spaces can open the IDE.
set -uo pipefail

ROOT="${PROJECT_SOURCE:-}"
if [[ -z "${ROOT}" ]]; then
  ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
fi
SRC="${ROOT}/demo/devspaces"
JDK_HOME="${JDK_HOME:-/home/user/jdk-17}"

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

# This cluster's UDI has no usable java/SDKMAN — install a portable JDK into $HOME.
bash "${SRC}/scripts/install-jdk17.sh" || true

resolve_java_home() {
  local candidate
  for candidate in \
    "${JDK_HOME}" \
    "${JAVA_HOME:-}" \
    "${JAVA_HOME_17:-}" \
    /home/user/jdk-17 \
    /home/tooling/.sdkman/candidates/java/current \
    /home/user/.sdkman/candidates/java/current \
    /usr/lib/jvm/java-17-openjdk \
    /usr/lib/jvm/jre-17-openjdk
  do
    if [[ -n "${candidate}" && -x "${candidate}/bin/java" ]]; then
      echo "${candidate}"
      return 0
    fi
  done
  if command -v java >/dev/null 2>&1; then
    local java_bin
    java_bin="$(command -v java)"
    if [[ -L "${java_bin}" ]]; then
      java_bin="$(readlink -f "${java_bin}" 2>/dev/null || true)"
    fi
    if [[ -n "${java_bin}" && -x "${java_bin}" ]]; then
      echo "$(cd "$(dirname "${java_bin}")/.." && pwd)"
      return 0
    fi
  fi
  return 1
}

JAVA_RESOLVED="$(resolve_java_home || true)"
if [[ -n "${JAVA_RESOLVED}" ]]; then
  echo "Resolved JAVA_HOME=${JAVA_RESOLVED}"
  "${JAVA_RESOLVED}/bin/java" -version 2>&1 || true
  mkdir -p /home/user/.local/bin
  ln -sfn "${JAVA_RESOLVED}/bin/java" /home/user/.local/bin/java || true
  ln -sfn "${JAVA_RESOLVED}/bin/javac" /home/user/.local/bin/javac || true
  if ! grep -q 'MTA_DEVSPACES_JAVA' /home/user/.bashrc 2>/dev/null; then
    cat >> /home/user/.bashrc <<EOF

# MTA_DEVSPACES_JAVA
export JAVA_HOME="${JAVA_RESOLVED}"
export PATH="\${JAVA_HOME}/bin:/home/user/.local/bin:/home/user/mta:\${PATH}"
EOF
  else
    # Refresh JAVA_HOME line if bootstrap re-runs with a new install
    sed -i "s|^export JAVA_HOME=.*|export JAVA_HOME=\"${JAVA_RESOLVED}\"|" /home/user/.bashrc || true
  fi
  if [[ -f "${ROOT}/.vscode/settings.json" ]] && command -v python3 >/dev/null 2>&1; then
    python3 - "${ROOT}/.vscode/settings.json" "${JAVA_RESOLVED}" <<'PY' || true
import json, sys
path, jhome = sys.argv[1], sys.argv[2]
with open(path) as f:
    data = json.load(f)
data["java.jdt.ls.java.home"] = jhome
data["java.configuration.runtimes"] = [{
    "name": "JavaSE-17",
    "path": jhome,
    "default": True,
}]
with open(path, "w") as f:
    json.dump(data, f, indent=2)
    f.write("\n")
PY
  fi
else
  echo "WARN: JDK still missing after install attempt. Run: bash demo/devspaces/scripts/install-jdk17.sh" >&2
fi

bash "${SRC}/scripts/install-mta-cli.sh" || true

mkdir -p /home/user/.local/bin
if [[ -x /home/user/mta/linux-mta-cli ]]; then
  ln -sfn /home/user/mta/linux-mta-cli /home/user/.local/bin/linux-mta-cli || true
fi

echo "Workspace bootstrap finished."
echo "If MTA still says Java is missing: open a new terminal, verify 'java -version', then Reload Window."
exit 0
