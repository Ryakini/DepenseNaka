package com.ydev.depenseNaka.exception;

public class CategorieExistant extends RuntimeException {

    public CategorieExistant(String categorie) {

        super("La catégorie: "+ categorie +" existe dejà dans la base de donée");
    }
}
