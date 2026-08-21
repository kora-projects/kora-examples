package io.koraframework.example.petclinic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.Network;
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL;
import io.koraframework.example.petclinic.model.dto.OwnerRequest;
import io.koraframework.example.petclinic.model.dto.PetRequest;
import io.koraframework.example.petclinic.model.dto.VisitRequest;
import io.koraframework.example.petclinic.service.PetclinicService;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;
import java.time.LocalDate;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

@TestcontainersPostgreSQL(
        network = @Network(shared = true),
        mode = ContainerMode.PER_RUN,
        migration = @Migration(engine = Migration.Engines.FLYWAY,
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class PetclinicIntegrationTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private PetclinicService service;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                jdbc {
                  jdbcUrl = ${POSTGRES_JDBC_URL}
                  username = ${POSTGRES_USER}
                  password = ${POSTGRES_PASS}
                  poolName = "petclinic-test"
                }
                """)
                .withSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void findsSeededOwnersWithTheirPetsAndVisits() {
        var owners = service.findOwners(null);

        assertEquals(2, owners.size());
        assertIterableEquals(List.of("Davis", "Franklin"), owners.stream().map(o -> o.lastName()).toList());
        assertEquals("Davis", service.findOwners("DAV").getFirst().lastName());
        assertEquals(2, service.findOwners(" ").size());

        var george = service.findOwner(1);
        assertEquals("George", george.firstName());
        assertEquals("Leo", george.pets().getFirst().name());
        assertEquals("cat", george.pets().getFirst().type().name());
        assertEquals("annual checkup", george.pets().getFirst().visits().getFirst().description());
    }

    @Test
    void createsAndUpdatesOwner() {
        var created = service.createOwner(new OwnerRequest(
                "Jane", "Doe", "12 Main Street", "Madison", "6085550101"));

        assertNotEquals(0, created.id());
        assertTrue(created.pets().isEmpty());

        var updated = service.updateOwner(created.id(), new OwnerRequest(
                "Janet", "Doe", "14 Main Street", "Sun Prairie", "6085550102"));
        var loaded = service.findOwner(created.id());

        assertEquals(updated, loaded);
        assertEquals("Janet", loaded.firstName());
        assertEquals("14 Main Street", loaded.address());
        assertEquals("Sun Prairie", loaded.city());
        assertEquals("6085550102", loaded.telephone());
    }

    @Test
    void createsUpdatesPetAndAddsOrderedVisits() {
        var owner = service.createOwner(new OwnerRequest(
                "Jane", "Doe", "12 Main Street", "Madison", "6085550101"));
        var createdPet = service.addPet(owner.id(), new PetRequest("Nori", LocalDate.of(2022, 5, 4), 1));

        assertEquals("cat", createdPet.type().name());
        assertTrue(createdPet.visits().isEmpty());

        var updatedPet = service.updatePet(owner.id(), createdPet.id(),
                new PetRequest("Nori II", LocalDate.of(2022, 5, 5), 2));
        assertEquals("Nori II", updatedPet.name());
        assertEquals("dog", updatedPet.type().name());

        service.addVisit(owner.id(), createdPet.id(),
                new VisitRequest(LocalDate.of(2026, 8, 21), "dental check"));
        service.addVisit(owner.id(), createdPet.id(),
                new VisitRequest(LocalDate.of(2026, 8, 20), "general check"));

        var loadedPet = service.findOwner(owner.id()).pets().getFirst();
        assertIterableEquals(List.of("general check", "dental check"),
                loadedPet.visits().stream().map(v -> v.description()).toList());
    }

    @Test
    void returnsReferenceDataAndAggregatesJoinedSpecialties() {
        assertIterableEquals(List.of("bird", "cat", "dog", "hamster", "lizard", "snake"),
                service.findPetTypes().stream().map(t -> t.name()).toList());

        var vets = service.findVets();

        assertEquals(3, vets.size());
        assertEquals("Carter", vets.get(0).lastName());
        assertTrue(vets.get(0).specialties().isEmpty());
        assertEquals("Douglas", vets.get(1).lastName());
        assertIterableEquals(List.of("dentistry", "surgery"), vets.get(1).specialties());
        assertEquals("Leary", vets.get(2).lastName());
        assertIterableEquals(List.of("radiology"), vets.get(2).specialties());
    }
}
