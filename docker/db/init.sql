DROP DATABASE IF EXISTS monitoring;
CREATE DATABASE monitoring;
\c monitoring;

CREATE TABLE waterstandpipe (
    boreholeNumber varchar(10),
    instrument varchar(20),
    surfaceLevel FLOAT,
    northing BIGINT,
    easting BIGINT,
    waterLevel FLOAT,
    ts varchar(30)
);
