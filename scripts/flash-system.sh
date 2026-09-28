#!/usr/bin/env bash
# Mise à jour rapide de system_a (sans wipe) depuis Android démarré avec adb, puis attente du boot complet.
# Usage : flash-system.sh <system.img>
set -euo pipefail

IMG="${1:?Usage: $0 <system.img>}"
BOOT_TIMEOUT_S=300

[ -f "$IMG" ] || { echo "Image introuvable : $IMG" >&2; exit 1; }
adb reboot fastboot
for _ in $(seq 60); do fastboot devices | grep -q . && break; sleep 2; done
[ "$(fastboot getvar is-userspace 2>&1 | awk '/is-userspace/ {print $2}')" = "yes" ] || { echo "fastbootd non atteint" >&2; exit 1; }
fastboot flash system_a "$IMG"
fastboot reboot

deadline=$((SECONDS + BOOT_TIMEOUT_S))
until [ "$(timeout 5 adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    [ $SECONDS -lt $deadline ] || { echo "Pas de boot complet après ${BOOT_TIMEOUT_S} s" >&2; exit 1; }
    timeout 10 adb wait-for-device >/dev/null 2>&1 || true
done
echo "Démarré : $(adb shell getprop ro.build.display.id | tr -d '\r')"
