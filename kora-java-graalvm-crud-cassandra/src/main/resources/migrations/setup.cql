CREATE KEYSPACE IF NOT EXISTS cassandra WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};


CREATE TABLE IF NOT EXISTS pets
(
    id       BIGINT,
    name     VARCHAR,
    status   INT,
    category VARCHAR,
    PRIMARY KEY (id)
);
