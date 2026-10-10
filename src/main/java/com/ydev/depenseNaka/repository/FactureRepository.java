package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.FactureEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FactureRepository extends JpaRepository<FactureEntity, String>, JpaSpecificationExecutor<FactureEntity> {
    //compter enregistrement
    @Override
    long count();

    List<FactureEntity> findByDateFactureBetween(LocalDate dateDebut, LocalDate dateFin);

    //compter nombre facure non payé
    @Query("SELECT COUNT(f)" +
            "FROM FactureEntity f" +
            " WHERE f.status = 'NON_PAYE'")
    long countFactureNomPaye();

    //compter nombre facure payé partiel
    @Query("SELECT COUNT(f)" +
            "FROM FactureEntity f" +
            " WHERE f.status = 'PARTIEL'")
    long countFacturePayePartiel();

    //compter nombre facure payé
    @Query("SELECT COUNT(f)" +
            "FROM FactureEntity f" +
            " WHERE f.status = 'PAYE'")
    long countFacturePaye();

    //compter nombre facure non payé
    @Query("SELECT SUM(f.montantFacture)" +
            "FROM FactureEntity f" +
            " WHERE f.status = 'NON_PAYE'")
    Double countMontantFactureNomPaye();

    //montant facture paye par mois
    @Query("SELECT SUM(f.montantFacture) " +
            "FROM FactureEntity f " +
            "WHERE f.status = 'PAYE' " +
            "AND f.dateFacture >= :debutMois " +
            "AND f.dateFacture <= :finMois")
    Double montantfacturePayeParMoisEnCour(@Param("debutMois") LocalDate debutMois, @Param("finMois")LocalDate finMois);

    /*
    // 1. Somme du chiffre d'affaires groupée par date pour un mois et une année donnés
    @Query("SELECT f.dateFacture AS date, SUM(f.montantFacture) AS total " +
            "FROM FactureEntity f " +
            "WHERE YEAR(f.dateFacture) = :annee AND MONTH(f.dateFacture) = :mois " +
            "GROUP BY f.dateFacture " +
            "ORDER BY f.dateFacture ASC")
    List<Object[]> findVentesJournalieresDuMois(@Param("annee") int annee, @Param("mois") int mois);

    // 2. Top N des produits les plus vendus sur le mois en cours
    @Query("SELECT fp.produit.nomProduit, SUM(fp.quantiteVenteProduit) AS totalQuantite, SUM(fp.sousTotalVenteProduit) AS totalCA " +
            "FROM FactureProduitEntity fp " +
            "WHERE YEAR(fp.facture.dateFacture) = :annee AND MONTH(fp.facture.dateFacture) = :mois " +
            "GROUP BY fp.produit.idProduit, fp.produit.nomProduit " +
            "ORDER BY totalQuantite DESC")
    List<Object[]> findTopProduitsDuMois(@Param("annee") int annee, @Param("mois") int mois, Pageable pageable);
    */
/*
    @Query("SELECT f.dateFacture AS date, SUM(f.montantFacture) AS total " +
            "FROM FactureEntity f " +
            "WHERE FUNCTION('strftime', '%Y', f.dateFacture) = :annee " +
            "AND FUNCTION('strftime', '%m', f.dateFacture) = :mois " +
            "GROUP BY f.dateFacture " +
            "ORDER BY f.dateFacture ASC")
    List<Object[]> findVentesJournalieresDuMois(@Param("annee") String annee, @Param("mois") String mois);

    @Query("SELECT fp.produit.nomProduit, SUM(fp.quantiteVenteProduit) AS totalQuantite, SUM(fp.sousTotalVenteProduit) AS totalCA " +
            "FROM FactureProduitEntity fp " +
            "WHERE FUNCTION('strftime', '%Y', fp.facture.dateFacture) = :annee " +
            "AND FUNCTION('strftime', '%m', fp.facture.dateFacture) = :mois " +
            "GROUP BY fp.produit.idProduit, fp.produit.nomProduit " +
            "ORDER BY totalQuantite DESC")
    List<Object[]> findTopProduitsDuMois(@Param("annee") String annee, @Param("mois") String mois, Pageable pageable);

 */

    @Query("SELECT f.dateFacture AS date, SUM(f.montantFacture) AS total " +
            "FROM FactureEntity f " +
            "WHERE f.dateFacture BETWEEN :debut AND :fin " +
            "GROUP BY f.dateFacture " +
            "ORDER BY f.dateFacture ASC")
    List<Object[]> findVentesJournalieresParPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT fp.produit.nomProduit, SUM(fp.quantiteVenteProduit) AS totalQuantite, SUM(fp.sousTotalVenteProduit) AS totalCA " +
            "FROM FactureProduitEntity fp " +
            "WHERE fp.facture.dateFacture BETWEEN :debut AND :fin " +
            "GROUP BY fp.produit.idProduit, fp.produit.nomProduit " +
            "ORDER BY totalQuantite DESC")
    List<Object[]> findTopProduitsParPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin, Pageable pageable);

}
