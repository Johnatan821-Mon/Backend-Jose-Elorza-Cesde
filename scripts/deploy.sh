#!/bin/bash
# Uso: ./scripts/deploy.sh [imagen]
#
# Sin argumento: construye la imagen localmente desde el Dockerfile.
# Con argumento: descarga la imagen del registry (GHCR) y reinicia la app.
#
# Ejemplos:
#   ./scripts/deploy.sh                                          # build local
#   ./scripts/deploy.sh ghcr.io/usuario/clientes-api:sha-abc123 # desde GHCR

set -euo pipefail

COMPOSE_FILE="docker-compose.prod.yml"
APP_IMAGE=${1:-""}

echo "==> [1/4] Actualizando código ..."
git pull origin main

if [ -n "${APP_IMAGE}" ]; then
    echo "==> [2/4] Descargando imagen ${APP_IMAGE} ..."
    export APP_IMAGE
    docker compose -f "${COMPOSE_FILE}" pull app
else
    echo "==> [2/4] Construyendo imagen localmente ..."
    docker compose -f "${COMPOSE_FILE}" build app
fi

echo "==> [3/4] Reiniciando app (sin tocar postgres ni nginx) ..."
docker compose -f "${COMPOSE_FILE}" up -d --no-deps app

echo "==> [4/4] Verificando arranque (30s) ..."
sleep 30

echo ""
echo "Estado de servicios:"
docker compose -f "${COMPOSE_FILE}" ps

echo ""
echo "Últimos logs de la app:"
docker compose -f "${COMPOSE_FILE}" logs app --tail=30

echo ""
echo "Deploy completado."
