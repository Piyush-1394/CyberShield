#!/usr/bin/env bash
set -e

echo "=== Starting CyberShield AI Backend from Root on Render ==="

# 1. Resolve or Install Java 21 at runtime
if [ -d "backend/.jdk21/bin" ]; then
  export JAVA_HOME="$(pwd)/backend/.jdk21"
  export PATH="$JAVA_HOME/bin:$PATH"
elif [ -d "./.jdk21/bin" ]; then
  export JAVA_HOME="$(pwd)/.jdk21"
  export PATH="$JAVA_HOME/bin:$PATH"
elif [ -d "$HOME/.jdk21/bin" ]; then
  export JAVA_HOME="$HOME/.jdk21"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Java not found at runtime. Auto-installing Eclipse Temurin OpenJDK 21..."
  mkdir -p ./.jdk21
  JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz"
  curl -sSL "$JDK_URL" | tar -xz -C ./.jdk21 --strip-components=1
  export JAVA_HOME="$(pwd)/.jdk21"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "Java Runtime: $(java -version 2>&1 | head -n 1)"

# 2. Translate Render's native DATABASE_URL to JDBC if present
if [ -n "$DATABASE_URL" ] && [ -z "$SPRING_DATASOURCE_URL" ]; then
  CLEAN_URL="${DATABASE_URL#postgres://}"
  CLEAN_URL="${CLEAN_URL#postgresql://}"
  
  USER_PASS="${CLEAN_URL%%@*}"
  HOST_PORT_DB="${CLEAN_URL#*@}"
  
  export SPRING_DATASOURCE_USERNAME="${USER_PASS%%:*}"
  export SPRING_DATASOURCE_PASSWORD="${USER_PASS#*:}"
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST_PORT_DB}"
  echo "Configured JDBC DataSource from Render DATABASE_URL: jdbc:postgresql://${HOST_PORT_DB%/*}/..."
fi

# 3. Set active profile to prod if not set
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

# 4. Locate JAR file
JAR_FILE=""
if [ -f "backend/target/cybershield-api-1.0.0.jar" ]; then
  JAR_FILE="backend/target/cybershield-api-1.0.0.jar"
elif [ -f "target/cybershield-api-1.0.0.jar" ]; then
  JAR_FILE="target/cybershield-api-1.0.0.jar"
else
  JAR_FILE=$(find . -name "*.jar" ! -name "*-sources.jar" | head -n 1)
fi

if [ -z "$JAR_FILE" ]; then
  echo "Error: Could not locate built jar file!"
  exit 1
fi

echo "Launching: $JAR_FILE with profile: $SPRING_PROFILES_ACTIVE on port: ${PORT:-8080}"
exec java -jar "$JAR_FILE"
