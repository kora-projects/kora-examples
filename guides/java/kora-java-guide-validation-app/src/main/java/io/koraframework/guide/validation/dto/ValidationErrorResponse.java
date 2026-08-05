package io.koraframework.guide.validation.dto;

import java.util.List;
import io.koraframework.json.common.annotation.Json;

@Json
public record ValidationErrorResponse(String code, String message, List<ValidationErrorDetails> errors) {

    public static ValidationErrorResponse of(List<ValidationErrorDetails> errors) {
        return new ValidationErrorResponse("VALIDATION_ERROR", "Validation failed", errors);
    }
}
