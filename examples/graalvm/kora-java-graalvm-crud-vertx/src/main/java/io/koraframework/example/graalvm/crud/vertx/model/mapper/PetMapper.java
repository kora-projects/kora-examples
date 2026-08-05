package io.koraframework.example.graalvm.crud.vertx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import io.koraframework.example.graalvm.crud.openapi.server.model.CategoryTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetTO;
import io.koraframework.example.graalvm.crud.vertx.model.dao.PetCategory;
import io.koraframework.example.graalvm.crud.vertx.model.dao.PetWithCategory;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}
