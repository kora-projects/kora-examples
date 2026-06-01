package ru.tinkoff.kora.guide.openapi.httpserver.advanced

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiController
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiDelegate
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiResponses
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class OpenApiHttpServerAdvancedAppTest {

    @TestComponent
    lateinit var dataApiDelegate: DataApiDelegate

    @Test
    fun dataFormFlowWorksThroughGeneratedDelegate() {
        val response = dataApiDelegate.processForm(DataApiController.ProcessFormFormParam("Ivan"))
        val form200 =
            assertInstanceOf(DataApiResponses.ProcessFormApiResponse.ProcessForm200ApiResponse::class.java, response)
        assertEquals("Hello World, Ivan", form200.content)
    }

    @Test
    fun dataMappingByCodeReturnsPayloadFor200() {
        val response = dataApiDelegate.mappingByCode(200)
        val mapping200 = assertInstanceOf(
            DataApiResponses.MappingByCodeApiResponse.MappingByCode200ApiResponse::class.java,
            response
        )
        assertEquals("Hello from response mapper", mapping200.content.message)
    }
}
