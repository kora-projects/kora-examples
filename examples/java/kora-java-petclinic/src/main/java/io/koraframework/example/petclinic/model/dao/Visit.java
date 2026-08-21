package io.koraframework.example.petclinic.model.dao;

import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import java.time.LocalDate;

@EntityJdbc
@Table("visits")
public record Visit(@Id long id,
                    @Column("pet_id") long petId,
                    @Column("visit_date") LocalDate visitDate,
                    String description) {}
