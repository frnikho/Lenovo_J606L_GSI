# Overview — tableau de bord voiture sur la tablette (partie 1)

Date : 2026-09-30 · Appareil : Lenovo Tab P11 TB-J606L, GSI LineageOS 23.2 GApps (`bgN4`), root Magisk.

## Contexte

La tablette est fixée dans la voiture, en paysage, et branchée en USB à l'autoradio : elle fait tourner Android Auto
(projection sur l'écran de la voiture). Son propre écran reste libre : l'overview l'occupe pendant la conduite et
regroupe les infos utiles avec des accès rapides. Internet vient du point d'accès Wi-Fi du Pixel 8a (`Pixel_9798`),
qui garde la SIM et les appels (Bluetooth direct avec la voiture).

## Découpage du projet

| Partie | Contenu | Statut |
|---|---|---|
| **1. Overview (ce document)** | mode conduite, musique, météo, GPL, lieux → Waze, veille sombre, thème auto | à faire |
| 2. Relais Pixel | notifications et appels entrants du Pixel vers la tablette (réseau local du point d'accès) | plus tard |
| 3. Voiture | ESP32 + bus CAN → tablette | plus tard |

Hors périmètre : assistant vocal IA (abandonné), remplacement du lanceur (le lanceur de base reste l'écran d'accueil),
recherche d'adresse dans l'overview, stations « sur le trajet ».

## Choix techniques

- Kotlin + Jetpack Compose, Coroutines/Flow, `minSdk`/`targetSdk` 36 (Android 16 uniquement).
- Projet Gradle dans `app/overview/` de ce dépôt. Paquet : `dev.nico.overview`.
- Installation en **appli système privilégiée via un module Magisk** (`/system/priv-app/Overview` + fichier
  `privapp-permissions`) : pas de rebuild de la ROM. Mises à jour par `adb install -r` (l'appli garde ses droits).
  Intégration dans l'image (`custom/apps`) seulement quand l'appli sera stable.
- Actions privilégiées par les API Android quand la permission est accordable à une priv-app ; sinon repli sur le
  root (`su -c cmd …`). La liste exacte est vérifiée pendant le plan (voir « Points à vérifier »).
- Pas de serveur : l'appli interroge directement Open-Meteo et le flux public des prix des carburants.
- Stockage local (DataStore) : réglages, lieux, dernières données reçues, état d'avant le mode conduite.

## Architecture

```
app/overview/
├─ drivemode/   détection Android Auto, machine à états, actions (Wi-Fi, profil, écran, premier plan)
├─ ui/          grille bento Compose, thème clair/sombre, composants animés, veille sombre
├─ tiles/
│   ├─ media/     lecteur en cours (MediaSession)
│   ├─ weather/   Open-Meteo
│   ├─ fuel/      prix des carburants, GPL, 20 km, 3 moins chères
│   └─ places/    favoris → Waze
├─ location/    position partagée (météo, carburant, thème)
├─ theme/       jour/nuit : soleil + capteur de luminosité + réglage manuel
├─ settings/    lieux, SSID du point d'accès, thème
└─ magisk/      module d'installation priv-app
```

Chaque tuile a sa source de données, son état et son affichage, sans dépendre des autres. L'interface ne fait
qu'afficher des états exposés en `StateFlow`. Les services système (Wi-Fi, profils, écran, capteurs, position) sont
derrière des interfaces pour pouvoir être remplacés par des faux dans les tests.

## Mode conduite

**Détection** : un service démarré au boot (priv-app persistante) observe `CarConnection` (`androidx.car.app`) ;
`CONNECTION_TYPE_PROJECTION` = en voiture. Repli si peu fiable : état USB en mode accessoire (Android Open Accessory).

**Entrée**, dans l'ordre :
1. Sauvegarde de l'état courant (profil actif, réseaux dont la connexion auto sera coupée) sur le stockage.
2. Profil LineageOS « Automobile » (écran de verrouillage désactivé).
3. Wi-Fi : connexion automatique coupée pour les réseaux enregistrés autres que le point d'accès du Pixel
   (réglable, défaut `Pixel_9798`), puis connexion à celui-ci.
4. Overview au premier plan, écran allumé et maintenu allumé tant qu'elle est affichée.

**Pendant la conduite** :
- Veille sombre après 30 s sans toucher : luminosité de la fenêtre au minimum (le réglage système n'est pas modifié),
  vue épurée (heure en grand, température, titre en cours). Un toucher rétablit la vue complète.
- Appui sur un lieu ou une station → Waze, puis retour de l'overview au premier plan ~2 s après (la navigation
  continue sur l'écran de la voiture).

**Sortie** : Android Auto déconnecté depuis plus de 10 s (un faux contact plus court est ignoré). Restauration de la
connexion auto des réseaux, profil « Par défaut », fermeture de l'overview, extinction normale de l'écran.

**Robustesse** : si l'appli plante ou si la tablette redémarre en mode conduite, l'état sauvegardé est restauré au
démarrage suivant (hors connexion Android Auto active). Point d'accès du Pixel introuvable → la tablette reste sur le
réseau courant, un indicateur discret le signale, le reste du mode conduite continue.

**Migration** : les déclencheurs Wi-Fi du profil « Automobile » configurés à la main sont à supprimer une fois
l'overview installée (sinon les deux mécanismes se contredisent).

## Tuiles et données

Disposition paysage 2000×1200, grille bento :

```
┌──────────────────────┬───────────────┬──────────────┐
│                      │ 14:32         │ GPL          │
│   MUSIQUE            │ ☀ 18° · 12/21 │ 0,98 € 3,2km │
│   pochette, titre    │ pluie 16h     │ 1,01 € 7,8km │
│   ⏮  ⏯  ⏭           │               │ 1,03 € 15km  │
├──────────────────────┴───────────────┴──────────────┤
│  🏠 Maison    💼 Travail    ⭐ Favori 3   ⭐ Favori 4 │
└─────────────────────────────────────────────────────┘
```

Emplacement prévu dans la grille pour la tuile notifications (partie 2) et la tuile voiture (partie 3).

| Tuile | Source | Contenu | Rafraîchissement |
|---|---|---|---|
| Position | `LocationManager` (pas le fournisseur Google) | position courante | ~1 km ou 2 min |
| Musique | `MediaSessionManager` (permission `MEDIA_CONTENT_CONTROL`) | pochette, titre, artiste, progression, ⏮ ⏯ ⏭ | temps réel |
| Heure + météo | Open-Meteo (sans clé) | heure, temp. actuelle, min/max, pluie prochaines heures, lever/coucher | 15 min ou 5 km |
| GPL | flux « prix des carburants » (data.economie.gouv.fr) | 3 stations GPL les moins chères à ≤ 20 km : prix, distance à vol d'oiseau, âge du prix ; appui → Waze | 10 min ou 2 km |
| Lieux | réglages (nom + adresse → coordonnées via `Geocoder`) | boutons ; appui → Waze | — |

Lien Waze : `https://waze.com/ul?ll=<lat>,<lon>&navigate=yes` (paquet `com.waze`).

## Thème clair / sombre

Android Auto n'expose pas son mode jour/nuit aux applis hors écran voiture : l'overview le reproduit.
- Mode **Auto** : nuit entre coucher et lever du soleil (données Open-Meteo pour la position courante).
- Capteur de luminosité de la tablette : passage en sombre si faible luminosité pendant plusieurs secondes
  (tunnel, parking), retour au clair seulement après un délai plus long (pas de clignotement).
- Réglage manuel : Auto / Toujours clair / Toujours sombre.
- Transition en fondu.

## Style et animations

Inspiration Tesla : fonds unis (noir en sombre, gris très clair en clair), typographie nette, grandes zones tactiles
(conduite), un seul accent de couleur. Animations courtes et discrètes : fondus et glissements entre états, chiffres qui
défilent (prix, température, distance), léger rétrécissement au toucher, fondu de pochette, entrée/sortie de la veille
sombre. Pas de flou plein écran ni de carte animée (GPU Adreno 610). Rien qui ressemble à de la vidéo (code de la route :
écran d'aide à la conduite uniquement).

## Erreurs et hors-ligne

- Chaque tuile a 4 états : chargement, OK, donnée ancienne, erreur. Une tuile en erreur n'affecte pas les autres.
- Dernière donnée reçue conservée sur le stockage : sans réseau, affichée avec son âge (« il y a 12 min ») et une
  légère transparence.
- Pas de position : dernière position connue, signalée sur la tuile GPL.
- Aucun lecteur actif : la tuile musique affiche un état vide avec un bouton pour ouvrir le dernier lecteur.

## Tests

- **Unitaires (JVM)**, objectif 80 % de couverture sur la logique : machine à états du mode conduite (entrée, sortie
  après 10 s, faux contact ignoré, restauration après plantage), lecture et filtrage du flux carburant (GPL, 20 km,
  tri, distance, prix anciens), jour/nuit (soleil + hystérésis du capteur), liens Waze. Services système simulés.
- **Captures d'écran automatiques** (Roborazzi) : chaque tuile dans ses 4 états, en clair et en sombre.
- **Sur la tablette avec le DHU** (`scripts/dhu.sh`) : connexion → premier plan, Wi-Fi Pixel, profil Automobile ;
  déconnexion → restauration ; lieu → Waze dans la fenêtre DHU puis retour de l'overview.
- **Fluidité** : même méthode que `scripts/bench.sh` (gfxinfo), objectif < 5 % d'images saccadées.
- **Essai voiture** : liste de contrôle (démarrage auto, point d'accès, Waze sur l'écran voiture, veille, thème nuit).

## Points à vérifier pendant le plan

1. Permissions réellement accordables à une priv-app sur ce build : désactivation de la connexion auto Wi-Fi
   (`WifiManager.allowAutojoin` : `NETWORK_SETTINGS` / `MANAGE_WIFI_NETWORK_SELECTION`), profils LineageOS
   (`lineageos.permission.MODIFY_PROFILES`), démarrage d'activité en arrière-plan, `MEDIA_CONTENT_CONTROL`.
   Repli root pour ce qui ne passe pas.
2. `CarConnection` signale bien la projection avec le DHU et en USB réel.
3. Format exact du flux carburant (champs GPL, filtre géographique `within_distance`) et de la réponse Open-Meteo.
4. L'écran de la tablette reste utilisable pendant la projection Android Auto.
