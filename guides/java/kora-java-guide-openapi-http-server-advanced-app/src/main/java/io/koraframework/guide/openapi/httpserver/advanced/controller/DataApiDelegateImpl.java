package io.koraframework.guide.openapi.httpserver.advanced.controller;

import io.koraframework.common.annotation.Component;
import io.koraframework.guide.openapi.httpserver.data.api.DataApiController;
import io.koraframework.guide.openapi.httpserver.data.api.DataApiDelegate;
import io.koraframework.guide.openapi.httpserver.data.api.DataApiResponses;
import io.koraframework.guide.openapi.httpserver.data.model.PayloadTO;
import io.koraframework.guide.openapi.httpserver.data.model.UploadResponseTO;
import io.koraframework.http.server.common.response.HttpServerResponseException;

import java.util.List;

@Component
public final class DataApiDelegateImpl implements DataApiDelegate {

    @Override
    public DataApiResponses.ProcessFormApiResponse processForm(DataApiController.ProcessFormFormParam form) {
        if ("admin".equalsIgnoreCase(form.name())) {
            throw new RestrictedFormNameException(form.name());
        }
        return new DataApiResponses.ProcessFormApiResponse.ProcessForm200ApiResponse("Hello World, " + form.name());
    }

    @Override
    public DataApiResponses.ProcessUploadApiResponse processUpload(DataApiController.ProcessUploadFormParam form) {
        var response = new UploadResponseTO(
                1,
                List.of(form.file().name())
        );
        return new DataApiResponses.ProcessUploadApiResponse.ProcessUpload200ApiResponse(response);
    }

    @Override
    public DataApiResponses.MappingByCodeApiResponse mappingByCode(int code) {
        if (code == 200) {
            return new DataApiResponses.MappingByCodeApiResponse.MappingByCode200ApiResponse(
                    new PayloadTO("Hello from response mapper")
            );
        }
        throw HttpServerResponseException.of(code, "Request failed with code " + code);
    }
}
