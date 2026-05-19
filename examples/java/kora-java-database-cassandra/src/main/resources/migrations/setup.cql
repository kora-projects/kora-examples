CREATE TABLE IF NOT EXISTS entities
(
    id     VARCHAR,
    value1 INT,
    value2 VARCHAR,
    value3 VARCHAR,
    PRIMARY KEY (id)
);


CREATE TYPE IF NOT EXISTS username(first text, last text);


CREATE TABLE IF NOT EXISTS entities_udt
(
    id   VARCHAR,
    name FROZEN<username>,
    PRIMARY KEY (id)
);