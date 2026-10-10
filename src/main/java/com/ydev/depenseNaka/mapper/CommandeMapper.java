package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.CommandeDTO;
import com.ydev.depenseNaka.dto.DepenseDTO;
import com.ydev.depenseNaka.dto.UtilisateurProduitDTO;
import com.ydev.depenseNaka.model.CommandeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandeMapper {
    private final UtilisateurProduitMapper utilisateurProduitMapper;
    private final DepenseMapper depenseMapper;

    public CommandeDTO toDTO(CommandeEntity entity) {
        if (entity == null) return null;

        List<UtilisateurProduitDTO> lignes = entity.getLignesProduits() != null
                ? entity.getLignesProduits().stream().map(utilisateurProduitMapper::toDTO).toList()
                : Collections.emptyList();

        List<DepenseDTO> depenses = entity.getListeDepense() != null
                ? entity.getListeDepense().stream().map(depenseMapper::toDTO).toList()
                : Collections.emptyList();

        String createur = entity.getUtilisateurCreateur() != null
                ? entity.getUtilisateurCreateur().getPseudoUtilisateur() : "-";

        String modificateur = entity.getUtilisateurModificateur() != null
                ? entity.getUtilisateurModificateur().getPseudoUtilisateur() : "-";

        return new CommandeDTO(
                entity.getIdCommande(),
                entity.getDateCreationCmd(),
                entity.getDateModificationCmd(),
                entity.getDatePrevuArriveCmd(),
                entity.getFournisseurCmd(),
                entity.getCommentaireCmd(),
                entity.getStatusCmd(),
                entity.getMontantCmd(),
                createur,
                modificateur,
                lignes,
                depenses
        );
    }

}
