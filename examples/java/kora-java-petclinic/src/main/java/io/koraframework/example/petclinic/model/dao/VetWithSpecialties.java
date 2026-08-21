package io.koraframework.example.petclinic.model.dao;

import io.koraframework.database.common.annotation.Embedded;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import java.util.List;

@EntityJdbc
public record VetWithSpecialties(@Embedded("v_") Vet vet,
                                 @Embedded("s_") List<Specialty> specialties) {}
