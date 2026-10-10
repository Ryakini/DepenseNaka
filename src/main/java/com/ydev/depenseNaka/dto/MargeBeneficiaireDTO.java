package com.ydev.depenseNaka.dto;

public record MargeBeneficiaireDTO(
        String idCommande,
        String clientNom,
        double montantVente,
        double totalDepenses,
        double margeBeneficiaire,
        double pourcentageMarge
) {
}
