-- Script de la base de datos de EJEMPLO de la plantilla rest_java_server (MySQL).
-- Vive fuera del codigo del proyecto (no va en resources). Sustituir por el esquema del dominio.

DROP DATABASE IF EXISTS ejemplo;
CREATE DATABASE ejemplo;
USE ejemplo;
SET NAMES 'utf8';

CREATE TABLE elemento (
    chr_elecodigo  CHAR(4)       NOT NULL,
    vch_elenombre  VARCHAR(40)   NOT NULL,
    dec_elevalor   DECIMAL(12,2) NOT NULL,
    CONSTRAINT PK_elemento PRIMARY KEY (chr_elecodigo)
);

INSERT INTO elemento (chr_elecodigo, vch_elenombre, dec_elevalor) VALUES
    ('0001', 'Primer elemento', 10.50),
    ('0002', 'Segundo elemento', 20.00),
    ('0003', 'Tercer elemento', 0.99);

-- Usuario de la aplicacion: solo con permisos sobre las tablas del proyecto (no es el administrador).
DROP USER IF EXISTS 'ejemplo'@'localhost';
CREATE USER 'ejemplo'@'localhost' IDENTIFIED BY 'admin';
GRANT SELECT, INSERT, UPDATE, DELETE ON ejemplo.* TO 'ejemplo'@'localhost';
FLUSH PRIVILEGES;
