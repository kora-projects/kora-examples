CREATE TABLE IF NOT EXISTS entities
(
    id     VARCHAR NOT NULL,
    value1 INT     NOT NULL,
    value2 VARCHAR NOT NULL,
    value3 VARCHAR,
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS entities_sequence
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR NOT NULL,
    code VARCHAR NOT NULL DEFAULT '1',
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS entities_uuid
(
    id   UUID    NOT NULL,
    name VARCHAR NOT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS entities_jsonb
(
    id    UUID  NOT NULL,
    value JSONB NOT NULL,
    PRIMARY KEY (id)
);
