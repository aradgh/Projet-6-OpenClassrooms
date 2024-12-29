-- Création de la base de données (si nécessaire)
CREATE DATABASE IF NOT EXISTS pay_my_buddy;

-- Sélection de la base de données
USE pay_my_buddy;

-- Création de la table User
CREATE TABLE User
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Création de la table Account
CREATE TABLE Account
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT            NOT NULL,
    balance DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES User (id)
);

-- Création de la table Transaction
CREATE TABLE Transaction
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT            NOT NULL,
    receiver_id INT            NOT NULL,
    description VARCHAR(255),
    amount      DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES Account (id),
    FOREIGN KEY (receiver_id) REFERENCES Account (id)
);

-- Création de la table User_Connections (relation N:N)
CREATE TABLE User_Connections
(
    user_id       INT NOT NULL,
    connection_id INT NOT NULL,
    PRIMARY KEY (user_id, connection_id),
    FOREIGN KEY (user_id) REFERENCES User (id),
    FOREIGN KEY (connection_id) REFERENCES User (id)
);

-- Insertion des utilisateurs
INSERT INTO User (username, email, password)
VALUES ('alice', 'alice@example.com', 'hashed_password1'),
       ('bob', 'bob@example.com', 'hashed_password2'),
       ('charlie', 'charlie@example.com', 'hashed_password3');

-- Insertion des comptes
INSERT INTO Account (owner_id, balance)
VALUES (1, 100.00),
       (2, 200.50),
       (3, 150.75);

-- Insertion des transactions
INSERT INTO Transaction (sender_id, receiver_id, description, amount)
VALUES (1, 2, 'Dinner payment', 25.00),
       (2, 3, 'Gift', 50.00),
       (3, 1, 'Reimbursement', 30.00);

-- Insertion des connexions utilisateur
INSERT INTO User_Connections (user_id, connection_id)
VALUES (1, 2),
       (1, 3),
       (2, 3);
