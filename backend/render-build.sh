#!/usr/bin/env bash
set -e

echo "=== CyberShield AI Render Build Starting ==="

REQUIRED_JAVA=21
NEED_JDK=true

if command -v java >/dev/null 2>&1; then
  JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed 's/^1\.//' | cut -d'.' -f1)
  if [ "$JAVA_VER" -ge "$REQUIRED_JAVA" ] 2>/dev/null; then
    echo "Found compatible Java $JAVA_VER installed on system."
    NEED_JDK=false
  fi
fi

if [ "$NEED_JDK" = true ]; then
  echo "Installing Eclipse Temurin OpenJDK $REQUIRED_JAVA (LTS) for Linux x64..."
  # Install into local project directory so Render build uploader preserves it
  mkdir -p ./.jdk21
  JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz"
  curl -sSL "$JDK_URL" | tar -xz -C ./.jdk21 --strip-components=1
  export JAVA_HOME="$(pwd)/.jdk21"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "Installed: $($JAVA_HOME/bin/java -version 2>&1 | head -n 1)"
fi

# Ensure mvnw has execute permissions
chmod +x ./mvnw 2>/dev/null || chmod +x ./backend/mvnw 2>/dev/null || true

# Build project
if [ -f "pom.xml" ]; then
  ./mvnw clean package -DskipTests
elif [ -f "backend/pom.xml" ]; then
  cd backend
  ./mvnw clean package -DskipTests
  cd ..
fi

echo "=== CyberShield AI Build Completed Successfully ==="
