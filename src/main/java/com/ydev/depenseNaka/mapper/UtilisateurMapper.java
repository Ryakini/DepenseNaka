package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.model.RoleUtilisateur;
import com.ydev.depenseNaka.model.UtilisateurEntity;
import com.ydev.depenseNaka.model.UtilisateurUser;
import com.ydev.depenseNaka.model.UtilisteurAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    //private UtilisateurDTO utilisateurDTO;
    public UtilisateurMapper() {
    }

    public UtilisateurDTO toDTO(UtilisateurEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UtilisateurDTO(
                entity.getIdUtilisateur(),
                entity.getNomUtilisateur(),
                entity.getPrenomUtilisateur(),
                entity.getPseudoUtilisateur(),
                null, // Ne pas exposer le mot de passe dans le DTO retourné
                entity.getAdresseUtilisateur(),
                entity.getTelephoneUtilisateur(),
                entity.getRoleUtilisateur()
        );
    }

    public UtilisateurEntity toEntity(UtilisateurDTO dto) {
        if (dto == null) return null;

        UtilisateurEntity entity;

        // Instanciation de la classe fille selon le rôle
        if (dto.roleUtilisateur() == RoleUtilisateur.ADMINISTRATEUR) {
            entity = new UtilisteurAdmin();
        } else {
            entity = new UtilisateurUser();
        }

        // Affectation des propriétés communes
        entity.setIdUtilisateur(dto.idUtilisateur());
        entity.setNomUtilisateur(dto.nomUtilisateur());
        entity.setPrenomUtilisateur(dto.prenomUtilisateur());
        entity.setPseudoUtilisateur(dto.pseudoUtilisateur());
        entity.setMdpUtilisateur(dto.mdpUtilisateur());
        entity.setAdresseUtilisateur(dto.adresseUtilisateur());
        entity.setTelephoneUtilisateur(dto.telephoneUtilisateur());

        return entity;
    }
}
