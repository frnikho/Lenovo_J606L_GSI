#!/usr/bin/env bash
# Retour au firmware stock Lenovo TB-J606L (S320435) depuis le mode fastboot (bootloader).
# Efface toutes les données de la tablette.
set -euo pipefail

STOCK_DIR="${STOCK_DIR:-/lab/lenovo/stock/S320435}"
SLOT="${SLOT:-a}"

cd "$STOCK_DIR"
sha256sum -c --quiet SHA256SUMS

fastboot devices | grep -q fastboot || { echo "Tablette non détectée en fastboot" >&2; exit 1; }
if [ "$(fastboot getvar is-userspace 2>&1 | awk '/is-userspace/ {print $2}')" = "yes" ]; then
    echo "Tablette en fastbootd : retour au bootloader" >&2
    fastboot reboot bootloader
    sleep 10
fi

read -rp "Flasher le stock S320435 sur le slot $SLOT et EFFACER les données ? [oui/N] " answer
[ "$answer" = "oui" ] || exit 1

for part in boot recovery dtbo vbmeta vbmeta_system; do
    fastboot flash "${part}_${SLOT}" "$part.img"
done
# super.img est l'image sparse reconstruite à partir de super_1..5 (voir notes/journal.md)
fastboot flash super super.img
fastboot set_active "$SLOT"
fastboot erase metadata
fastboot format:f2fs userdata
fastboot reboot
