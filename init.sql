-- ============================================================
--  Initialisation de la base de données carburants
-- ============================================================

CREATE DATABASE IF NOT EXISTS carburants
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE carburants;

-- ── Table STATION ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS station (
    id_station  BIGINT       NOT NULL PRIMARY KEY,
    latitude    DOUBLE       NOT NULL,
    longitude   DOUBLE       NOT NULL,
    adresse     VARCHAR(255),
    ville       VARCHAR(100),
    cp          VARCHAR(10),
    automate    TINYINT(1)   DEFAULT 0,
    lavage      TINYINT(1)   DEFAULT 0,
    gonflage    TINYINT(1)   DEFAULT 0,
    nom_affiche VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table PRIX ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS prix (
    id_prix       BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_station    BIGINT        NOT NULL,
    nom_carburant VARCHAR(50)   NOT NULL,
    prix          DOUBLE        NOT NULL,
    date_maj      VARCHAR(50),
    FOREIGN KEY (id_station) REFERENCES station(id_station)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table HORAIRES ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS horaires (
    id_horaire  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_station  BIGINT       NOT NULL,
    jour        INT          NOT NULL,
    ouverture   VARCHAR(10),
    fermeture   VARCHAR(10),
    FOREIGN KEY (id_station) REFERENCES station(id_station)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Index ─────────────────────────────────────────────────
CREATE INDEX idx_prix_station     ON prix      (id_station);
CREATE INDEX idx_prix_carburant   ON prix      (nom_carburant);
CREATE INDEX idx_horaires_station ON horaires  (id_station);
CREATE INDEX idx_station_ville    ON station   (ville);