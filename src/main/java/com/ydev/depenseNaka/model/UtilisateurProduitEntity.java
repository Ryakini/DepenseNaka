package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "utilisateur_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurProduitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur_produit")
    private Long idUtilisateurProduit;

    @Min(value = 0, message = "La quantité commandée doit être positive")
    private double quantiteCommandeProduit;

    @Min(value = 0, message = "Le prix d'achat doit être positif")
    private double prixAchatCommandeProduit;

    @Min(value = 0, message = "Le montant de la commande doit être positif")
    private double montantCommandeProduit;

    // Relation avec le Produit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_produit")
    private ProduitEntity produit;

    // Relation avec la Commande parente
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_commande")
    private CommandeEntity commande;

    // Relation avec l'Utilisateur ayant préparé/effectué la commande
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur")
    private UtilisateurEntity utilisateur;
}
