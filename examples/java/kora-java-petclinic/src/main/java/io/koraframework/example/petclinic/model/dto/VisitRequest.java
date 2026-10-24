package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.validation.common.annotation.NotBlank;
import io.koraframework.validation.common.annotation.Size;
import io.koraframework.validation.common.annotation.Valid;
import java.time.LocalDate;

@Json
@Valid
public record VisitRequest(LocalDate visitDate,
                           @NotBlank @Size(max = 255) String description) {}
