# Dacia Sandero 3 (2022) ECO-G 100 — infos OBD (Car Scanner, 2026-09-28)

- Interface : ELM327 v1.5 clone (PIC18F25K80), Bluetooth classique, alimenté contact mis.
- Protocole : **7 — ISO 15765-4 CAN, identifiants 29 bits, 500 kbit/s** (ELM327 : `ATSP7`).
- Calculateur moteur : « ECM -EngineControl », adresse/CAN id rapportée par Car Scanner : 15.
  En 29 bits, réponses attendues sur `18DAF1xx` (xx = adresse du calculateur).
- VIN : présent (mode 09 PID 02), non recopié ici.
- Homologation échappement : HMLGT1447R
- CVN : 011B1B0456 — CALID : 591000096
- Suivi de performance en service (mode 09, IUMPR) : valeurs incohérentes (quasi toutes de la forme 0xXX00,
  ex. 41728 = 0xA300, « complétions » > « conditions ») → réponse multi-trame mal découpée par Car Scanner.
  Sans intérêt pour le tableau de bord.

## À obtenir
- Liste des capteurs en temps réel (mode 01 + profil Renault/Dacia) : surtout GPL et température d'huile.
- Log CSV en roulant (GPL puis essence).
- Journal de communication ELM327 (commandes / réponses brutes).
