package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.CategorieEntity;
import com.ydev.depenseNaka.model.ProduitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<ProduitEntity, String>, JpaSpecificationExecutor<ProduitEntity> {
    //nombre de produit
    @Override
    long count();

    //liste ordonnée
    List<ProduitEntity> findByNomProduitContainingIgnoreCaseOrderByNomProduitAsc(String nomProduit);

    //Affichage ordonné
    List<ProduitEntity> findAllByOrderByNomProduitAsc();

    //verification existence catégorie
    boolean existsByCategorieIdCategorie(String idCategorie);

    // Recherche exacte par nom (insensible à la casse)
    Optional<ProduitEntity> findByNomProduitIgnoreCase(String nomProduit);

    // Si vous cherchez la correspondance exacte du nom ET de la taille :
    Optional<ProduitEntity> findByNomProduitIgnoreCaseAndTailleProduitIgnoreCase(String nomProduit, String tailleProduit);

    //liste de produit actif et non actif avec JPQL
    @Query("SELECT p FROM ProduitEntity p LEFT JOIN FETCH p.categorie WHERE p.estActifProduit = false ORDER BY p.estActifProduit ASC")
    List<ProduitEntity> listeProduitInActif();

    @Query("SELECT p FROM ProduitEntity p LEFT JOIN FETCH p.categorie WHERE p.estActifProduit = true ORDER BY p.estActifProduit ASC")
    List<ProduitEntity> listeProduitActif();


}
