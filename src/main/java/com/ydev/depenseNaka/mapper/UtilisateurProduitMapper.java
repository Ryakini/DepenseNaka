package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.UtilisateurProduitDTO;
import com.ydev.depenseNaka.model.UtilisateurProduitEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UtilisateurProduitMapper {

    public UtilisateurProduitDTO toDTO(UtilisateurProduitEntity entity) {
        if (entity == null) return null;
        return new UtilisateurProduitDTO(
                entity.getIdUtilisateurProduit(),
                entity.getProduit() != null ? entity.getProduit().getIdProduit() : null,
                entity.getProduit() != null ? entity.getProduit().getNomProduit() : null,
                entity.getQuantiteCommandeProduit(),
                entity.getPrixAchatCommandeProduit(),
                entity.getMontantCommandeProduit()
        );
    }

    public UtilisateurProduitEntity toEntity(UtilisateurProduitDTO dto) {
        if (dto == null) return null;
        return UtilisateurProduitEntity.builder()
                .idUtilisateurProduit(dto.idUtilisateurProduit())
                .quantiteCommandeProduit(dto.quantiteCommandeProduit())
                .prixAchatCommandeProduit(dto.prixAchatCommandeProduit())
                .montantCommandeProduit(dto.montantCommandeProduit())
                .build();
    }

}
