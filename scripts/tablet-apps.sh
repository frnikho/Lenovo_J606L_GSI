#!/usr/bin/env bash
# Sauvegarde / réinstallation des applis utilisateur de la tablette via adb (APK + splits).
# Les applis ne sont pas intégrées à l'image système : elles restent mises à jour par Aurora/Play
# et désinstallables. Les données des applis ne sont pas sauvegardées (flash-gsi.sh efface /data).
# Usage : tablet-apps.sh backup   -> copie les APK de la tablette dans apks/tablet/<paquet>/
#         tablet-apps.sh restore  -> réinstalle tout apks/tablet/ + script de tuning root (service.d)
set -euo pipefail

APPS_DIR=/lab/lenovo/apks/tablet
TUNING=/lab/lenovo/root/nico-tuning.sh
# GmsCore ReVanced d'abord : YouTube/Music ReVanced en dépendent
FIRST=(com.topjohnwu.magisk app.revanced.android.gms)

adb get-state >/dev/null 2>&1 || { echo "Tablette non détectée par adb" >&2; exit 1; }

backup() {
    mkdir -p "$APPS_DIR"
    # mapfile plutôt que `| while read` : adb lit stdin et viderait la liste
    local pkgs apks
    mapfile -t pkgs < <(adb shell pm list packages -3 | sed 's/^package://' | tr -d '\r')
    for pkg in "${pkgs[@]}"; do
        rm -rf "${APPS_DIR:?}/$pkg"
        mkdir -p "$APPS_DIR/$pkg"
        mapfile -t apks < <(adb shell pm path "$pkg" | sed 's/^package://' | tr -d '\r')
        for apk in "${apks[@]}"; do
            adb pull "$apk" "$APPS_DIR/$pkg/" >/dev/null
        done
        echo "$pkg : ${#apks[@]} APK"
    done
}

install_pkg() {
    local dir="$1"
    local apks=("$dir"/*.apk)
    [ -e "${apks[0]}" ] || { echo "$(basename "$dir") : aucun APK" >&2; return 1; }
    if adb install-multiple -r "${apks[@]}" >/dev/null; then
        echo "$(basename "$dir") : OK"
    else
        echo "$(basename "$dir") : ÉCHEC" >&2
        return 1
    fi
}

restore() {
    [ -d "$APPS_DIR" ] || { echo "Pas de sauvegarde dans $APPS_DIR (lancer backup avant le flash)" >&2; exit 1; }
    local failed=0
    for pkg in "${FIRST[@]}"; do
        [ -d "$APPS_DIR/$pkg" ] && { install_pkg "$APPS_DIR/$pkg" || failed=1; }
    done
    for dir in "$APPS_DIR"/*/; do
        pkg=$(basename "$dir")
        [[ " ${FIRST[*]} " == *" $pkg "* ]] && continue
        install_pkg "$dir" || failed=1
    done
    # Tuning root au boot : nécessite que Magisk ait fini son installation (ouvrir l'appli Magisk une fois)
    if adb shell su -c true 2>/dev/null; then
        adb push "$TUNING" /data/local/tmp/nico-tuning.sh >/dev/null
        adb shell su -c "'mkdir -p /data/adb/service.d && cp /data/local/tmp/nico-tuning.sh /data/adb/service.d/ && chmod 755 /data/adb/service.d/nico-tuning.sh'"
        echo "nico-tuning.sh : installé dans service.d"
    else
        echo "Root indisponible : ouvrir Magisk, finir l'installation, puis relancer restore" >&2
        failed=1
    fi
    return "$failed"
}

case "${1:-}" in
    backup) backup ;;
    restore) restore ;;
    *) echo "Usage : $0 backup|restore" >&2; exit 1 ;;
esac
