DROP DATABASE IF EXISTS testdb;
CREATE DATABASE testdb;
\c testdb;

CREATE TABLE testtable (
    id INT NOT NULL,
    input varchar(250) NOT NULL,
    PRIMARY KEY (id)
);
