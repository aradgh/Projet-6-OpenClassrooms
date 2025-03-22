# Projet 6 - OpenClassrooms
## Script Setup SQL
src/main/resources/database_setup.sql

## Modèle Physique de Données (MPD)

Voici le schéma de la base de données :

![MPD](src/assets/pay_my_buddy_mpd.jpg)

- **Table User** : Contient les informations des utilisateurs.
- **Table Account** : Stocke les soldes des comptes liés aux utilisateurs.
- **Table Transaction** : Enregistre les transferts d’argent entre utilisateurs.
- **Table User_Connections** : Gère les relations entre utilisateurs (amis ou connexions).

Légende des icônes du schéma :

| Icon                                                                                 | Foreign key | Primary key | Indexed | NOT NULL |
|--------------------------------------------------------------------------------------|:-----------:|:-----------:|:-------:|:--------:|
| ![Column icon](src/assets/database-plugin.icons.expui.column.svg)                    |             |             |         |          |
| ![Column icon](src/assets/database-plugin.icons.expui.columnDot.svg)                 |             |             |         |    X     |
| ![Column icon](src/assets/database-plugin.icons.expui.columnDotIndex.svg)            |             |             |    X    |    X     |
| ![Column icon](src/assets/database-plugin.icons.expui.columnBlueKeyDotIndex.svg)     |      X      |             |    X    |    X     |
| ![Column icon](src/assets/database-plugin.icons.expui.columnGoldKeyDotIndex.svg)     |             |      X      |    X    |    X     |
| ![Column icon](src/assets/database-plugin.icons.expui.columnGoldBlueKeyDotIndex.svg) |      X      |      X      |    X    |    X     |

