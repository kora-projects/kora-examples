CREATE KEYSPACE IF NOT EXISTS petshop WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};


CREATE TABLE IF NOT EXISTS petshop.pets
(
    id       BIGINT,
    name     VARCHAR,
    status   INT,
    category VARCHAR,
    PRIMARY KEY (id)
);
