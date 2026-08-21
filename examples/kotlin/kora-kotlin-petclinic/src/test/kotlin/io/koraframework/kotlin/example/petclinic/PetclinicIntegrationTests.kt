package io.koraframework.kotlin.example.petclinic

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.Network
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import io.koraframework.kotlin.example.petclinic.model.OwnerRequest
import io.koraframework.kotlin.example.petclinic.model.PetRequest
import io.koraframework.kotlin.example.petclinic.model.VisitRequest
import io.koraframework.kotlin.example.petclinic.service.PetclinicService
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

@TestcontainersPostgreSQL(
    network = Network(shared = true),
    mode = ContainerMode.PER_RUN,
    migration = Migration(
        engine = Migration.Engines.FLYWAY,
        apply = Migration.Mode.PER_METHOD,
        drop = Migration.Mode.PER_METHOD,
    ),
)
@KoraAppTest(Application::class)
class PetclinicIntegrationTests(
    @ConnectionPostgreSQL private val connection: JdbcConnection,
) : KoraAppTestConfigModifier {
    @TestComponent
    lateinit var service: PetclinicService

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
        jdbc {
          jdbcUrl = "${connection.params().jdbcUrl()}"
          username = "${connection.params().username()}"
          password = "${connection.params().password()}"
          poolName = "petclinic-test"
        }
        """.trimIndent(),
    )

    @Test
    fun findsSeededOwnersWithTheirPetsAndVisits() {
        val owners = service.findOwners(null)

        assertEquals(2, owners.size)
        assertIterableEquals(listOf("Davis", "Franklin"), owners.map { it.lastName })
        assertEquals("Davis", service.findOwners("DAV").first().lastName)
        assertEquals(2, service.findOwners(" ").size)

        val george = service.findOwner(1)
        assertEquals("George", george.firstName)
        assertEquals("Leo", george.pets.first().name)
        assertEquals("cat", george.pets.first().type.name)
        assertEquals("annual checkup", george.pets.first().visits.first().description)
    }

    @Test
    fun createsAndUpdatesOwner() {
        val created = service.createOwner(OwnerRequest(
            "Jane", "Doe", "12 Main Street", "Madison", "6085550101",
        ))

        assertNotEquals(0, created.id)
        assertTrue(created.pets.isEmpty())

        val updated = service.updateOwner(created.id, OwnerRequest(
            "Janet", "Doe", "14 Main Street", "Sun Prairie", "6085550102",
        ))
        val loaded = service.findOwner(created.id)

        assertEquals(updated, loaded)
        assertEquals("Janet", loaded.firstName)
        assertEquals("14 Main Street", loaded.address)
        assertEquals("Sun Prairie", loaded.city)
        assertEquals("6085550102", loaded.telephone)
    }

    @Test
    fun createsUpdatesPetAndAddsOrderedVisits() {
        val owner = service.createOwner(OwnerRequest(
            "Jane", "Doe", "12 Main Street", "Madison", "6085550101",
        ))
        val createdPet = service.addPet(owner.id, PetRequest("Nori", LocalDate.of(2022, 5, 4), 1))

        assertEquals("cat", createdPet.type.name)
        assertTrue(createdPet.visits.isEmpty())

        val updatedPet = service.updatePet(
            owner.id,
            createdPet.id,
            PetRequest("Nori II", LocalDate.of(2022, 5, 5), 2),
        )
        assertEquals("Nori II", updatedPet.name)
        assertEquals("dog", updatedPet.type.name)

        service.addVisit(owner.id, createdPet.id, VisitRequest(LocalDate.of(2026, 8, 21), "dental check"))
        service.addVisit(owner.id, createdPet.id, VisitRequest(LocalDate.of(2026, 8, 20), "general check"))

        val loadedPet = service.findOwner(owner.id).pets.first()
        assertIterableEquals(listOf("general check", "dental check"), loadedPet.visits.map { it.description })
    }

    @Test
    fun returnsReferenceDataAndAggregatesJoinedSpecialties() {
        assertIterableEquals(
            listOf("bird", "cat", "dog", "hamster", "lizard", "snake"),
            service.findPetTypes().map { it.name },
        )

        val vets = service.findVets()

        assertEquals(3, vets.size)
        assertEquals("Carter", vets[0].lastName)
        assertTrue(vets[0].specialties.isEmpty())
        assertEquals("Douglas", vets[1].lastName)
        assertIterableEquals(listOf("dentistry", "surgery"), vets[1].specialties)
        assertEquals("Leary", vets[2].lastName)
        assertIterableEquals(listOf("radiology"), vets[2].specialties)
    }
}
