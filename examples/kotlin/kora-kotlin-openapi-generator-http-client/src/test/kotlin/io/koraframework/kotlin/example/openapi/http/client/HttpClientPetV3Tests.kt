package io.koraframework.kotlin.example.openapi.http.client

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
import io.koraframework.kotlin.example.openapi.petV3.api.PetApi
import io.koraframework.kotlin.example.openapi.petV3.api.PetApiResponses
import io.koraframework.kotlin.example.openapi.petV3.model.Category
import io.koraframework.kotlin.example.openapi.petV3.model.Pet
import io.koraframework.kotlin.example.openapi.petV3.model.Tag
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class HttpClientPetV3Tests : KoraAppTestConfigModifier {

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
            .put("status", Pet.StatusEnum.AVAILABLE.value)
            .put("category", JSONObject().put("id", 1L).put("name", "category"))
            .put("tags", tags)

        mockserverConnection.client().`when`(
            request()
                .withMethod("GET")
                .withPath("/v3/pet/{id}")
                .withHeader("X-API-KEY", "MyAuthApiKey")
                .withPathParameter("id", responseBody.getLong("id").toString())
        ).respond(response().withBody(responseBody.toString()))

        // when
        val response = petApi.getPetById(1L)

        // then
        if (response is PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse) {
            assertEquals(responseBody.getLong("id"), response.content.id)
            assertEquals(responseBody.getString("name"), response.content.name)
            assertEquals(responseBody.getString("status"), response.content.status?.value)
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
            .put("status", Pet.StatusEnum.AVAILABLE.value)
            .put("category", JSONObject().put("id", 1L).put("name", "category"))
            .put("tags", tags)

        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withPath("/v3/pet")
                .withHeader("X-API-KEY", "MyAuthApiKey")
                .withBody(JsonBody(requestBody.toString()))
        ).respond(response().withBody(JsonBody(requestBody.toString())))

        // when
        // the 2.0 generator orders the constructor by optionality, so the arguments are named
        val request = Pet(
            id = 1L,
            name = "name",
            category = Category(1L, "category"),
            tags = listOf(Tag(1L, "tag")),
            status = Pet.StatusEnum.AVAILABLE
        )
        val response = petApi.addPet(request)

        // then
        if (response is PetApiResponses.AddPetApiResponse.AddPet200ApiResponse) {
            assertEquals(request, response.content)
        } else {
            fail("Shouldn't happen")
        }
    }
}
