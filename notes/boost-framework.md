# Boosts CPU Qualcomm sur le GSI (J606 / bengal)

## Constat (2026-09-28)
- Vendor Lenovo : `android.hardware.power-service` = « QTI PowerHAL » (AIDL V1), lié à `libqti-perfd-client.so`
  (`perf_hint`, `perf_lock_acq`, `perf_lock_rel`).
- Au démarrage, le framework interroge `isBoostSupported(0=INTERACTION, 1=DISPLAY_UPDATE_IMMINENT)` et
  `isModeSupported(5=LAUNCH, 9, 15, 16, 17)`. En usage, seul `setMode(7=INTERACTIVE)` passe :
  **INTERACTION et LAUNCH sont déclarés non supportés** → aucun boost au toucher ni au lancement d'appli.
- Sur le stock, c'est le BoostFramework QTI (côté /system, absent du GSI) qui appelle directement
  `vendor.qti.hardware.perf@2.2::IPerf/default` (`vendor.qti.hardware.perf@2.2-service`, qui tourne).
- SELinux vendor : `system_server` fait partie de `vendor_hal_perf_client` → il a le droit d'appeler IPerf.
- Interface : `LineageOS/android_vendor_qcom_opensource_interfaces` (`perf/2.0..2.3`, `gen_java: true`,
  `system_ext_specific`). Copie : `build/tools/qcom-interfaces/perf/`.
  `perfHint(uint32 hint, string userDataStr, int32 userData1, int32 userData2, vec<int32> reserved) generates (int32)`
  (userData1 = durée ms, userData2 = type ; convention du BoostFramework QTI).

## Hints définis pour bengal (`/vendor/etc/perf/perfboostsconfig.xml`)
| Hint | Type | Durée | Usage |
|------|------|-------|-------|
| 0x1081 | 1 | 2000 ms | lancement d'appli (principal) |
| 0x1081 | 2 | 1500 ms | lancement, packing désactivé |
| 0x1081 | 3/4/6 | 15 s / 15 s / 2 s | lancement lourd / jeux |
| 0x1083 | – | 400 ms | boost d'animation |
| 0x1080 | 1 / 2 | durée de l'appelant | défilement vertical / horizontal |
| 0x1080 | 4 | 80 ms | pre-fling (toucher) |
| 0x1087 | 1 | 0 (appelant) | drag |
| 0x1089 | – | 1500 ms | latence de rotation |
| 0x1090 | – | 1000 ms | animation de rotation |
| 0x1086 / 0x1088 | – | – | MTP / installation de paquet |

## Conception du patch `frameworks/base` (à écrire quand les sources sont synchronisées)
- Nouvelle classe `services/core/.../power/QtiPerfBooster.java` : récupère `IPerf` (HIDL Java, `V2_0`) en paresseux,
  gère la mort du service (`linkToDeath`), ne fait rien si le HAL est absent (GSI utilisable sur un autre appareil).
- Branchements dans `PowerManagerService` :
  - `setPowerModeInternal(Mode.LAUNCH, true)` → `perfHint(0x1081, pkg, 2000, 1)` (même si le HAL AIDL dit non supporté) ;
  - activité utilisateur de type TOUCH (déjà limitée à ~100 ms par le framework) → `perfHint(0x1080, "", 80, 4)` ;
  - option : rotation → 0x1089.
- Propriété d'activation `persist.sys.nico.qtiboost` (défaut 1) pour comparer avec / sans via `scripts/bench.sh`.
- Build : `static_libs: ["vendor.qti.hardware.perf-V2.0-java"]` dans `services.core` + projet
  `vendor/qcom/opensource/interfaces` ajouté au local manifest si absent de l'arbre GSI.
