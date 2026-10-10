package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.ConnexionDTO;
import com.ydev.depenseNaka.model.ConnexionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnexionMapper {
    public ConnexionDTO toDTO(ConnexionEntity entity) {
        if (entity == null) return null;
        return new ConnexionDTO(
                entity.getIdConnexion(),
                entity.getNomConnexion(),
                entity.getDateDebutConnexion(),
                entity.getDateFinConnexion(),
                entity.isActive(),
                entity.getUtilisateur() != null ? entity.getUtilisateur().getIdUtilisateur() : null,
                entity.getUtilisateur() != null ? entity.getUtilisateur().getPseudoUtilisateur() : null
        );
    }
}
