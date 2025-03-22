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
