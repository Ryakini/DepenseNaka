package com.ydev.depenseNaka.exception;

public class ImpossibleDeleteUtilisateur extends RuntimeException {
    public ImpossibleDeleteUtilisateur(String idUtilisateur) {
        super("Impossible de  supprimer l'utilisateur :" + idUtilisateur+" , car il est lié à une commande !");
    }
}
