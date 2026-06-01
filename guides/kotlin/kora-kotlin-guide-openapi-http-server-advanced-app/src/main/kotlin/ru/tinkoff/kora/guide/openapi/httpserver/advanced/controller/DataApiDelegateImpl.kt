package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiController
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiDelegate
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.DataApiResponses
import ru.tinkoff.kora.guide.openapi.httpserver.data.model.PayloadTO
import ru.tinkoff.kora.guide.openapi.httpserver.data.model.UploadResponseTO
import ru.tinkoff.kora.http.server.common.HttpServerResponseException

@Component
class DataApiDelegateImpl : DataApiDelegate {

    override fun processForm(form: DataApiController.ProcessFormFormParam): DataApiResponses.ProcessFormApiResponse {
        if (form.name.equals("admin", ignoreCase = true)) {
            throw RestrictedFormNameException(form.name)
        }

        return DataApiResponses.ProcessFormApiResponse.ProcessForm200ApiResponse("Hello World, ${form.name}")
    }

    override fun processUpload(form: DataApiController.ProcessUploadFormParam): DataApiResponses.ProcessUploadApiResponse {
        val response = UploadResponseTO(1, listOf(form.file.name()))
        return DataApiResponses.ProcessUploadApiResponse.ProcessUpload200ApiResponse(response)
    }

    override fun mappingByCode(code: Int): DataApiResponses.MappingByCodeApiResponse {
        if (code == 200) {
            return DataApiResponses.MappingByCodeApiResponse.MappingByCode200ApiResponse(
                PayloadTO("Hello from response mapper")
            )
        }
        throw HttpServerResponseException.of(code, "Request failed with code $code")
    }
}
