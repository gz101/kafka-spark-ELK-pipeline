DROP DATABASE IF EXISTS monitoring;
CREATE DATABASE monitoring;
\c monitoring;

CREATE TABLE waterstandpipe (
    boreholeNumber varchar(10) NOT NULL,
    instrument varchar(20) NOT NULL,
    surfaceLevel FLOAT NOT NULL,
    northing BIGINT NOT NULL,
    easting BIGINT NOT NULL,
    waterLevel FLOAT NOT NULL,
    ts varchar(30) NOT NULL
);
