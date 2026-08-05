package io.koraframework.example.submodule.app.model;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import io.koraframework.example.submodule.openapi.http.server.model.VetTO;
import io.koraframework.example.submodule.vet.model.dao.Vet;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VetMapper {

    VetTO asDTO(Vet pet);
}
