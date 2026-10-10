package com.ydev.depenseNaka.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConnexionDTO(
        String idConnexion,
        String nomConnexion,
        LocalDateTime dateDebutConnexion,
        LocalDateTime dateFinConnexion,
        boolean isActive,
        String idUtilisateur,
        String pseudoUtilisateur
) {}
