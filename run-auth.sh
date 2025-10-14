#!/bin/bash
set -e
cd "$(dirname "$0")"

# Cargar variables desde .env.jwt (una carpeta arriba)
source ../.env.jwt

echo "[Auth-SVC] Usando secreto de $(pwd)"
echo -n "$APP_JWT_SECRET" | wc -c

export APP_JWT_SECRET
export APP_JWT_EXP_MIN

./gradlew bootRun