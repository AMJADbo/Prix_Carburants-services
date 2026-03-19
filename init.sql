-- Le fichier init.sql initialise la strucute de la BDD MySQL au premier démarrage


-- Initialisation de la base de données carburants
CREATE DATABASE IF NOT EXISTS carburants
    -- Encodage UTF-8 (accents)
    CHARACTER SET utf8mb4
    -- Sensibilité à la casse, accents, ...
    COLLATE utf8mb4_unicode_ci;

-- Sélectionne la BDD pour les opérations suivantes
USE carburants;

-- Table STATION : Stocke les informations de chaque station service
CREATE TABLE IF NOT EXISTS station (
    -- Intérêt du BIGINT : gère les très grands nombres pour les codes de stations
    id_station  BIGINT       NOT NULL PRIMARY KEY,
    latitude    DOUBLE       NOT NULL,
    longitude   DOUBLE       NOT NULL,
    adresse     VARCHAR(255),
    ville       VARCHAR(100),
    cp          VARCHAR(10),
    -- Intérêt du TINYINT : économie de mémoire (suffisant pour binaire 1/0 - simuler booléens)
    automate    TINYINT(1)   DEFAULT 0,
    lavage      TINYINT(1)   DEFAULT 0,
    gonflage    TINYINT(1)   DEFAULT 0,
    nom_affiche VARCHAR(255)
-- Support les transactions et clés étrangères
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table PRIX : Stocke les prix des carburants pour chaque station
CREATE TABLE IF NOT EXISTS prix (
    id_prix       BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_station    BIGINT        NOT NULL,
    -- Type de carburant (gazole, SP95, SP98, ...)
    nom_carburant VARCHAR(50)   NOT NULL,
    prix          DOUBLE        NOT NULL,
    date_maj      VARCHAR(50),
    -- Lie le prix à une station existante
    FOREIGN KEY (id_station) REFERENCES station(id_station)
        -- Si la station est supprimée, tous ses prix aussi
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table HORAIRES : Stocke les horaires d'ouverture de chaque station par jour
CREATE TABLE IF NOT EXISTS horaires (
    id_horaire  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_station  BIGINT       NOT NULL,
    jour        INT          NOT NULL,
    ouverture   VARCHAR(10),
    fermeture   VARCHAR(10),
    FOREIGN KEY (id_station) REFERENCES station(id_station)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index : trie les données et ajoute des pointeurs directs pour accélérer les recherches
-- Accélère la recherche des prix par station
CREATE INDEX idx_prix_station     ON prix      (id_station);
-- Accélère la recherche des prix par type de carbura,t
CREATE INDEX idx_prix_carburant   ON prix      (nom_carburant);
-- Accélère la recherche des horaraires par station
CREATE INDEX idx_horaires_station ON horaires  (id_station);
-- Accélère la recherche des stations par ville
CREATE INDEX idx_station_ville    ON station   (ville);