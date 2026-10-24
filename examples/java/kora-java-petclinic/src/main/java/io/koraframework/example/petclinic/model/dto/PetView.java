package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;
import java.time.LocalDate;
import java.util.List;

@Json
public record PetView(long id,
                      String name,
                      LocalDate birthDate,
                      PetTypeView type,
                      List<VisitView> visits) {}
