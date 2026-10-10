package com.ydev.depenseNaka.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record EvolutionStatDTO(
        LocalDate date,
        long nombreCommandes,
        long nouveauxClients,
        double totalVentes
) {
}
