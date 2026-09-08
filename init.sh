#!/bin/bash
# Heavyweight, human-triggered backend bootstrap. Do not invoke from hooks.
set -euo pipefail

APP_PORT=8080
LOG_DIR="logs"
LOG_FILE="$LOG_DIR/backend.log"

if ! command -v java >/dev/null 2>&1; then
  echo "[ERROR] Java 17+ is required."
  exit 1
fi
if ! command -v curl >/dev/null 2>&1; then
  echo "[ERROR] curl is required for the readiness check."
  exit 1
fi

mkdir -p "$LOG_DIR"
cd back/demo
nohup ./mvnw spring-boot:run > "../../$LOG_FILE" 2>&1 &
echo "[init] Backend started (PID $!, logs -> $LOG_FILE)"

for _ in $(seq 1 60); do
  if curl -s -o /dev/null -w "%{http_code}" "http://localhost:$APP_PORT/swagger-ui.html" | grep -qv '^000$'; then
    echo "[init] Backend reachable on port $APP_PORT"
    exit 0
  fi
  sleep 1
done

echo "[ERROR] Backend was not reachable after 60 seconds; check $LOG_FILE"
exit 1
