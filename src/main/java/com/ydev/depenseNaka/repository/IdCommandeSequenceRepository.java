package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.CommandeEntity;
import com.ydev.depenseNaka.model.IdCommandeSequence;
import org.springframework.data.repository.CrudRepository;

public interface IdCommandeSequenceRepository extends CrudRepository<IdCommandeSequence, String> {
}
