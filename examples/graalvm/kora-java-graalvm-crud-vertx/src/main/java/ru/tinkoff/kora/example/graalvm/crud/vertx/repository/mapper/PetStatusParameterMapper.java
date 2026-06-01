package ru.tinkoff.kora.example.graalvm.crud.vertx.repository.mapper;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.vertx.mapper.parameter.VertxParameterColumnMapper;
import ru.tinkoff.kora.example.graalvm.crud.vertx.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements VertxParameterColumnMapper<Pet.Status> {

    @Override
    public Object apply(@Nullable Pet.Status value) {
        if (value == null) {
            return null;
        } else {
            return value.code;
        }
    }
}
