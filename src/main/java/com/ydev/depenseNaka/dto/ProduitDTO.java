package com.ydev.depenseNaka.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProduitDTO(
        String idProduit,
        String nomProduit,
        String tailleProduit,
        LocalDate dateCreationProduit,
        boolean estDisponibleProduit,
        boolean estActifProduit,
        double quantiteProduit,
        double prixAchatProduit,
        double prixVenteProduit,
        double margeProduit,
        String descriptionProduit,
        CategorieDTO categorie
) {
}
