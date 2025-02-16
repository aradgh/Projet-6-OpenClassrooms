-- Suppression de la base existante (si présente)
DROP DATABASE IF EXISTS pay_my_buddy;

-- Création de la base de données
CREATE DATABASE pay_my_buddy;

-- Sélection de la base de données
USE pay_my_buddy;

-- Création de la table User (sans colonne username)
CREATE TABLE User (
                      id       BIGINT AUTO_INCREMENT PRIMARY KEY,
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
                             FOREIGN KEY (sender_id) REFERENCES User(id),
                             FOREIGN KEY (receiver_id) REFERENCES User(id)
);

-- Création de la table de la relation Many-to-Many pour les connexions utilisateur
CREATE TABLE User_Connections (
                                  user_id       BIGINT NOT NULL,
                                  connection_id BIGINT NOT NULL,
                                  PRIMARY KEY (user_id, connection_id),
                                  FOREIGN KEY (user_id) REFERENCES User(id),
                                  FOREIGN KEY (connection_id) REFERENCES User(id)
);

-- Insertion des utilisateurs
INSERT INTO User (email, password)
VALUES
    ('alice@example.com', 'hashed_password1'),
    ('bob@example.com', 'hashed_password2'),
    ('charlie@example.com', 'hashed_password3');

-- Insertion des comptes (la colonne est désormais user_id)
INSERT INTO Account (user_id, balance)
VALUES
    (1, 100.00),
    (2, 200.50),
    (3, 150.75);

-- Insertion des transactions en utilisant la date courante pour timestamp
INSERT INTO Transaction (sender_id, receiver_id, amount, timestamp, description)
VALUES
    (1, 2, 25.00, NOW(), 'Dinner payment'),
    (2, 3, 50.00, NOW(), 'Gift'),
    (3, 1, 30.00, NOW(), 'Reimbursement');

-- Insertion des connexions utilisateur
INSERT INTO User_Connections (user_id, connection_id)
VALUES
    (1, 2),
    (1, 3),
    (2, 3);

-- Insertion d'un utilisateur test avec données sécurisées
INSERT INTO User (email, password)
VALUES
    ('indiana.jones@wanadoo.com', SHA2('holy_grail', 256));
