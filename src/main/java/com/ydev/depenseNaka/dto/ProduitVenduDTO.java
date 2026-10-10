package com.ydev.depenseNaka.dto;

public class ProduitVenduDTO {
    private String nomProduit;
    private Long quantiteVendue;
    private Double montantTotal;

    public ProduitVenduDTO(String nomProduit, Long quantiteVendue, Double montantTotal) {
        this.nomProduit = nomProduit;
        this.quantiteVendue = quantiteVendue;
        this.montantTotal = montantTotal;
    }

    public String getNomProduit() { return nomProduit; }
    public Long getQuantiteVendue() { return quantiteVendue; }
    public Double getMontantTotal() { return montantTotal; }
}
