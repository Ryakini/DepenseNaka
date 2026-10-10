package com.ydev.depenseNaka.dto;

public record MeilleurClientDTO(
        String idClient,
        String nomComplet,
        long nombreCommandes,
        double totalDepense
) {}
