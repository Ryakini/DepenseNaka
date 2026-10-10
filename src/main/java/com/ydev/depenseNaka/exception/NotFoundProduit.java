package com.ydev.depenseNaka.exception;

public class NotFoundProduit extends RuntimeException {
    public NotFoundProduit(String idproduit) {

        super("Le produit: " + idproduit + " n'existe pas");
    }
}
