package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;
import java.util.List;

@Json
public record VetView(long id,
                      String firstName,
                      String lastName,
                      List<String> specialties) {}
