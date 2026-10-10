package com.ydev.depenseNaka.session;

import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.model.RoleUtilisateur;
import com.ydev.depenseNaka.model.UtilisateurEntity;

public class UserSession {
    private static UserSession instance;

    private final String idConnexion;
    private final String idUtilisateur;
    private final String pseudo;
    private final RoleUtilisateur role;

    private UserSession(String idConnexion, String idUtilisateur, String pseudo, RoleUtilisateur role) {
        this.idConnexion = idConnexion;
        this.idUtilisateur = idUtilisateur;
        this.pseudo = pseudo;
        this.role = role;
    }

    public static void createSession(UtilisateurDTO utilisateur, String idConnexion) {
        instance = new UserSession(
                idConnexion,
                utilisateur.idUtilisateur(),
                utilisateur.pseudoUtilisateur(),
                utilisateur.roleUtilisateur()
        );
    }

    //creation surcharge
    public static void createSession(String idConnexion,String idUtilisateur, String pseudo, RoleUtilisateur role) {
        instance = new UserSession(idConnexion,idUtilisateur,pseudo,role);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void clearSession() {
        instance = null;
    }
    public String getIdConnexion() {
        return idConnexion;
    }

    public String getIdUtilisateur() { return idUtilisateur; }
    public String getPseudo() { return pseudo; }
    public RoleUtilisateur getRole() { return role; }
}
