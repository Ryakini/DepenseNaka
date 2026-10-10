package com.ydev.depenseNaka.exception;

public class ExistUtilisateur extends RuntimeException {
    public ExistUtilisateur(String pseudoUtilisateur) {
        super("Le pseudonime : " + pseudoUtilisateur + " existe dejà !");
    }
}
