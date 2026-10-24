CREATE TABLE owners (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(80) NOT NULL,
    telephone VARCHAR(20) NOT NULL
);
CREATE INDEX owners_last_name_idx ON owners (last_name);
CREATE TABLE pet_types (id BIGSERIAL PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE);
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES owners(id),
    type_id BIGINT NOT NULL REFERENCES pet_types(id),
    name VARCHAR(30) NOT NULL,
    birth_date DATE NOT NULL
);
CREATE INDEX pets_owner_id_idx ON pets (owner_id);
CREATE TABLE visits (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id),
    visit_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL
);
CREATE INDEX visits_pet_id_idx ON visits (pet_id);
CREATE TABLE vets (id BIGSERIAL PRIMARY KEY, first_name VARCHAR(30) NOT NULL, last_name VARCHAR(30) NOT NULL);
CREATE TABLE specialties (id BIGSERIAL PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE);
CREATE TABLE vet_specialties (
    vet_id BIGINT NOT NULL REFERENCES vets(id),
    specialty_id BIGINT NOT NULL REFERENCES specialties(id),
    PRIMARY KEY (vet_id, specialty_id)
);
INSERT INTO pet_types(name) VALUES ('cat'), ('dog'), ('lizard'), ('snake'), ('bird'), ('hamster');
INSERT INTO owners(first_name, last_name, address, city, telephone) VALUES
    ('George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023'),
    ('Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO pets(owner_id, type_id, name, birth_date) VALUES
    (1, 1, 'Leo', DATE '2020-09-07'),
    (2, 2, 'Basil', DATE '2019-08-06');
INSERT INTO visits(pet_id, visit_date, description) VALUES (1, DATE '2024-01-01', 'annual checkup');
INSERT INTO vets(first_name, last_name) VALUES ('James', 'Carter'), ('Helen', 'Leary'), ('Linda', 'Douglas');
INSERT INTO specialties(name) VALUES ('radiology'), ('surgery'), ('dentistry');
INSERT INTO vet_specialties(vet_id, specialty_id) VALUES (2, 1), (3, 2), (3, 3);
