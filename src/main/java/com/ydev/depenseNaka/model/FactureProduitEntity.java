package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "facture_produit")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FactureProduitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactureProduit;

    @Min(0)
    private double prixVenteProduit;
    @Min(0)
    private double quantiteVenteProduit;
    @Min(0)
    private double sousTotalVenteProduit;
    @Min(0)
    private double margeVenteProduit;

    //relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facture")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FactureEntity facture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit")
    @EqualsAndHashCode.Exclude
    private ProduitEntity produit;


}
