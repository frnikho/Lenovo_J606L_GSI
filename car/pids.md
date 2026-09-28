# PID OBD2 (mode 01) disponibles — Sandero 3 ECO-G 100, relevé Car Scanner 2026-09-28 (ralenti, moteur chaud, en GPL)

| PID | Donnée | Valeur relevée | Usage tableau de bord |
|-----|--------|----------------|------------------------|
| 04 | Charge calculée | 14,12 % | secondaire |
| 05 / 67 | Liquide de refroidissement | 83 °C | **jauge principale + alerte** |
| 06 / 07 | Correction richesse court / long terme B1 | 4,69 % / -2,34 % | diagnostic |
| 0B | Pression tubulure admission (MAP) | 30 kPa | **turbo : MAP − baro** |
| 0C | Régime | 849 tr/min | **jauge principale** |
| 0D | Vitesse | 0 km/h | **principale** |
| 0E | Avance allumage | 3° | diagnostic |
| 0F / 68 | Air admission (68 : B1S1 21 °C, B1S2 42 °C) | 42 °C | secondaire |
| 11 / 45 / 47 / 4C | Papillon abs. / relatif / B / commandé | 12,16 / 2,75 / 12,55 / 2,75 % | secondaire |
| 15 | Sonde lambda aval B1S2 | 0,64 V | diagnostic |
| 1C | Norme OBD | EOBD | – |
| 1F | Temps moteur depuis démarrage | 11 min 24 s | trajet |
| 21 | Distance avec témoin moteur | 0 km | **alerte** |
| 24 / 34 | Sonde lambda amont large bande | 14,55 / 1,98 V | diagnostic |
| 2E | Purge canister commandée | 0 % | – |
| 2F | Niveau de carburant | 100 % | **probablement GPL** (plein de GPL le matin, 60 km en GPL) — confirmer si jauge essence ≠ plein |
| 30 / 31 | Réchauffements / km depuis effacement défauts | 63 / 2505 km | entretien |
| 33 | Pression atmosphérique | 101 kPa | calcul turbo |
| 42 | Tension calculateur | 13,16 V | **batterie / alternateur** |
| 43 | Charge absolue | 18,04 % | secondaire |
| 44 | Richesse commandée (AFR) | 14,63 | diagnostic |
| 46 | Air ambiant | 21 °C | **extérieur** |
| 49 / 4A | Pédale accélérateur D / E | 16,47 % | secondaire |
| 51 | Type de carburant | **Bifuel running LPG** | **indicateur GPL / essence** |
| 5E | Débit carburant | 0 L/h (non géré ?) | conso : à estimer autrement |
| 61 / 62 / 63 | Couple demandé / réel / référence | 8 % / 10 % / 160 N·m | **couple (N·m) et puissance (kW)** |

## Dérivées utiles
- Pression turbo relative = PID 0B − PID 33 (ralenti : 30 − 101 = −71 kPa, dépression normale).
- Couple réel = PID 62 × PID 63 (10 % × 160 = 16 N·m) ; puissance kW = couple × tr/min / 9549.
- Conso instantanée : PID 5E absent → estimation « speed-density » (MAP, IAT, régime, cylindrée 999 cm³)
  avec rapport stœchiométrique selon PID 51 (essence ≈ 14,7, GPL ≈ 15,5). Approximatif, à calibrer.

## Non trouvé (pour l'instant)
- Niveau GPL : probablement PID 2F (voir ci-dessus). Niveau essence alors inconnu (DID Renault ?).
- Température d'huile (PID 5C) : non visible sur les captures.
