package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ClientRepository extends JpaRepository<ClientEntity, String>, JpaSpecificationExecutor<ClientEntity> {
    @Override
    long count();

    //liste ordonée
    List<ClientEntity> findAllByOrderByNomClientAsc();

    //liste pour la recherche
    List<ClientEntity> findByNomClientContainingIgnoreCaseOrderByNomClientAsc(String nomClient);
}
