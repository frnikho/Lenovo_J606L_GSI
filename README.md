# GSI LineageOS personnalisé pour Lenovo Tab P11 (TB-J606L)

Build maison d'Android 16 (LineageOS 23.2, GSI TrebleDroid) optimisé pour la **Lenovo Tab P11 TB-J606L**
(Snapdragon 662 / `bengal`, 4 Go de RAM, vendor Lenovo Android 11), plus les outils pour le construire,
le personnaliser, le flasher, le mesurer et revenir au firmware d'origine.

## Résultat

| | Écran d'accueil : images en retard | Veille (débranchée) |
|---|---|---|
| GSI LineageOS 22.2 d'origine (Android 15) | 49 % | – |
| GSI LineageOS 23.2 MisterZtr (Android 16) | 18-20 % | ~0,2 %/h |
| **Ce build + réglages root** | **~2-3 %** | ~0,2 %/h |

Détails et méthode : [`notes/journal.md`](notes/journal.md), [`notes/bench.csv`](notes/bench.csv).

## Ce qui change par rapport à un GSI standard

- **Boost CPU Qualcomm** ([`patches/a16/frameworks__base/0001`](patches/a16/frameworks__base)) : le HAL power de Lenovo
  déclare les boosts « toucher » et « lancement » non supportés, donc un GSI ne boost jamais. Le patch appelle
  directement `vendor.qti.hardware.perf` IPerf depuis `system_server` avec les hints du vendor
  (`0x1083` animation au toucher, `0x1081` au lancement). Voir [`notes/boost-framework.md`](notes/boost-framework.md).
  Désactivable : `setprop persist.sys.nico.qtiboost false` (root).
- **Animations ×0,5 par défaut** (patch `0002`, SettingsProvider).
- **Débogage distant phh retiré** (`patches/a16/device__phh__treble`) : tunnel SSH inverse exposant adb.
- **Applis inutiles retirées** de l'image ([`custom/config-a16.json`](custom/config-a16.json)) : Messages, SIM Toolkit,
  impression, fonds animés, alertes d'urgence mobiles…
- **Réglages root au démarrage** ([`root/nico-tuning.sh`](root/nico-tuning.sh), Magisk `service.d`) :
  `schedtune` top-app boost 10 + `prefer_idle`, I/O `noop`, zram `page-cluster=0`.
- **Paquets désactivés** ([`notes/disabled-packages.txt`](notes/disabled-packages.txt)) : télémétrie Qualcomm QMS,
  Seedvault, Téléphone… (`pm enable <paquet>` pour revenir).

## Arborescence

```
scripts/     build, personnalisation, flash, restauration, mesures
patches/     patches maison par version (a15/, a16/), un dossier par projet (chemin avec / → __)
custom/      configs de customize-gsi.py (chemins à retirer, propriétés, APK à intégrer dans custom/apps/)
root/        script de réglages Magisk
notes/       journal du projet, mesures, analyses
car/         infos OBD2 de la voiture (projet abandonné)
```

Non versionnés (voir `.gitignore`) : `build/` (sources ~120 Go + sortie ~100 Go), `out/`, `stock/`, images et APK.

## Reconstruire

Prérequis (Arch Linux) : `repo`, `git-lfs`, `gperf`, `ccache`, `ncurses5-compat-libs`, Java 17, ~250 Go libres.

```bash
# Sources LineageOS 23.2 + manifeste et patches TrebleDroid de MisterZtr
mkdir -p build/src16 && cd build/src16
repo init -u https://github.com/LineageOS/android.git -b lineage-23.2 --git-lfs --depth=1
git clone https://github.com/MisterZtr/treble_manifest.git .repo/local_manifests -b lineage-23.2
repo sync -c --force-sync --optimized-fetch --no-tags --no-clone-bundle -j8 --retry-fetches=5
bash LineageOS_gsi/patches/apply-patches.sh .

# Patches maison
for d in ../../patches/a16/*/; do
  git -C "$(basename "$d" | sed 's#__#/#g')" am --3way "$d"*.patch
done
cd ../..

# Compilation (~3 h la 1re fois, quelques minutes ensuite) puis personnalisation
scripts/build-gsi16.sh userdebug
scripts/customize-gsi.py custom/config-a16.json      # -> out/system-custom-a16.img
```

## Flasher

Bootloader déverrouillé requis. **Effacer les données après toute modification de l'image système**
(une mise à jour sans effacement a provoqué un « Can't load Android system »).

```bash
scripts/flash-gsi.sh out/system-custom-a16.img   # 1re fois : vbmeta sans vérification, product_a supprimé, wipe
# ensuite : adb reboot fastboot ; fastboot flash system_a out/system-custom-a16.img ; fastboot -w ; fastboot reboot
```

Root : patcher le `boot.img` **de la version exacte du firmware** (S320435) avec Magisk, puis
`fastboot flash boot_a magisk_patched.img`.

## Revenir au firmware Lenovo

Firmware officiel `TB-J606L_S320435_250303_ROW` (miroir : lolinet). Le paquet QPST découpe `super` en morceaux ;
`super.img` se reconstruit avec les offsets des métadonnées LP (détails dans `notes/journal.md`), puis :

```bash
scripts/restore-stock.sh   # boot, recovery, dtbo, vbmeta, super + wipe
```

## Mesurer

```bash
scripts/launcherjank.sh           # fluidité de l'écran d'accueil (gfxinfo)
scripts/bench.sh "<étiquette>"    # défilement + lancements à froid -> notes/bench.csv
scripts/sleep-report.sh 69 10     # veille profonde après un test débranché (niveau de départ, heures)
```

## Crédits

[LineageOS](https://lineageos.org), [TrebleDroid](https://github.com/TrebleDroid) (phhusson),
[MisterZtr/LineageOS_gsi](https://github.com/MisterZtr/LineageOS_gsi),
[AndyCGYan](https://github.com/AndyCGYan/lineage_build_unified), [Magisk](https://github.com/topjohnwu/Magisk).
Les patches modifient du code AOSP sous licence Apache 2.0.
