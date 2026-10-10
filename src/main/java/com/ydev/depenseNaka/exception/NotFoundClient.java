package com.ydev.depenseNaka.exception;

public class NotFoundClient extends RuntimeException {
    public NotFoundClient(String idClient) {
        super("Le client N°:" + idClient + " n'existe pas");
    }
}
