package com.ydev.depenseNaka.dto;

import com.ydev.depenseNaka.model.RoleUtilisateur;
import lombok.Builder;

@Builder
public record UtilisateurDTO(
        String idUtilisateur,
        String nomUtilisateur,
        String prenomUtilisateur,
        String pseudoUtilisateur,
        String mdpUtilisateur,
        String adresseUtilisateur,
        String telephoneUtilisateur,
        RoleUtilisateur roleUtilisateur
) {}
