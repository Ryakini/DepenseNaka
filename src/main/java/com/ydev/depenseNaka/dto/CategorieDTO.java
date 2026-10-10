package com.ydev.depenseNaka.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public record CategorieDTO(
        String idCategorie,
        String nomCategorie,
        String descriptionCategorie) {
}
