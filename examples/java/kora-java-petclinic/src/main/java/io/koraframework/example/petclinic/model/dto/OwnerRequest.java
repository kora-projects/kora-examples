package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.validation.common.annotation.NotBlank;
import io.koraframework.validation.common.annotation.Pattern;
import io.koraframework.validation.common.annotation.Size;
import io.koraframework.validation.common.annotation.Valid;

@Json
@Valid
public record OwnerRequest(@NotBlank @Size(max = 30) String firstName,
                           @NotBlank @Size(max = 30) String lastName,
                           @NotBlank @Size(max = 255) String address,
                           @NotBlank @Size(max = 80) String city,
                           @Pattern("^[0-9]{1,20}$") String telephone) {}
