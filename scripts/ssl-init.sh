#!/bin/bash
# Uso: ./scripts/ssl-init.sh TU_DOMINIO.COM tu@email.com
#
# Ejecutar UNA SOLA VEZ en el VPS para obtener el primer certificado SSL.
# Los renovaciones posteriores son automáticas vía el servicio certbot del compose.

set -euo pipefail

DOMAIN=${1:?"Uso: $0 <dominio> <email>  — Ejemplo: $0 api.miempresa.com admin@miempresa.com"}
EMAIL=${2:?"Uso: $0 <dominio> <email>  — Ejemplo: $0 api.miempresa.com admin@miempresa.com"}

CERT_DIR="./data/certbot/conf/live/${DOMAIN}"

echo "==> Configurando SSL para ${DOMAIN} ..."

# ── Paso 1: Crear directorios necesarios ────────────────────────────────────
mkdir -p ./data/certbot/www ./data/certbot/conf

# ── Paso 2: Certificado temporal para que nginx pueda arrancar ──────────────
# (nginx no puede iniciar si los archivos de cert no existen)
if [ ! -f "${CERT_DIR}/fullchain.pem" ]; then
    echo "==> Creando certificado autofirmado temporal ..."
    mkdir -p "${CERT_DIR}"
    openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
        -keyout "${CERT_DIR}/privkey.pem" \
        -out    "${CERT_DIR}/fullchain.pem" \
        -subj "/CN=${DOMAIN}" 2>/dev/null
fi

# ── Paso 3: Arrancar nginx con el cert temporal ─────────────────────────────
echo "==> Arrancando nginx ..."
docker compose -f docker-compose.prod.yml up -d nginx
sleep 5

# ── Paso 4: Obtener certificado real de Let's Encrypt ───────────────────────
echo "==> Solicitando certificado a Let's Encrypt ..."
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
    --webroot \
    --webroot-path /var/www/certbot \
    --email "${EMAIL}" \
    --agree-tos \
    --no-eff-email \
    --force-renewal \
    -d "${DOMAIN}"

# ── Paso 5: Recargar nginx con el certificado real ──────────────────────────
echo "==> Recargando nginx con el certificado definitivo ..."
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload

echo ""
echo "✓ SSL configurado correctamente para ${DOMAIN}"
echo ""
echo "Arranca todos los servicios con:"
echo "  docker compose -f docker-compose.prod.yml up -d"
