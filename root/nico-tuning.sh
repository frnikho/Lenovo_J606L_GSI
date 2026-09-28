#!/system/bin/sh
# Réglages de performance Lenovo TB-J606 (bengal) sur GSI — Magisk service.d, exécuté à chaque démarrage.
# Mesures (écran d'accueil, janky frames) : boost QTI seul 10 % → + schedtune top-app 6 % ; noop : lancements à froid −4 %.

# Attendre la fin du démarrage : les scripts vendor (post_boot) réécrivent ces valeurs pendant le boot
until [ "$(getprop sys.boot_completed)" = "1" ]; do sleep 2; done
sleep 20

# Ordonnanceur (WALT + schedtune) : l'appli au premier plan privilégie les cœurs rapides et libres
echo 10 > /dev/stune/top-app/schedtune.boost
echo 1 > /dev/stune/top-app/schedtune.prefer_idle
echo 1 > /dev/stune/foreground/schedtune.prefer_idle

# Stockage UFS : noop plutôt que cfq
for queue in /sys/block/sd*/queue; do
    echo noop > "$queue/scheduler"
done

# zram : lire une seule page à la fois (page-cluster=3 est prévu pour un disque, pas pour de la RAM compressée)
echo 0 > /proc/sys/vm/page-cluster

log -t nico-tuning "réglages appliqués"
