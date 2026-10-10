package com.ydev.depenseNaka.dto;

import com.ydev.depenseNaka.model.StatusCommande;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CommandeDTO(
        String idCommande,
        LocalDate dateCreationCmd,
        LocalDate dateModificationCmd,
        LocalDate datePrevuArriveCmd,
        String fournisseurCmd,
        String commentaireCmd,
        StatusCommande statusCmd,
        double montantCmd,
        String utilisateurCreationPseudo,     // Pseudo du créateur
        String utilisateurModificationPseudo,
        List<UtilisateurProduitDTO> lignesProduits,
        List<DepenseDTO> listeDepenses
) {}
