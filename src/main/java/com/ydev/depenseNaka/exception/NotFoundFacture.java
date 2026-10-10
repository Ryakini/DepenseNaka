package com.ydev.depenseNaka.exception;

public class NotFoundFacture extends RuntimeException {
    public NotFoundFacture(String idFacture) {

        super("La facture N°: " + idFacture+" n'existe pas");
    }
}
