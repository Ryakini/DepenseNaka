package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connexion")//pour enregistrer l'historique de connexion d'un utiliateur
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConnexionEntity {
    @Id
    private String idConnexion;
    private String nomConnexion;
    private LocalDateTime dateDebutConnexion;
    private LocalDateTime dateFinConnexion;
    boolean isActive;

    //relation avec utilisatur
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private UtilisateurEntity utilisateur;
}
