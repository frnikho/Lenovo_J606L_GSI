#!/usr/bin/env bash
# Overview : construction + installation sur la tablette.
#   overview-install.sh module   -> 1re fois : module Magisk (priv-app + permissions), puis redémarrage
#   overview-install.sh update   -> ensuite : adb install -r (l'appli reste privilégiée, même clé debug)
set -euo pipefail

PROJECT=/lab/lenovo/app/overview
APK="$PROJECT/app/build/outputs/apk/debug/app-debug.apk"
OUT=/lab/lenovo/out
PKG=dev.nico.overview

adb get-state >/dev/null 2>&1 || { echo "Tablette non détectée par adb" >&2; exit 1; }
(cd "$PROJECT" && ./gradlew -q :app:assembleDebug)

grant_runtime() {
    adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION
    adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
}

case "${1:-}" in
    module)
        stage=$(mktemp -d)
        trap 'rm -rf "$stage"' EXIT
        mkdir -p "$stage/system/priv-app/Overview" "$stage/system/etc/permissions"
        cp "$PROJECT/magisk/module.prop" "$stage/"
        cp "$APK" "$stage/system/priv-app/Overview/Overview.apk"
        cp "$PROJECT/magisk/privapp-permissions-$PKG.xml" "$stage/system/etc/permissions/"
        mkdir -p "$OUT"
        (cd "$stage" && zip -qr "$OUT/overview-module.zip" .)
        adb push "$OUT/overview-module.zip" /data/local/tmp/overview-module.zip >/dev/null
        adb shell su -c "'magisk --install-module /data/local/tmp/overview-module.zip'"
        echo "Module installé : redémarrer la tablette, puis lancer « $0 grant »."
        ;;
    grant)
        grant_runtime
        adb shell am start -n "$PKG/.DashboardActivity"
        ;;
    update)
        adb install -r "$APK"
        grant_runtime
        adb shell am start -n "$PKG/.DashboardActivity"
        ;;
    *)
        echo "Usage : $0 module|grant|update" >&2
        exit 1
        ;;
esac
