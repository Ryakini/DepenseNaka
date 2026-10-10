package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.FactureProduitDTO;
import com.ydev.depenseNaka.model.FactureProduitEntity;
import org.springframework.stereotype.Component;

@Component
public class FactureProduitMapper {
    public FactureProduitDTO toDTO(FactureProduitEntity entity) {
        if (entity == null) return null;
        return new FactureProduitDTO(
                entity.getIdFactureProduit(),
                entity.getPrixVenteProduit(),
                entity.getQuantiteVenteProduit(),
                entity.getSousTotalVenteProduit(),
                entity.getMargeVenteProduit(),
                entity.getProduit() != null ? entity.getProduit().getIdProduit() : null,
                entity.getProduit() != null ? entity.getProduit().getNomProduit() : null
        );
    }

    public FactureProduitEntity toEntity(FactureProduitDTO dto) {
        if (dto == null) return null;
        return FactureProduitEntity.builder()
                .idFactureProduit(dto.idFactureProduit())
                .prixVenteProduit(dto.prixVenteProduit())
                .quantiteVenteProduit(dto.quantiteVenteProduit())
                .sousTotalVenteProduit(dto.sousTotalVenteProduit())
                .margeVenteProduit(dto.margeVenteProduit())
                .build();
    }
}
