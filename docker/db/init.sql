DROP DATABASE IF EXISTS monitoring;
CREATE DATABASE monitoring;
\c monitoring;

CREATE TABLE record (
    id SERIAL PRIMARY KEY,
    boreholeNumber varchar(10),
    instrument varchar(20),
    surfaceLevel FLOAT,
    northing BIGINT,
    easting BIGINT,
    reading FLOAT,
    ts varchar(30)
);
