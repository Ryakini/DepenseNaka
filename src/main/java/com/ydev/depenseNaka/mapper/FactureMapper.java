package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.FactureDTO;
import com.ydev.depenseNaka.dto.FactureProduitDTO;
import com.ydev.depenseNaka.model.FactureEntity;
import com.ydev.depenseNaka.model.FactureProduitEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FactureMapper {
    private final FactureProduitMapper factureProduitMapper;
    private final ClientMapper clientMapper;

    public FactureDTO toDTO(FactureEntity entity) {
        if (entity == null) return null;

        List<FactureProduitDTO> lignes = entity.getFactureProduitEntityList() != null
                ? entity.getFactureProduitEntityList().stream().map(factureProduitMapper::toDTO).toList()
                : Collections.emptyList();

        return new FactureDTO(
                entity.getIdFacture(),
                entity.getDateFacture(),
                entity.getDateModifFacture(),
                entity.getMontantFacture(),
                entity.getAvanceFacture(),
                entity.getResteFacture(),
                entity.getMargeFacture(),
                entity.getModePaiement(),
                entity.getStatus(),
                clientMapper.toDTO(entity.getClient()),
                lignes
        );
    }
}
