package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.RoleUtilisateur;
import com.ydev.depenseNaka.model.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, String> {
    @Override
    long count();

    Optional<UtilisateurEntity> findByPseudoUtilisateur(String pseudoUtilisateur);

    boolean existsByPseudoUtilisateur(String pseudoUtilisateur);

    long countByRoleUtilisateur(RoleUtilisateur roleUtilisateur);
}
