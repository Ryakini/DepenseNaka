package com.ydev.depenseNaka.dto;

import lombok.Builder;

@Builder
public record MargeMensuelDTO(
        String moisAnnee,       // Format : "MM/YYYY" ou "Janvier 2026"
        double totalVentes,
        double totalDepenses,
        double margeNette,
        double pourcentageMarge
) {}
