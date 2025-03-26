-- Suppression de la base existante (si présente)
DROP DATABASE IF EXISTS pay_my_buddy;

-- Création de la base de données
CREATE DATABASE pay_my_buddy;

-- Sélection de la base de données
USE pay_my_buddy;

-- Création de la table User
CREATE TABLE User (
                      id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(255) NOT NULL,
                      email    VARCHAR(255) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL
);

-- Création de la table Account avec la colonne user_id correspondant au propriétaire
CREATE TABLE Account (
                         id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         balance DECIMAL(10, 2) NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES User(id)
);

-- Création de la table Transaction
CREATE TABLE Transaction (
                             id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                             sender_id   BIGINT NOT NULL,
                             receiver_id BIGINT NOT NULL,
                             amount      DECIMAL(10, 2) NOT NULL,
                             timestamp   DATETIME NOT NULL,
                             description VARCHAR(255),
                             FOREIGN KEY (sender_id) REFERENCES Account(id),
                             FOREIGN KEY (receiver_id) REFERENCES Account(id)
);

-- Création de la table de la relation Many-to-Many pour les connexions utilisateur
CREATE TABLE User_Connections (
                                  user_id       BIGINT NOT NULL,
                                  connection_id BIGINT NOT NULL,
                                  PRIMARY KEY (user_id, connection_id),
                                  FOREIGN KEY (user_id) REFERENCES User(id),
                                  FOREIGN KEY (connection_id) REFERENCES User(id)
);

-- Insertion des données factices --

-- Insertion des utilisateurs
INSERT INTO User (username, email, password) VALUES
                                                 ('Alice', 'alice@example.com', '$2a$10$V7HzBxOm.58foA/Es0RSu.WSOTWBOl37RmVPaajd3gOBDiwMG2F1e'),
                                                 ('Bob', 'bob@example.com', '$2a$10$V7HzBxOm.58foA/Es0RSu.WSOTWBOl37RmVPaajd3gOBDiwMG2F1e'),
                                                 ('Charlie', 'charlie@example.com', '$2a$10$V7HzBxOm.58foA/Es0RSu.WSOTWBOl37RmVPaajd3gOBDiwMG2F1e'),
                                                 ('David', 'david@example.com', '$2a$10$V7HzBxOm.58foA/Es0RSu.WSOTWBOl37RmVPaajd3gOBDiwMG2F1e'),
                                                 ('Eve', 'eve@example.com', '$2a$10$V7HzBxOm.58foA/Es0RSu.WSOTWBOl37RmVPaajd3gOBDiwMG2F1e');

-- Insertion des comptes associés aux utilisateurs
-- On suppose ici que l’auto-incrément attribuera les IDs dans l’ordre d’insertion (1 pour Alice, 2 pour Bob, etc.)
INSERT INTO Account (user_id, balance) VALUES
                                           (1, 100.00),
                                           (2, 150.00),
                                           (3, 200.00),
                                           (4, 50.00),
                                           (5, 75.00);

-- Insertion des transactions entre comptes
INSERT INTO Transaction (sender_id, receiver_id, amount, timestamp, description) VALUES
                                                                                     (1, 2, 20.00, '2025-03-20 10:00:00', 'Remboursement déjeuner'),
                                                                                     (2, 3, 15.50, '2025-03-21 11:30:00', 'Paiement café'),
                                                                                     (3, 1, 30.00, '2025-03-22 14:00:00', 'Cadeau'),
                                                                                     (4, 5, 5.00,  '2025-03-23 09:15:00', 'Remboursement bus'),
                                                                                     (5, 2, 10.00, '2025-03-24 16:45:00', 'Billets de cinéma');

-- Insertion des connexions entre utilisateurs (relation Many-to-Many)
-- Ici, nous insérons des relations symétriques pour représenter des connexions bidirectionnelles
INSERT INTO User_Connections (user_id, connection_id) VALUES
                                                          (1, 2),
                                                          (1, 3),
                                                          (2, 1),
                                                          (2, 4),
                                                          (3, 1),
                                                          (3, 5),
                                                          (4, 2),
                                                          (5, 3);