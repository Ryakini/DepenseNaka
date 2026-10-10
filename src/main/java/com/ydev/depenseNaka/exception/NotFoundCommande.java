package com.ydev.depenseNaka.exception;

public class NotFoundCommande extends RuntimeException {
    public NotFoundCommande(String idCommande) {

        super("La commande N°: " + idCommande + " n'existe pas");
    }

    public NotFoundCommande() {

        super("Interdiction : Impossible de supprimer une commande déjà validée !");
    }
}
