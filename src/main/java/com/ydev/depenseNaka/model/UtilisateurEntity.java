package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class UtilisateurEntity {
    @Id
    private String idUtilisateur;
    @NotNull(message = "Le nom de l'utilisateur est obligatoire")
    private String nomUtilisateur;
    private String prenomUtilisateur;
    @NotNull(message = "Le pseudonime est obligatoire")
    @Column(unique = true)
    private String pseudoUtilisateur;
    @NotNull(message = "Le mot de passe est obligatoire")
    private String mdpUtilisateur;
    private String adresseUtilisateur;
    private String telephoneUtilisateur;

    @Enumerated(EnumType.STRING)
    @NotNull
    private RoleUtilisateur roleUtilisateur;

    //relation
    @OneToMany(mappedBy = "utilisateurCreateur")
    @Fetch(FetchMode.SUBSELECT)
    @EqualsAndHashCode.Exclude
    private List<CommandeEntity> commandes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private List<UtilisateurProduitEntity> utilisateurs = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private List<ConnexionEntity> connexions = new ArrayList<>();

}
