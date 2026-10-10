package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.model.ProduitEntity;
import com.ydev.depenseNaka.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProduitMapper {
    private final CategorieMapper categorieMapper;

    public ProduitDTO toDTO(ProduitEntity entity) {
        if (entity == null) return null;
        return ProduitDTO.builder()
                .idProduit(entity.getIdProduit())
                .nomProduit(entity.getNomProduit())
                .tailleProduit(entity.getTailleProduit())
                .dateCreationProduit(entity.getDateCreationProduit())
                .estDisponibleProduit(entity.isEstDisponibleProduit())
                .estActifProduit(entity.isEstActifProduit())
                .quantiteProduit(entity.getQuantiteProduit())
                .prixAchatProduit(entity.getPrixAchatProduit())
                .prixVenteProduit(entity.getPrixVenteProduit())
                .margeProduit(entity.getMargeProduit())
                .descriptionProduit(entity.getDescriptionProduit())
                .categorie(categorieMapper.toDTO(entity.getCategorie()))
                .build();
    }

    public ProduitEntity toEntity(ProduitDTO dto) {
        if (dto == null) return null;
        return ProduitEntity.builder()
                .idProduit(dto.idProduit())
                .nomProduit(dto.nomProduit())
                .tailleProduit(dto.tailleProduit())
                .dateCreationProduit(dto.dateCreationProduit())
                .estDisponibleProduit(dto.estDisponibleProduit())
                .estActifProduit(dto.estActifProduit())
                .quantiteProduit(dto.quantiteProduit())
                .prixAchatProduit(dto.prixAchatProduit())
                .prixVenteProduit(dto.prixVenteProduit())
                .margeProduit(dto.margeProduit())
                .descriptionProduit(dto.descriptionProduit())
                .categorie(categorieMapper.toEntity(dto.categorie()))
                .build();
    }
}
