package com.ydev.depenseNaka.exception;

public class ImpossibleDeleteClient extends RuntimeException {
    public ImpossibleDeleteClient() {
        super("Suppression impossible car ce client est rattaché à une facture");
    }
}
