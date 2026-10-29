-- Script de la base de datos de EJEMPLO de la plantilla soap_dotnet_server (SQL Server).
-- Vive fuera del codigo del proyecto (no va en el proyecto compilado). Sustituir por el esquema del dominio.
-- Ejecutar con un cliente que respete los lotes GO (sqlcmd, o "Execute script" en DBeaver).
-- Mismos nombres logicos de tablas y columnas que la plantilla Java (solo cambia el dialecto).

USE master;
GO

IF DB_ID(N'ejemplo') IS NOT NULL
BEGIN
    ALTER DATABASE ejemplo SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ejemplo;
END;
GO

CREATE DATABASE ejemplo;
GO

USE ejemplo;
GO

CREATE TABLE elemento (
    chr_elecodigo  CHAR(4)       NOT NULL,
    vch_elenombre  VARCHAR(40)   NOT NULL,
    dec_elevalor   DECIMAL(12,2) NOT NULL,
    CONSTRAINT PK_elemento PRIMARY KEY (chr_elecodigo)
);
GO

INSERT INTO elemento (chr_elecodigo, vch_elenombre, dec_elevalor) VALUES
    ('0001', 'Primer elemento', 10.50),
    ('0002', 'Segundo elemento', 20.00),
    ('0003', 'Tercer elemento', 0.99);
GO

-- Usuario de la aplicacion: solo con permisos sobre las tablas del proyecto (no es el administrador).
-- Es un LOGIN de SQL Server: requiere autenticacion mixta (ENTORNO_DOTNET.md 2.2).
USE master;
GO

IF NOT EXISTS ( SELECT 1 FROM sys.server_principals WHERE name = N'ejemplo' )
BEGIN
    CREATE LOGIN ejemplo WITH PASSWORD = N'admin', CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF;
END;
GO

USE ejemplo;
GO

CREATE USER ejemplo FOR LOGIN ejemplo;
GO

GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO ejemplo;
GO
