CREATE KEYSPACE IF NOT EXISTS cassandra WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS cassandra.entities
(
    id     VARCHAR,
    value1 INT,
    value2 VARCHAR,
    value3 VARCHAR,
    PRIMARY KEY (id)
);


CREATE TYPE IF NOT EXISTS cassandra.username(first text, last text);


CREATE TABLE IF NOT EXISTS cassandra.entities_udt
(
    id   VARCHAR,
    name FROZEN<username>,
    PRIMARY KEY (id)
);