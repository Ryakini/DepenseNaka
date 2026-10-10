package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private String idCategorie;

    @NotBlank(message = "La designation de catégorie est obligatoire")
    private String nomCategorie;
    private String descriptionCategorie;

    //relation

    @OneToMany(mappedBy = "categorie", cascade ={CascadeType.MERGE,CascadeType.PERSIST} )
    @ToString.Exclude //pour eviter LazyInitialisationException
    private List<ProduitEntity> produit = new ArrayList<>();
}
