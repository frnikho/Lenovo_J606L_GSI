#!/usr/bin/env bash
# Émulateur d'écran de voiture Android Auto (Desktop Head Unit) sur le PC, connecté à la tablette via adb.
# Prérequis tablette : Android Auto à jour, mode développeur Android Auto activé (10 appuis sur la version),
#   puis menu ⋮ → « Démarrer le serveur de l'unité principale ».
# Le serveur n'accepte qu'une connexion : après chaque arrêt de DHU, l'arrêter puis le redémarrer dans Android Auto.
# DHU lit des commandes sur stdin et quitte sur EOF : le lancer depuis un terminal (ou `tail -f /dev/null | dhu.sh`).
# DHU : SDK extras;google;auto (r02.0) dans ~/Android/Sdk/extras/google/auto. Il lui faut libc++ :
#   on prend celle du NDK plutôt que d'installer le paquet système.
set -euo pipefail

SDK="${ANDROID_HOME:-$HOME/Android/Sdk}"
DHU="$SDK/extras/google/auto/desktop-head-unit"
LIBCXX="$(dirname "$(find "$SDK/ndk" -path '*prebuilt/linux-x86_64/lib/libc++.so.1' | sort | tail -1)")"

[ -x "$DHU" ] || { echo "DHU absent : $DHU" >&2; exit 1; }
adb get-state >/dev/null 2>&1 || { echo "Tablette non détectée par adb" >&2; exit 1; }

# Config ALSA réduite à PipeWire : en énumérant les sorties audio, DHU ouvre le module JACK de PipeWire
# (50-jack.conf) et plante (SIGSEGV dans jack_client_open).
ALSA_CONF="${XDG_RUNTIME_DIR:-/tmp}/dhu-alsa.conf"
awk '
    /^@hooks \[/ { in_hooks = 1 }
    in_hooks && /files \[/ {
        print; print "\t\t\t\"/usr/share/alsa/alsa.conf.d/50-pipewire.conf\""
        print "\t\t\t\"/usr/share/alsa/alsa.conf.d/99-pipewire-default.conf\""
        skip = 1; next
    }
    skip && /^\t\t\]/ { skip = 0; in_hooks = 0 }
    !skip
' /usr/share/alsa/alsa.conf > "$ALSA_CONF"

adb forward tcp:5277 tcp:5277
ALSA_CONFIG_PATH="$ALSA_CONF" LD_LIBRARY_PATH="$LIBCXX" exec "$DHU" --adb "$@"
