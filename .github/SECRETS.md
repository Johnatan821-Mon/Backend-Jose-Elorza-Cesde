# Secrets requeridos en GitHub Actions

Configura estos secrets en:  
**GitHub → Settings → Secrets and variables → Actions → New repository secret**

---

## Secrets de la aplicación

| Secret | Usado en | Cómo generarlo |
|--------|----------|----------------|
| `JWT_SECRET` | `ci.yml` (tests) | `openssl rand -hex 32` |

---

## Secrets de CI/CD (Docker + VPS)

| Secret | Usado en | Descripción |
|--------|----------|-------------|
| `GHCR_TOKEN` | `cd.yml` | PAT de GitHub con scope `read:packages`. Crearlo en **GitHub → Settings → Developer settings → Personal access tokens → Fine-grained** |
| `VPS_HOST` | `cd.yml` | IP pública o dominio del VPS (ej. `123.45.67.89` o `api.miempresa.com`) |
| `VPS_USER` | `cd.yml` | Usuario SSH del VPS (ej. `ubuntu`, `debian`, `root`) |
| `VPS_SSH_KEY` | `cd.yml` | Clave SSH privada. Ver instrucciones abajo. |

---

## Cómo crear la clave SSH para el deploy

Ejecutar **en tu máquina local** (no en el VPS):

```bash
# 1. Generar par de claves dedicado para el deploy
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/deploy_key -N ""

# 2. Agregar la clave pública al VPS (autoriza que GitHub Actions se conecte)
ssh-copy-id -i ~/.ssh/deploy_key.pub usuario@IP_DEL_VPS
# O manualmente: cat ~/.ssh/deploy_key.pub >> ~/.ssh/authorized_keys en el VPS

# 3. Copiar la clave privada — este es el valor del secret VPS_SSH_KEY
cat ~/.ssh/deploy_key
```

Pegar el contenido completo de `deploy_key` (incluyendo `-----BEGIN...` y `-----END...`) como valor del secret `VPS_SSH_KEY`.

---

## Cómo crear GHCR_TOKEN

1. Ir a **GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens**
2. Click **Generate new token**
3. Nombre: `ghcr-deploy-read`
4. Expiration: 90 días (renovar periódicamente)
5. Repository access: solo este repositorio
6. Permissions → **Packages → Read**
7. Copiar el token generado y guardarlo como secret `GHCR_TOKEN`

---

## Resumen de qué secret va en cada workflow

```
ci.yml      →  JWT_SECRET
cd.yml      →  GHCR_TOKEN, VPS_HOST, VPS_USER, VPS_SSH_KEY
security.yml → (ninguno adicional)
```
