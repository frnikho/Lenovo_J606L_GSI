#!/usr/bin/env bash
# Rapport de veille après un test débranché (à lancer dès la reconnexion USB, avant d'utiliser la tablette).
# Usage : sleep-report.sh <niveau_depart_%> <heures>
set -euo pipefail
START="${1:?niveau de départ en %}"
HOURS="${2:?durée en heures}"
dump=$(adb shell dumpsys batterystats | tr -d '\r')
level=$(adb shell dumpsys battery | awk '/level:/ {print $2}' | tr -d '\r')
echo "Batterie : ${START} % -> ${level} % en ${HOURS} h => $(echo "scale=2; ($START-$level)/$HOURS" | bc) %/h"
echo "$dump" | grep -E "Time on battery:|Time on battery screen off:|Screen off discharge|Total run time|Start clock time" | head -6
echo "--- Deep sleep : realtime vs uptime écran éteint (uptime faible = veille profonde OK)"
echo "$dump" | grep -m1 "Time on battery screen off:"
echo "--- Top wakelocks noyau"
echo "$dump" | grep -A8 "All kernel wake locks:" | tail -8
echo "--- Top wakelocks partiels"
echo "$dump" | grep -A8 "All partial wake locks:" | tail -8
echo "--- Réveils (wakeup reasons)"
echo "$dump" | grep -A8 "All wakeup reasons:" | tail -8
