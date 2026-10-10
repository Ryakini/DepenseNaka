package com.ydev.depenseNaka.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateur_admin")
@Inheritance(strategy = InheritanceType.JOINED)
public class UtilisteurAdmin extends UtilisateurEntity {

    public UtilisteurAdmin(){
        this.setRoleUtilisateur(RoleUtilisateur.ADMINISTRATEUR);
    }
}
