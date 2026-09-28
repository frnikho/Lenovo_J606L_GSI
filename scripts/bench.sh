#!/usr/bin/env bash
# Mesure de fluidité reproductible sur la tablette (écran allumé et déverrouillé).
# Usage : bench.sh [étiquette]  -> ajoute une ligne à /lab/lenovo/notes/bench.csv
set -euo pipefail

LABEL="${1:-sans-nom}"
CSV=/lab/lenovo/notes/bench.csv
SWIPES=15
LAUNCH_RUNS=3
SETTINGS=com.android.settings/.Settings
APPS=("$SETTINGS" "org.lineageos.jelly/.MainActivity" "org.lineageos.etar/com.android.calendar.AllInOneActivity")

adb shell input keyevent KEYCODE_WAKEUP
adb shell svc power stayon true

cold_launch_ms() {
    local component="$1" total=0 run ms
    for run in $(seq "$LAUNCH_RUNS"); do
        adb shell am force-stop "${component%%/*}"
        sleep 1
        ms=$(adb shell am start -W -n "$component" | awk '/TotalTime/ {print $2}' | tr -d '\r')
        total=$((total + ms))
        adb shell input keyevent KEYCODE_HOME
        sleep 1
    done
    echo $((total / LAUNCH_RUNS))
}

scroll_jank() {
    adb shell am start -W -n "$SETTINGS" >/dev/null
    sleep 2
    adb shell dumpsys gfxinfo "${SETTINGS%%/*}" reset >/dev/null
    for _ in $(seq "$SWIPES"); do
        adb shell input swipe 600 1600 600 400 150
        adb shell input swipe 600 400 600 1600 150
    done
    sleep 1
    adb shell dumpsys gfxinfo "${SETTINGS%%/*}" | tr -d '\r' | awk '
        /Total frames rendered/ {t=$4}
        /^Janky frames:/ {j=$3; p=$4}
        /^90th percentile/ {p90=$3}
        /^99th percentile/ {p99=$3}
        END {gsub(/[()%]/,"",p); print t","j","p","p90","p99}'
}

[ -f "$CSV" ] || echo "date,etiquette,frames,janky,janky_pct,p90,p99,launch_settings_ms,launch_jelly_ms,launch_etar_ms" > "$CSV"
jank=$(scroll_jank)
launches=()
for app in "${APPS[@]}"; do launches+=("$(cold_launch_ms "$app")"); done
adb shell svc power stayon false

line="$(date -Iseconds),$LABEL,$jank,$(IFS=,; echo "${launches[*]}")"
echo "$line" >> "$CSV"
column -s, -t "$CSV"
