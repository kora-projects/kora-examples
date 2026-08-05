package io.koraframework.guide.openapi.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.openapi.httpserver.data.api.DataApiController
import io.koraframework.guide.openapi.httpserver.data.api.DataApiDelegate
import io.koraframework.guide.openapi.httpserver.data.api.DataApiResponses
import io.koraframework.guide.openapi.httpserver.data.model.PayloadTO
import io.koraframework.guide.openapi.httpserver.data.model.UploadResponseTO
import io.koraframework.http.server.common.response.HttpServerResponseException

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
