package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeEntity {

    @Id
    @Column(name = "id_commande")
    private String idCommande;

    private LocalDate dateCreationCmd;
    private LocalDate dateModificationCmd;
    private LocalDate datePrevuArriveCmd;

    @NotNull(message = "Le fournisseur est obligatoire")
    private String fournisseurCmd;
    private String CommentaireCmd;

    @Enumerated(EnumType.STRING)
    private StatusCommande statusCmd;

    @PositiveOrZero
    private double montantCmd;

    //relation
    // Utilisateur qui a préparé/créé la commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_creation")
    private UtilisateurEntity utilisateurCreateur;

    // Utilisateur qui a modifié en dernier la commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_modification")
    private UtilisateurEntity utilisateurModificateur;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    private List<DepenseEntity> listeDepense = new ArrayList<>();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    private List<UtilisateurProduitEntity> lignesProduits = new ArrayList<>();

}
