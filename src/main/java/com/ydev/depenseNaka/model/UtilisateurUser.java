package com.ydev.depenseNaka.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateur_user")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
public class UtilisateurUser extends UtilisateurEntity {

    public UtilisateurUser() {
        this.setRoleUtilisateur(RoleUtilisateur.UTILISATEUR);
    }
}
