package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.CategorieDTO;
import com.ydev.depenseNaka.model.CategorieEntity;
import org.springframework.stereotype.Component;

@Component
public class CategorieMapper {
    public CategorieDTO toDTO(CategorieEntity entity) {
        if (entity == null) return null;
        return CategorieDTO.builder()
                .idCategorie(entity.getIdCategorie())
                .nomCategorie(entity.getNomCategorie())
                .descriptionCategorie(entity.getDescriptionCategorie())
                .build();
    }

    public CategorieEntity toEntity(CategorieDTO dto) {
        if (dto == null) return null;
        return CategorieEntity.builder()
                .idCategorie(dto.idCategorie())
                .nomCategorie(dto.nomCategorie())
                .descriptionCategorie(dto.descriptionCategorie())
                .build();
    }
}
