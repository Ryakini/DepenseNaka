package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.CategorieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<CategorieEntity, String> {

    // Recherche exacte par nom (insensible à la casse)
    Optional<CategorieEntity> findByNomCategorieIgnoreCase(String nomCategorie);

    // Recherche par mot-clé (retourne une liste de résultats)
    List<CategorieEntity> findByNomCategorieContainingIgnoreCaseOrderByNomCategorieAsc(String nomCategorie);

    // Récupérer toutes les catégories triées par nom
    List<CategorieEntity> findAllByOrderByNomCategorieAsc();


    // Vérification d'existence (remplace Containig par Containing)
    boolean existsByNomCategorieContainingIgnoreCase(String nomCategorie);
}
