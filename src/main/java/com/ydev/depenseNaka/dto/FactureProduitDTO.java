package com.ydev.depenseNaka.dto;

import lombok.Builder;

@Builder
public record FactureProduitDTO(
        Long idFactureProduit,
        double prixVenteProduit,
        double quantiteVenteProduit,
        double sousTotalVenteProduit,
        double margeVenteProduit,
        String idProduit,
        String nomProduit
) {
}
