package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.CommandeEntity;
import com.ydev.depenseNaka.model.IdClientSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CommandeRepository extends JpaRepository<CommandeEntity, String>, JpaSpecificationExecutor<CommandeEntity> {
    @Override
    long count();

    //liste
    List<CommandeEntity> findAllByOrderByDateCreationCmdDesc();
    List<CommandeEntity> findByDateCreationCmdBetween(LocalDate dateDebut, LocalDate dateFin);

    //commande validée
    @Query("SELECT COUNT(c) FROM CommandeEntity c WHERE c.statusCmd ='VALIDEE' ")
    long countCommandeValide();

    //commande validée
    @Query("SELECT COUNT(c) FROM CommandeEntity c WHERE c.statusCmd ='EN_ATTENTE' ")
    long countCommandeEnAttente();

}
