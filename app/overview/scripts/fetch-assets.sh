#!/usr/bin/env bash
# Icônes Material Symbols (Rounded) et police Inter (OFL) dans les ressources de l'appli.
set -euo pipefail

RES="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/res"
BASE=https://raw.githubusercontent.com/google/material-design-icons/master/symbols/android
INTER=https://github.com/rsms/inter/releases/download/v4.1/Inter-4.1.zip
mkdir -p "$RES/drawable" "$RES/font"

icon() { # nom [variante]
    local name=$1 variant=${2:-}
    local file="${name}${variant:+_$variant}_24px.xml"
    # android:tint pointe vers un attribut de thème AppCompat absent ici
    curl -fsSL "$BASE/$name/materialsymbolsrounded/$file" \
        | sed 's/ *android:tint="[^"]*"//' > "$RES/drawable/ic_${name}.xml"
}

for n in sunny clear_night partly_cloudy_day partly_cloudy_night cloud foggy rainy_light rainy weather_snowy \
         thunderstorm local_gas_station home work star settings add delete music_note wifi_off; do
    icon "$n"
done
for n in play_arrow pause skip_next skip_previous; do
    icon "$n" fill1
done

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
curl -fsSL -o "$tmp/inter.zip" "$INTER"
for weight in Light Regular Medium SemiBold; do
    unzip -p "$tmp/inter.zip" "extras/ttf/Inter-$weight.ttf" > "$RES/font/inter_${weight,,}.ttf"
done
