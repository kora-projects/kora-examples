package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;
import java.time.LocalDate;

@Json
public record VisitView(long id, LocalDate visitDate, String description) {}
