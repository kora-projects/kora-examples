package io.koraframework.example.graalvm.crud.cassandra.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import io.koraframework.example.graalvm.crud.cassandra.model.dao.Pet;
import io.koraframework.example.graalvm.crud.openapi.server.model.CategoryTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    @Mapping(source = "pet", target = "category")
    PetTO asDTO(Pet pet);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "category", target = "name")
    CategoryTO asCategoryTO(Pet pet);
}
