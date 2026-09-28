# Revue des services — GSI A16 (LineageOS 23.2 nico) pour tablette de cuisine Wi-Fi, sans SIM

Analyse hors ligne de l'image (`/system/etc/init/*.rc`, dossiers app/priv-app) le 2026-09-28.
99 services init, 44 démarrés automatiquement : quasiment tous indispensables (zygote, surfaceflinger, netd, vold,
keystore2, audioserver, lmkd…). Les gains sont côté applis/services Java et zygote 32 bits.

## Fait
| Élément | Action | Raison |
|---|---|---|
| `phh-remotectl` + `dbclient` | retiré des sources (`patches/a16/device__phh__treble`) | tunnel SSH inverse exposant adb vers android-remote.phh.me si `persist.sys.phh.remote=true` (non démarré par défaut, mais aucune raison de l'embarquer) |
| messaging, Recorder, Twelve, Gallery2, EasterEgg, Stk, SimAppDialog, MtkInCallService, QuickAccessWallet | retirés (customize) | inutiles sans SIM / doublons (Glimpse = galerie) / MediaTek sur Qualcomm |

## Retrait sans risque (ajouté à `custom/config-a16.json`)
| Élément | Raison |
|---|---|
| BuiltInPrintService, PrintRecommendationService | impression (PrintSpooler gardé : le framework s'y lie) |
| BookmarkProvider, PartnerBookmarksProvider | favoris du vieux navigateur AOSP, inutilisés |
| LiveWallpapersPicker | fonds animés (coûteux en batterie) |
| CellBroadcastLegacyApp | alertes d'urgence via réseau mobile : pas de SIM |
| Camelot | lecteur PDF LineageOS (réinstallable) |

## À tester à chaud avant retrait (`pm disable-user --user 0 <pkg>`, réversible)
| Paquet | Raison | Risque |
|---|---|---|
| Dialer (`com.android.dialer`), EmergencyInfo | pas de SIM | Telecom attend une appli téléphone par défaut |
| Iwlan, QualifiedNetworksService, ImsServiceEntitlement | appels Wi-Fi / IMS : pas de SIM | liés à la pile téléphonie |
| Seedvault | sauvegarde (désactivée par défaut) | aucun si non configurée |

## Expérience mémoire
- `zygote_secondary` (zygote 32 bits) : ~40-60 Mo de RAM préchargés pour d'éventuelles applis 32 bits.
  Passer en 64 bits seul (`ro.zygote=zygote64`, abilist32 vide) libérerait cette RAM sur 3,7 Go.
  Risque : applis 32 bits uniquement (rares en 2026). À mesurer (`MemAvailable`, lancements) avant d'adopter.

## Mesures à faire quand la tablette est rebranchée
- `dumpsys procstats --hours 3` et `dumpsys meminfo` : processus les plus gourmands au repos.
- `dumpsys batterystats` : applis qui réveillent l'appareil (alarms, jobs, wakelocks).
- `getprop | grep init.svc` : services réellement lancés (y compris vendor).
