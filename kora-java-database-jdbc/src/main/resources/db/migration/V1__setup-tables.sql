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


CREATE TABLE IF NOT EXISTS entities_composite
(
    a BIGINT    GENERATED ALWAYS AS IDENTITY,
    b BIGINT    GENERATED ALWAYS AS IDENTITY,
    name VARCHAR NOT NULL,
    PRIMARY KEY (a, b)
);


CREATE TABLE IF NOT EXISTS entities_composite_uuid
(
    a UUID    NOT NULL,
    b UUID    NOT NULL,
    name VARCHAR NOT NULL,
    PRIMARY KEY (a, b)
);


CREATE TABLE IF NOT EXISTS entities_jsonb
(
    id    UUID  NOT NULL,
    value JSONB NOT NULL,
    PRIMARY KEY (id)
);

