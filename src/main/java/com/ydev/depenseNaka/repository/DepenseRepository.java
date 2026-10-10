package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.DepenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepenseRepository extends JpaRepository<DepenseEntity, String> {

    @Override
    long count();

    // Méthode de recherche par ID de la commande associée
    List<DepenseEntity> findByCommandeIdCommande(String idCommande);

}
