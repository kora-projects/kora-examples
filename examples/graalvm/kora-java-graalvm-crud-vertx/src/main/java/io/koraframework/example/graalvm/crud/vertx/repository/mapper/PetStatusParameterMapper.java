package io.koraframework.example.graalvm.crud.vertx.repository.mapper;

import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Component;
import io.koraframework.database.vertx.mapper.parameter.VertxParameterColumnMapper;
import io.koraframework.example.graalvm.crud.vertx.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements VertxParameterColumnMapper<Pet.Status> {

    @Override
    public Object apply(Pet.@Nullable Status value) {
        if (value == null) {
            return null;
        } else {
            return value.code;
        }
    }
}
