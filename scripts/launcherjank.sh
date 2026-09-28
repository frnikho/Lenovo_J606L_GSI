#!/usr/bin/env bash
# Mesure de fluidité de l'écran d'accueil : tiroir d'applis + pages, 6 cycles.
adb shell '
input keyevent KEYCODE_WAKEUP; input keyevent KEYCODE_HOME; sleep 1
P=com.android.launcher3
dumpsys gfxinfo $P reset >/dev/null
for i in 1 2 3 4 5 6; do input swipe 600 1800 600 500 200; sleep 0.8; input keyevent KEYCODE_HOME; sleep 0.8; input swipe 1000 1000 200 1000 200; sleep 0.6; input swipe 200 1000 1000 1000 200; sleep 0.6; done
dumpsys gfxinfo $P | grep -E "^Janky frames:|^(50|90|99)th percentile|Slow UI" | tr "\n" " "; echo'
