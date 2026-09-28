#!/usr/bin/env bash
# Flash d'un GSI sur la Lenovo TB-J606L (slot a), à lancer tablette en fastboot (bootloader).
# Usage : flash-gsi.sh <system.img> [--keep-product]
# Efface toutes les données de la tablette. Retour arrière : restore-stock.sh
set -euo pipefail

GSI_IMG="${1:?Usage: $0 <system.img> [--keep-product]}"
KEEP_PRODUCT="${2:-}"
STOCK_DIR="${STOCK_DIR:-/lab/lenovo/stock/S320435}"
SLOT="a"

[ -f "$GSI_IMG" ] || { echo "Image introuvable : $GSI_IMG" >&2; exit 1; }
fastboot devices | grep -q fastboot || { echo "Tablette non détectée en fastboot" >&2; exit 1; }

read -rp "Flasher $(basename "$GSI_IMG") sur system_$SLOT et EFFACER les données ? [oui/N] " answer
[ "$answer" = "oui" ] || exit 1

# 1. Bootloader : vbmeta stock avec vérification AVB désactivée (sinon le GSI non signé Lenovo ne démarre pas).
# vbmeta-disabled.img = vbmeta.img avec flags=3 (offset 120, big-endian) : `fastboot --disable-verification`
# échoue ici avec "Failed to find AVB_MAGIC at offset: 0".
fastboot flash "vbmeta_${SLOT}" "$STOCK_DIR/vbmeta-disabled.img"

# 2. fastbootd (fastboot userspace du recovery) : seul mode qui peut écrire les partitions logiques de super
fastboot reboot fastboot
fastboot wait-for-device 2>/dev/null || sleep 15
[ "$(fastboot getvar is-userspace 2>&1 | awk '/is-userspace/ {print $2}')" = "yes" ] || { echo "fastbootd non atteint" >&2; exit 1; }

# 3. product_a contient les applis/overlays Lenovo, souvent incompatibles avec un GSI
if [ "$KEEP_PRODUCT" != "--keep-product" ]; then
    fastboot delete-logical-partition "product_${SLOT}" || true
fi

fastboot flash "system_${SLOT}" "$GSI_IMG"
fastboot -w
fastboot reboot
