package com.ydev.depenseNaka.dto;

import lombok.Builder;

@Builder
public record UtilisateurProduitDTO(
        Long idUtilisateurProduit,
        String idProduit,
        String nomProduit,
        double quantiteCommandeProduit,
        double prixAchatCommandeProduit,
        double montantCommandeProduit
) {}
