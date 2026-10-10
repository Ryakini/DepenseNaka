package com.ydev.depenseNaka.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "produit")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProduitEntity {

    @Id
    @Column(name = "id_produit")
    private String idProduit; //Cat-ddMM-0001

    @NotBlank(message = "La designation est obligatoire")
    private String nomProduit;

    private String tailleProduit;

    @NotNull
    private LocalDate dateCreationProduit;
    private LocalDate dateModifProduit;

    boolean estDisponibleProduit; //Disponible, nom disponible
    boolean estActifProduit;

    @Min(0)
    private double quantiteProduit;
    @PositiveOrZero
    private double prixAchatProduit;
    @PositiveOrZero
    private double prixVenteProduit;
    @Min(0)
    private double margeProduit;

    private String descriptionProduit;

    //relation
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorie")
    private CategorieEntity categorie;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<FactureProduitEntity> factureProduitEntitiesroduit = new HashSet<>();
}
