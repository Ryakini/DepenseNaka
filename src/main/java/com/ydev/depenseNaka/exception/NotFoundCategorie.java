package com.ydev.depenseNaka.exception;

public class NotFoundCategorie extends RuntimeException {
    public NotFoundCategorie(String idCategorie) {

        super("Le numéro de catégorie: "+ idCategorie +" n'existe pas");
    }
}
