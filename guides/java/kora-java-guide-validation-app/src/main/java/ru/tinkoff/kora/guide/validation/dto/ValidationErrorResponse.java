package ru.tinkoff.kora.guide.validation.dto;

import java.util.List;
import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record ValidationErrorResponse(String code, String message, List<ValidationErrorDetails> errors) {

    public static ValidationErrorResponse of(List<ValidationErrorDetails> errors) {
        return new ValidationErrorResponse("VALIDATION_ERROR", "Validation failed", errors);
    }
}
