#!/usr/bin/env bash
# Mesure rapide : 10 allers-retours dans Paramètres, affiche p50/p90/p99 et GPU p50/p90.
adb shell '
input keyevent KEYCODE_WAKEUP
am force-stop com.android.settings; am start -W -n com.android.settings/.Settings >/dev/null; sleep 2
dumpsys gfxinfo com.android.settings reset >/dev/null
for i in 1 2 3 4 5 6 7 8 9 10; do input swipe 600 1600 600 400 150; input swipe 600 400 600 1600 150; done; sleep 1
dumpsys gfxinfo com.android.settings | grep -E "^Janky frames:|^(50|90|99)th percentile|^(50|90)th gpu percentile|Pipeline" | tr "\n" " "; echo'
