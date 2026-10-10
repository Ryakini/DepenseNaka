package com.ydev.depenseNaka.exception;

public class NotFoundUtilisateur extends RuntimeException {
    public NotFoundUtilisateur(String idUtilisateur) {
        super("L'utilisateur ayant comme n°: " + idUtilisateur+" n'existe pas !");
    }
}
