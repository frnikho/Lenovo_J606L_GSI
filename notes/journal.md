# Projet GSI — Lenovo Tab P11 TB-J606L

## Appareil
- Modèle : TB-J606L (LTE), SN <SN>, plateforme `bengal` (Snapdragon 662 / SM6115), 4 Go RAM
- Bootloader déverrouillé, A/B (slot b marqué unbootable), dynamic partitions, Treble, first_api_level 29
- Firmware stock : `TB-J606L_S320435_250303_ROW` (Android 11, dernière version connue)
  - source : https://mirrors-obs-1.lolinet.com/firmware/lenowow/2020/Tab_P11/TB-J606L/
  - copie locale : `~/Downloads/TB-J606L_S320435_250303_ROW/` (paquet QPST complet)
  - images utiles : `stock/S320435/` (+ `SHA256SUMS`)

## super (12 GiB) — reconstruction depuis le paquet QPST
Les fichiers `.x` (rawprogram, contents) du paquet sont chiffrés ; offsets déduits des métadonnées LP de `super_1.img` :

| chunk | partition | secteur de départ (512 o) |
|-------|-----------|---------------------------|
| super_1.img | métadonnées LP | 0 |
| super_2.img | system_a | 2048 |
| super_3.img | system_ext_a | 2967552 |
| super_4.img | product_a | 3805184 |
| super_5.img | vendor_a | 8402944 |

Fichier brut de 12884901888 octets, chunks écrits aux offsets, puis `img2simg` → `stock/S320435/super.img`.

## Historique
- **2026-09-27** — Tablette en bootloop (root raté il y a ~6 mois, fichiers perdus). Réparée :
  flash stock boot/recovery/dtbo/vbmeta/vbmeta_system (insuffisant) → flash super reconstruit → format userdata → démarre.
  Note : le fastboot de la tablette peut se figer après le flash de super → extinction forcée + Vol Bas + Power.
- **2026-09-27** — Stock allégé (39 paquets retirés via `pm uninstall --user 0`, liste : `~/Downloads/tablette-debloat.txt`),
  mode avion + Wi-Fi, animations ×0.5. (Effacé si on flashe un GSI.)
- **2026-09-27** — Étape 0 GSI : téléchargé `gsi/lineage-22.2-20250621-UNOFFICIAL-gsi_arm64_vN.img` (Android 15, AndyYan, « light », vanilla).

## Étape 0 — grille d'évaluation du GSI
Flash 2026-09-27 23:20 : vbmeta_a flags=3, product_a supprimé, system_a = GSI, wipe. Premier boot ~40 s.
Build : Android 15 (SDK 35), patch 2025-06-01, vendor patch 2025-03-05. adb actif d'office (18d1:4ee7).

| Point | Résultat |
|-------|----------|
| Démarre | ✅ boot_completed=1 |
| Wi-Fi | ✅ testé par l'utilisateur |
| Bluetooth | ✅ service actif |
| Luminosité (manuelle / auto) | ✅ testé par l'utilisateur |
| Rotation (accéléromètre) | ✅ testé par l'utilisateur |
| Son (haut-parleurs, Dolby) | ✅ testé par l'utilisateur (pas de Dolby : product_a supprimé) |
| Caméras | ✅ testé par l'utilisateur |
| Tactile / stylet | ✅ testé par l'utilisateur |
| Veille profonde (`dumpsys batterystats`, % perdu en 8 h écran éteint) | ⏳ à faire |
| Charge | ✅ status=2 (en charge) vu par adb |
| Fluidité | ✅ OK selon l'utilisateur |

## Scripts
- `scripts/flash-gsi.sh <system.img>` — vbmeta sans vérification, fastbootd, suppression product_a, flash system_a, wipe
- `scripts/restore-stock.sh` — retour complet au stock S320435

## État au 2026-09-27 fin de soirée
- GSI LineageOS 22.2 en place, tout fonctionne à l'usage (tests manuels OK).
- Build : `generic/lineage_gsi_arm64_vN/lineage_gsi_arm64:15/BP1A.250505.005/eng.crossg:userdebug/test-keys` (userdebug, test-keys)
- ⚠️ Le GSI n'est plus mis à jour depuis juin 2025 → motive l'étape 2 (build maison à jour).

## Prochaines étapes
1. Test veille profonde : `adb shell dumpsys batterystats --reset`, nuit écran éteint débranché, puis
   `adb shell dumpsys batterystats | grep -iE "Discharge|Screen off|Time on battery|Wake lock"`.
2. Étape 1 : personnalisation de l'image (debloat, applis intégrées, props) sans compiler.
3. Étape 2 : build TrebleDroid/LineageOS à jour avec patches J606.

## Étape 1 — personnalisation sans compilation (2026-09-28)
- Outil : `scripts/customize-gsi.py` + `custom/config.json` (debugfs, sans root). Sortie : `out/system-custom.img`.
  - agrandit l'image (+512 Mio), supprime des chemins, modifie des props, ajoute les APK de `custom/apps/`
    (→ `/system/product/app/<Nom>/<Nom>.apk`, uid/gid 0, label `u:object_r:system_file:s0`), fsck, réduit au minimum.
- Build 1 : 9 applis retirées (messaging, Recorder, Twelve, Gallery2, EasterEgg, Stk, SimAppDialog, Updater, QuickAccessWallet),
  `ro.build.display.id=nico-j606-gsi 2026-09-27 (lineage-22.2 base)`.
- Flash sans wipe (même base) via fastbootd : `adb reboot fastboot; fastboot flash system_a out/system-custom.img; fastboot reboot`.
- Résultat : démarre (~29 s), 229 paquets, applis bien absentes, aucun refus SELinux lié aux fichiers modifiés
  (seuls refus : vendor_init qui tente de poser ro.adb.secure / ro.*.build.fingerprint — normal sur un GSI).

## Étape 2 — préparation
- PC : Ryzen 7 8700F (16 threads), 62 Go RAM, docker + podman présents ; repo/ccache absents (on compilera dans un conteneur Ubuntu).
- Besoin disque : ~300 Go (sources + out + ccache). L'utilisateur libère de la place.

## Mesures de fluidité (référence)
- Outil : `scripts/bench.sh <étiquette>` → `notes/bench.csv` (15 allers-retours de défilement dans Paramètres + 3 lancements à froid par appli).
- Référence (GSI AndyYan userdebug + debloat) : 3,9 % d'images en retard, p90 20 ms (> 16,7 ms à 60 Hz → saccades perçues),
  lancements à froid Paramètres 1108 ms / Jelly 729 ms / Etar 650 ms.
- Pistes :
  - HAL QTI `vendor.qti.hardware.perf@2.2-service` tourne mais le framework AOSP ne l'appelle pas (pas de BoostFramework QTI dans le GSI)
    → pas de boost CPU au toucher/lancement. `android.hardware.power-service` (AIDL) présent.
  - schedutil, cpu0-3 max 1,8 GHz, cpu4-7 max 2,0 GHz ; zram 2 Go actif ; écran 60 Hz ; renderer par défaut.
  - Build `user` au lieu de `userdebug`.

## Étape 2 — build maison (2026-09-28)
- Build natif Arch OK : sync 206 Go (2 passes, la 1re avec erreurs réseau en -j16 → relancé en -j8 --retry-fetches=5),
  76 patches AndyYan appliqués (1 par fuzz : « Remove debuggable requirement for signature spoofing »).
- 1er build complet : 2 h 35. Image `out/lineage-22.2-20260928-nico-gsi_arm64_vN-userdebug.img`, correctif sécurité 2026-09-01.
- Flash sans wipe par-dessus le GSI AndyYan : OK (1er démarrage ~105 s, dexopt).
- Patch `patches/frameworks__base/0001-power-send-Qualcomm-perf-HAL-hints…` (voir notes/boost-framework.md) :
  `QtiPerfBooster: connected to vendor.qti.hardware.perf IPerf`, effet visible sur scaling_min_freq.

### Mesures (scripts/quickjank.sh, scripts/launcherjank.sh)
- Paramètres (défilement) : fluide, 0,35 % d'images perdues ; GPU 11-12 ms/image.
  SkiaVK pire (p50 28 ms) → rester en SkiaGL. GPU min 600 MHz : p99 30 → 19 ms (coût batterie, non retenu).
- **Écran d'accueil (Trebuchet) = vraie source de lenteur, limitée CPU (thread UI)** :
  | Config | janky | p50 | p99 |
  |---|---|---|---|
  | sans boost | 56 % | 46 ms | 77 ms |
  | boost 0x1080 type 4 (80 ms) | 45 % | 32 ms | 77 ms |
  | CPU min = max (test manuel) | 8,5 % | 21 ms | 44 ms |
  → boost tactile passé à 0x1083 (animation boost, 2 clusters au max, 400 ms). Build 3 en cours.

### Suite du 2026-09-28
- Boost corrigé : 0x1083 exige type -1 (hint sans Type dans perfboostsconfig). Mesuré : pendant un toucher min cpu0/cpu4 =
  1804/2016 MHz (max), relâché après. **Écran d'accueil : 49 % → 15-17 % d'images en retard, p50 38 → 23 ms.**
- Lancements d'applis ≈ inchangés (~1 s Paramètres) même CPU au max en permanence → pas un problème de fréquence.
  dexopt `speed`/`speed-profile` : gain ~10 %, non retenu.
- `out/system-custom.img` = build maison + debloat (customize-gsi.py), flashé : « nico-j606-gsi 2026-09-28 (lineage-22.2 nico build, qtiboost) ».
- Veille profonde : test impossible branché en USB (0 suspend en 10 min, en charge). Wakelock `dream:dream` = économiseur
  d'écran actif pendant la charge. → test débranché à faire par l'utilisateur.
- Scripts : `flash-system.sh <img>` (maj system_a sans wipe), `quickjank.sh`, `launcherjank.sh`.

## Android 16 (LineageOS 23.2)
- Base : MisterZtr/LineageOS_gsi (branche lineage-23.2, patches TrebleDroid/RestlessOS, « support Android 11 vendor »).
- Étape 0 : GSI prêt `gsi/a16/LineageOS-23.2-20260524-VANILLA-EXT4-GSI.img` (sécurité 2026-05-01) flashé avec wipe :
  la tablette reste stable sur USB (18d1:4ee7) mais **adbd renvoie `error: closed` à toute commande** → état inconnu,
  intervention manuelle demandée (voir l'écran, puis forcer fastboot).
- Sources : `build/src16` (122 Go). 313 patches appliqués, 1 échec sans objet (goodix fingerprint).
  Patch qtiboost appliqué sans conflit. Build : `scripts/build-gsi16.sh` (cible lineage_arm64_bvN4-bp4a-userdebug).
2026-09-28 07:40
- Début test veille A16 MisterZtr prebuilt : 69 %, batterystats reset, 10 h prévues, débranchée
- A16 prebuilt MisterZtr : tests manuels OK (utilisateur), « aussi fluide qu'A15, encore un peu laggy ».
  Mesures sans boost : accueil 18-20 % janky (p50 24 ms), lancements Paramètres 1149 / Jelly 1033 / Etar 773 ms.
  Animations à ×1.0 par défaut → mises à ×0.5 à la main.
- Build A16 maison OK (3 h 21 ; TrebleApp.apk à compiler à part via Gradle, intégré au script).
- Patch 0002 « animations ×0.5 par défaut » (SettingsProvider, global window/transition/animator), versions séparées :
  `patches/a15/`, `patches/a16/` (le contexte diffère entre A15 et A16).
- Images prêtes : `out/system-custom.img` (A15 + boost + anim + debloat), `out/system-custom-a16.img` (A16 idem).
- `scripts/sleep-report.sh <départ%> <heures>` pour le test de veille (utilisateur absent 10 h, départ 69 %, A16 MisterZtr).

## Test de veille profonde — A16 MisterZtr prebuilt (2026-09-28 07:40 → 17:58, débranchée, Wi-Fi allumé)
- **10 h 17 sur batterie, dont seulement 3 min 47 s réveillée (0,6 %)** → la veille profonde fonctionne parfaitement avec le vendor Lenovo.
- Batterie 69 % → 67 % ; 150 mAh consommés = **~15 mAh/h (~0,2 %/h)** écran éteint. Batterie 7700 mAh → plusieurs semaines en veille.
- Réveils : 77 alarmes RTC, 77 réveils Wi-Fi (WLAN_CE_2), jauge batterie (qg). Wakelocks principaux : deviceidle_maint (1 min 30 en 3 fois),
  AnyMotionDetector 10 s. Rien d'anormal.

## Build A16 maison sur la tablette (2026-09-28 soir)
- Flash `out/system-custom-a16.img` + wipe : démarre (correctif 2026-09-01, animations ×0.5 par défaut OK).
- Bug `customize-gsi.py` : les chemins de **fichiers** (pas dossiers) généraient un `rmdir` → script interrompu après les
  suppressions de dossiers (props non modifiées, image non réduite), erreur masquée par un filtre d'affichage. Corrigé
  (`is_dir()` + variable de boucle renommée). phh-remotectl/dbclient désormais bien retirés, display.id OK.
- Reflash **sans wipe** de l'image corrigée → « Can't load Android system » (Rescue Party). Factory reset depuis le
  recovery Lenovo (messages permission denied sans conséquence) → démarre.
  **Règle : après toute modification de l'image système, flasher avec wipe.**
- Après factory reset + redémarrage : `QtiPerfBooster: connected` OK sur A16.
- `adb root` sur ce build A16 → l'USB disparaît (déconnexion, pas de retour) jusqu'au redémarrage de la tablette. Éviter `adb root` en A16.
- `setprop persist.sys.nico.qtiboost` refusé au shell (contexte SELinux system_prop) → bascule on/off seulement en root.
- **Mesures A16 maison (boost + anim ×0.5) : écran d'accueil 10-11 % d'images en retard, p50 18 ms, p99 42 ms**
  (A16 prebuilt MisterZtr anim ×1.0 : 18-20 % / 24 ms / 53-61 ms ; A15 boost : 15-17 % / 23 ms / 57-65 ms).
  Paramètres : 0,4 % ; lancements Paramètres 1039 / Jelly 942 / Etar 707 ms.
- Build `user` A16 : échec `precompiled_sepolicy` (probablement règles TrebleDroid debug interdites en user) — à analyser.

## Root Magisk (2026-09-28 soir)
- Magisk v30.7 officiel (sha256 vérifié) → `root/Magisk-v30.7.apk`. Boot stock S320435 patché dans l'appli :
  `root/magisk_patched-30700_jD2vI.img`, flashé sur boot_a (bootloader). Démarrage OK, `su -v` = 30.7:MAGISKSU.
- Retour arrière : `fastboot flash boot_a /lab/lenovo/stock/S320435/boot.img`.
- Une mise à jour de system_a (GSI) ne touche pas boot_a → le root reste.

## Optimisations avec root (2026-09-28 soir) — écran d'accueil, janky frames
| Étape | janky | p50 | p99 |
|---|---|---|---|
| A16 nico (boost QTI) | 10,2 % | 12 ms | 48 ms |
| boost désactivé (référence) | 13,8 % | 13-16 ms | 57 ms |
| + schedtune top-app boost=10, prefer_idle=1 | 6,2-6,9 % | 9-10 ms | 32 ms |
| + prefer_idle foreground | 5,7-6,6 % | 9 ms | 32 ms |
| **final après reboot (+ noop, page-cluster 0, 6 paquets désactivés)** | **1,9-2,8 %** | 10-11 ms | **26 ms** |
Sans effet (non retenus) : schedtune boost=20, colocate=1, sched_upmigrate 70/60.
noop vs cfq : lancement à froid Paramètres 1530 → 1450 ms. zram : lzo seul dispo (pas de lz4).
- Paquets désactivés (`notes/disabled-packages.txt`, `pm enable` pour revenir) : QMS ConnectionSecurity + SSGTelemetry (Qualcomm, 23 Mo chacun),
  Seedvault, CellBroadcast, Dialer, EmergencyInfo. Reboot OK sans crash, MemAvailable 1,66 → 1,9 Go.
- Réglages persistants : `/data/adb/service.d/nico-tuning.sh` (copie : `root/nico-tuning.sh`), log `nico-tuning`.
- Note : la passe finale a rendu moins d'images (~500 vs ~800) → comparaison indicative, tendance nette.

## GSI GApps pour Android Auto (2026-09-30)
- Objectif : Android Auto (filaire) depuis la tablette → vrais Google Play Services nécessaires (GmsCore/microG insuffisant).
- `vendor/gapps` (MindTheGapps) déjà présent dans `build/src16` → cible `lineage_arm64_bgN4` :
  `scripts/build-gsi16.sh userdebug gapps` (52 min avec ccache). Image 3,3 Go, personnalisée 3 201 Mio
  (`custom/config-a16-gapps.json` → `out/system-custom-a16-gapps.img`).
- super : groupe `qti_dynamic_partitions_a` max 6 140 Mio ; system 3 201 + system_ext 409 + vendor 664 = 4 274 Mio → OK.
- Flash avec wipe, premier démarrage OK. Applis réinstallées via `scripts/tablet-apps.sh restore` (APK + splits sauvegardés
  avant le flash par `backup` dans `apks/tablet/`).
- **Incident** : après la 1re session (applis + « installation supplémentaire » Magisk + redémarrage), écran « Can't load
  Android system » en boucle, adb jamais autorisé (→ /data probablement non monté). `persist.sys.disable_rescue=true` :
  ce n'est donc pas Rescue Party. Factory reset depuis le recovery → démarre.
- Reproduction pas à pas après le reset, journaux capturés à chaque démarrage : reboot à nu OK, reboot avec les 12 applis
  OK, Magisk (installation supplémentaire + reboot) OK, reboot avec `nico-tuning.sh` OK. **Cause non reproduite.**
  Si ça revient : récupérer `/sys/fs/pstore` (root) au démarrage suivant avant tout reset.
- Après un reset : penser à redésactiver `notes/disabled-packages.txt` et à remettre `nico-tuning.sh` dans service.d.
- Double tap pour réveiller : smart wake du tactile Himax (`/proc/android_touch/gesture_control` + `GESTURE`, geste 0),
  activé dans `nico-tuning.sh`. Réveille l'écran, l'écran de verrouillage reste (PIN).
- **Android Auto validé sans voiture** avec le Desktop Head Unit (`scripts/dhu.sh`, SDK `extras;google;auto` r02.0) :
  projection OK (Maps, YouTube Music, téléphone). Android Auto 17.7 (le paquet MindTheGapps n'est qu'un stub 1.2 →
  mise à jour Play Store). Premier lancement : téléchargement Maps/Appli Google/synthèse vocale, puis accès aux
  notifications requis (`cmd notification allow_listener ...SharedNotificationListenerManager$ListenerService`).
  DHU sur ce PC : libc++ du NDK (LD_LIBRARY_PATH) et config ALSA sans JACK (sinon SIGSEGV dans jack_client_open).
