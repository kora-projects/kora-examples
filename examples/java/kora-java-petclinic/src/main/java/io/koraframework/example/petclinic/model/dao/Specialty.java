package io.koraframework.example.petclinic.model.dao;

import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import org.jspecify.annotations.Nullable;

@EntityJdbc
@Table("specialties")
// TODO(Kora next version): make joined fields non-null after the @Embedded List<T> LEFT JOIN mapper fix is released.
public record Specialty(@Id @Nullable Long id, @Nullable String name) {}
