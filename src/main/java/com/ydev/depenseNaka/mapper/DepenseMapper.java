package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.DepenseDTO;
import com.ydev.depenseNaka.model.DepenseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepenseMapper {
    public DepenseDTO toDTO(DepenseEntity entity) {
        if (entity == null) return null;
        return new DepenseDTO(
                entity.getIdDepense(),
                entity.getDateCreationDepense(),
                entity.getDateModificationDepense(),
                entity.getLibelleDepense(),
                entity.getPrixDepense(),
                entity.getQuantiteDepense(),
                entity.getMontantDepense(),
                entity.getDescriptionDepense(),
                entity.getCommande() != null ? entity.getCommande().getIdCommande() : null
        );
    }

    public DepenseEntity toEntity(DepenseDTO dto) {
        if (dto == null) return null;
        return DepenseEntity.builder()
                .idDepense(dto.idDepense())
                .dateCreationDepense(dto.dateCreationDepense())
                .dateModificationDepense(dto.dateModificationDepense())
                .libelleDepense(dto.libelleDepense())
                .prixDepense(dto.prixDepense())
                .quantiteDepense(dto.quantiteDepense())
                .montantDepense(dto.montantDepense())
                .descriptionDepense(dto.descriptionDepense())
                .build();
    }
}
