package io.koraframework.kotlin.example.crud.model

import io.koraframework.example.crud.openapi.http.server.model.CategoryTO
import io.koraframework.example.crud.openapi.http.server.model.PetTO
import io.mcarle.konvert.api.Konverter

@Konverter
interface PetMapper {

    fun petWithCategoryToPetTO(pet: PetWithCategory): PetTO

    fun petCategoryToCategoryTO(category: PetCategory): CategoryTO
}
