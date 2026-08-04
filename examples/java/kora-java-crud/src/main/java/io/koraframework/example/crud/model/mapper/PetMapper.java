package io.koraframework.example.crud.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import io.koraframework.example.crud.openapi.http.server.model.CategoryTO;
import io.koraframework.example.crud.openapi.http.server.model.PetTO;
import io.koraframework.example.crud.model.dao.PetCategory;
import io.koraframework.example.crud.model.dao.PetWithCategory;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}
