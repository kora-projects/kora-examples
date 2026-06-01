package ru.tinkoff.kora.guide.openapi.httpserver.advanced;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiController;
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiDelegate;
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiResponses;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@KoraAppTest(Application.class)
class OpenApiHttpServerAdvancedAppTest {

    @TestComponent
    private DataApiDelegate dataApiDelegate;

    @Test
    void dataFormFlowWorksThroughGeneratedDelegate() throws Exception {
        var response = this.dataApiDelegate.processForm(new DataApiController.ProcessFormFormParam("Ivan"));
        var form200 = assertInstanceOf(DataApiResponses.ProcessFormApiResponse.ProcessForm200ApiResponse.class, response);
        assertEquals("Hello World, Ivan", form200.content());
    }

    @Test
    void dataMappingByCodeReturnsPayloadFor200() throws Exception {
        var response = this.dataApiDelegate.mappingByCode(200);
        var mapping200 = assertInstanceOf(DataApiResponses.MappingByCodeApiResponse.MappingByCode200ApiResponse.class, response);
        assertEquals("Hello from response mapper", mapping200.content().message());
    }
}

