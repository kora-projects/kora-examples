# Kora Kotlin Petclinic

REST analogue of Spring Petclinic built with Kora 2.0, Kotlin, Java 25, Undertow and PostgreSQL.

Covered flows: owner search/create/update, pets and pet types, visits, vets and specialties.
Writes spanning validation and lookup use JDBC transactions. Owner responses aggregate pets,
types and visit history.

From repository root, start PostgreSQL; Flyway applies schema and seed data during application startup:

```shell
docker compose -f examples/kotlin/kora-kotlin-petclinic/docker-compose.yml up -d
```

Then run:

```shell
./gradlew :examples:kotlin:kora-kotlin-petclinic:run
```

Main endpoints:

- `GET /api/owners?lastName=...`
- `POST /api/owners`
- `POST /api/owners/{ownerId}/pets`
- `POST /api/owners/{ownerId}/pets/{petId}/visits`
- `GET /api/pet-types`
- `GET /api/vets`
