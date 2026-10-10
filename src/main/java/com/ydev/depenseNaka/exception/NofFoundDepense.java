package com.ydev.depenseNaka.exception;

public class NofFoundDepense extends RuntimeException {
    public NofFoundDepense(String idDepense) {
        super("La dépense N°: "+idDepense+" n'existe pas");
    }
}
