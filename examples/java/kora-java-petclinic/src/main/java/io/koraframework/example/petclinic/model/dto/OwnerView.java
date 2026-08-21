package io.koraframework.example.petclinic.model.dto;

import io.koraframework.json.common.annotation.Json;
import java.util.List;

@Json
public record OwnerView(long id,
                        String firstName,
                        String lastName,
                        String address,
                        String city,
                        String telephone,
                        List<PetView> pets) {}
