#!/bin/bash
# Genera valores criptográficamente seguros para el .env de producción.
# Ejecutar en el VPS o en cualquier máquina con openssl instalado:
#   chmod +x scripts/generate-secrets.sh && ./scripts/generate-secrets.sh

set -euo pipefail

command -v openssl >/dev/null 2>&1 || { echo "Error: openssl no está instalado."; exit 1; }

echo "# ── Valores generados el $(date '+%Y-%m-%d %H:%M') ──────────────────"
echo "# Pegar en .env (reemplazando los CAMBIAR_POR_...)"
echo ""
echo "JWT_SECRET=$(openssl rand -hex 32)"
echo "DB_PASSWORD=$(openssl rand -base64 32 | tr -dc 'A-Za-z0-9!@#%^&*' | head -c 24)"
echo ""
echo "# IMPORTANTE: guarda estos valores en un gestor de contraseñas."
echo "# No los compartas ni los subas a git."
