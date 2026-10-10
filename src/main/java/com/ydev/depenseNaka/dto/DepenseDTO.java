package com.ydev.depenseNaka.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DepenseDTO(
        String idDepense,
        LocalDate dateCreationDepense,
        LocalDate dateModificationDepense,
        String libelleDepense,
        double prixDepense,
        double quantiteDepense,
        double montantDepense,
        String descriptionDepense,
        String idCommande
) {}
