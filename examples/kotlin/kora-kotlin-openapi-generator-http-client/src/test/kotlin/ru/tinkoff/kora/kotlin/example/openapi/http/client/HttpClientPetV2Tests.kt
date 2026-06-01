package ru.tinkoff.kora.kotlin.example.openapi.http.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody
import ru.tinkoff.kora.kotlin.example.openapi.petV2.api.PetApi
import ru.tinkoff.kora.kotlin.example.openapi.petV2.api.PetApiResponses
import ru.tinkoff.kora.kotlin.example.openapi.petV2.model.Category
import ru.tinkoff.kora.kotlin.example.openapi.petV2.model.Pet
import ru.tinkoff.kora.kotlin.example.openapi.petV2.model.Tag
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class HttpClientPetV2Tests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var petApi: PetApi

    override fun config(): KoraConfigModification = KoraConfigModification
        .ofSystemProperty("HTTP_CLIENT_PET_V2_URL", mockserverConnection.params().uri().toString())
        .withSystemProperty("HTTP_CLIENT_PET_V3_URL", mockserverConnection.params().uri().toString())

    @Test
    fun getRequestSuccess() {
        // given
        val tags = JSONArray()
        tags.put(JSONObject().put("id", 1L).put("name", "tag"))
        val responseBody = JSONObject()
            .put("id", 1L)
            .put("name", "name")
            .put("status", Pet.StatusEnum.AVAILABLE.getValue())
            .put("category", JSONObject().put("id", 1L).put("name", "category"))
            .put("tags", tags)

        mockserverConnection.client().`when`(
            request()
                .withMethod("GET")
                .withPath("/v2/pet/{id}")
                .withPathParameter("id", responseBody.getLong("id").toString())
        ).respond(response().withBody(responseBody.toString()))

        // when
        val response = petApi.getPetById(1L)

        // then
        if (response is PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse) {
            assertEquals(responseBody.getLong("id"), response.content.id)
            assertEquals(responseBody.getString("name"), response.content.name)
            assertEquals(responseBody.getString("status"), response.content.status?.getValue())
        } else {
            fail("Shouldn't happen")
        }
    }

    @Test
    fun postRequestSuccess() {
        // given
        val tags = JSONArray()
        tags.put(JSONObject().put("id", 1L).put("name", "tag"))
        val requestBody = JSONObject()
            .put("id", 1L)
            .put("name", "name")
            .put("status", Pet.StatusEnum.AVAILABLE.getValue())
            .put("category", JSONObject().put("id", 1L).put("name", "category"))
            .put("tags", tags)

        val responseBody = JSONObject()
        responseBody.put("message", "OK")

        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withPath("/v2/pet")
                .withBody(JsonBody(requestBody.toString()))
        ).respond(response().withBody(JsonBody(responseBody.toString())))

        // when
        val request = Pet(
            1L,
            "name",
            Category(1L, "category"),
            listOf(Tag(1L, "tag")),
            Pet.StatusEnum.AVAILABLE
        )
        val response = petApi.addPet(request)

        // then
        if (response is PetApiResponses.AddPetApiResponse.AddPet200ApiResponse) {
            assertEquals("OK", response.content.message)
        } else {
            fail("Shouldn't happen")
        }
    }
}
