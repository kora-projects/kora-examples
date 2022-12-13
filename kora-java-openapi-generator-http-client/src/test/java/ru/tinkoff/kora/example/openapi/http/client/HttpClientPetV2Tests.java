package ru.tinkoff.kora.example.openapi.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ContainerMockserverConnection;
import io.goodforgod.testcontainers.extensions.mockserver.MockserverConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockserver;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockserver.model.JsonBody;
import ru.tinkoff.kora.example.openapi.petV2.api.PetApi;
import ru.tinkoff.kora.example.openapi.petV2.api.PetApiResponses;
import ru.tinkoff.kora.example.openapi.petV2.model.Category;
import ru.tinkoff.kora.example.openapi.petV2.model.Pet;
import ru.tinkoff.kora.example.openapi.petV2.model.Tag;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersMockserver(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class HttpClientPetV2Tests implements KoraAppTestConfigModifier {

    @ContainerMockserverConnection
    private MockserverConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("HTTP_CLIENT_PET_V2_URL", mockserverConnection.params().uri().toString())
                .withSystemProperty("HTTP_CLIENT_PET_V3_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private PetApi petApi;

    @Test
    void getRequestSuccess() {
        // given
        var tags = new JSONArray();
        tags.put(new JSONObject()
                .put("id", 1L)
                .put("name", "tag"));
        var responseBody = new JSONObject()
                .put("id", 1L)
                .put("name", "name")
                .put("status", Pet.StatusEnum.AVAILABLE.getValue())
                .put("category", new JSONObject()
                        .put("id", 1L)
                        .put("name", "category"))
                .put("tags", tags);

        mockserverConnection.client().when(request()
                .withMethod("GET")
                .withPath("/v2/pet/{id}")
                .withPathParameter("id", String.valueOf(responseBody.getLong("id"))))
                .respond(response()
                        .withBody(responseBody.toString()));

        // when
        // then
        var response = petApi.getPetById(1L);
        if (response instanceof PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse r) {
            assertEquals(responseBody.getLong("id"), r.content().id());
            assertEquals(responseBody.getString("name"), r.content().name());
            assertEquals(responseBody.getString("status"), r.content().status().getValue());
        } else {
            fail("Shouldn't happen");
        }
    }

    @Test
    void postRequestSuccess() {
        // given
        var tags = new JSONArray();
        tags.put(new JSONObject()
                .put("id", 1L)
                .put("name", "tag"));
        var requestBody = new JSONObject()
                .put("id", 1L)
                .put("name", "name")
                .put("status", Pet.StatusEnum.AVAILABLE.getValue())
                .put("category", new JSONObject()
                        .put("id", 1L)
                        .put("name", "category"))
                .put("tags", tags);

        var responseBody = new JSONObject();
        responseBody.put("message", "OK");

        mockserverConnection.client().when(request()
                .withMethod("POST")
                .withPath("/v2/pet")
                .withBody(new JsonBody(requestBody.toString())))
                .respond(response()
                        .withBody(new JsonBody(responseBody.toString())));

        // when
        // then
        var request = new Pet(1L,
                new Category(1L, "category"), "name",
                List.of(new Tag(1L, "tag")),
                Pet.StatusEnum.AVAILABLE);
        var response = petApi.addPet(request);
        if (response instanceof PetApiResponses.AddPetApiResponse.AddPet200ApiResponse r) {
            assertEquals("OK", r.content().message());
        } else {
            fail("Shouldn't happen");
        }
    }
}
