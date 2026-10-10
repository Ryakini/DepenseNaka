package com.ydev.depenseNaka.exception;

public class ImpossibleDeleteCategorie extends RuntimeException {
    public ImpossibleDeleteCategorie(String nomCategorie) {

        super("Impossible de supprimer la categorie : "+ nomCategorie+" car elle est rattachée au(x) produit(s)");
    }
}
