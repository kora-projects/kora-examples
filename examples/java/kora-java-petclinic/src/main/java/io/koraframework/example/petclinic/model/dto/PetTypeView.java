package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record PetTypeView(long id, String name) {}
