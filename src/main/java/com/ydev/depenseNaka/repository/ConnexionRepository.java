package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.ConnexionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnexionRepository extends JpaRepository<ConnexionEntity, String> {
    //lister utilisateur selon sa date de connexion la plus récente
    List<ConnexionEntity> findByUtilisateurIdUtilisateurOrderByDateDebutConnexionDesc(String idUtilisateur);
    //liser liste d'un utilisateur encore active( Optionnel)
    List<ConnexionEntity> findByUtilisateurIdUtilisateurAndIsActiveTrue(String idUtilisateur);
    //compter le nombre de connexion d'un utilisateur
    long countByUtilisateurIdUtilisateur(String idUtilisateur);

    Optional<ConnexionEntity> findByIdConnexionAndIsActiveTrue(String idConnexion);

    Optional<ConnexionEntity> findFirstByUtilisateurIdUtilisateurAndIsActiveTrueOrderByDateDebutConnexionDesc(String idUtilisateur);

}
