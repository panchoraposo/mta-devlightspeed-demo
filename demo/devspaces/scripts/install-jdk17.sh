#!/usr/bin/env bash
# Install a user-local Temurin JDK 17 for Dev Spaces (no root required).
# Soft-fails (exit 0) so workspace postStart never blocks the IDE.
set -uo pipefail

JDK_HOME="${JDK_HOME:-/home/user/jdk-17}"
JDK_URL="${JDK_URL:-https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk}"

if [[ -x "${JDK_HOME}/bin/java" ]]; then
  echo "JDK already present at ${JDK_HOME}"
  "${JDK_HOME}/bin/java" -version 2>&1 || true
  exit 0
fi

TMP="$(mktemp -d)"
cleanup() { rm -rf "${TMP}"; }
trap cleanup EXIT

echo "Downloading Temurin JDK 17 → ${JDK_HOME}"
if ! curl -fL --retry 3 --connect-timeout 20 --max-time 300 -o "${TMP}/jdk.tgz" "${JDK_URL}"; then
  echo "WARN: JDK download failed (network policy / URL). Set JDK_URL to an internal mirror and re-run." >&2
  exit 0
fi

mkdir -p "${TMP}/extract" "${JDK_HOME}"
if ! tar -xzf "${TMP}/jdk.tgz" -C "${TMP}/extract"; then
  echo "WARN: failed to extract JDK archive" >&2
  exit 0
fi

# Archive contains a single top-level directory (e.g. jdk-17.0.x+y)
INNER="$(find "${TMP}/extract" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
if [[ -z "${INNER}" || ! -x "${INNER}/bin/java" ]]; then
  echo "WARN: unexpected JDK archive layout" >&2
  exit 0
fi

# Replace destination atomically-ish
rm -rf "${JDK_HOME}.partial"
mv "${INNER}" "${JDK_HOME}.partial"
rm -rf "${JDK_HOME}"
mv "${JDK_HOME}.partial" "${JDK_HOME}"

mkdir -p /home/user/.local/bin
ln -sfn "${JDK_HOME}/bin/java" /home/user/.local/bin/java
ln -sfn "${JDK_HOME}/bin/javac" /home/user/.local/bin/javac

echo "Installed:"
"${JDK_HOME}/bin/java" -version 2>&1 || true
echo "JAVA_HOME=${JDK_HOME}"
exit 0
