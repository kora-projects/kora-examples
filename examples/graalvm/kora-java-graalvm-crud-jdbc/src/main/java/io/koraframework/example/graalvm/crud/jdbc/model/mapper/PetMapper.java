package io.koraframework.example.graalvm.crud.jdbc.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import io.koraframework.example.graalvm.crud.jdbc.model.dao.PetCategory;
import io.koraframework.example.graalvm.crud.jdbc.model.dao.PetWithCategory;
import io.koraframework.example.graalvm.crud.openapi.server.model.CategoryTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}
