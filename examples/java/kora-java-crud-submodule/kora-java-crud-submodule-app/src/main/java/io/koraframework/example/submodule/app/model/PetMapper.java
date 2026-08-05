package io.koraframework.example.submodule.app.model;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import io.koraframework.example.submodule.openapi.http.server.model.CategoryTO;
import io.koraframework.example.submodule.openapi.http.server.model.PetTO;
import io.koraframework.example.submodule.pet.model.dao.PetCategory;
import io.koraframework.example.submodule.pet.model.dao.PetWithCategory;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}
