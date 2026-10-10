package com.ydev.depenseNaka.repository;

import com.ydev.depenseNaka.model.IdClientSequence;
import com.ydev.depenseNaka.model.IdDepenseSequenceEntity;
import org.springframework.data.repository.CrudRepository;

public interface IdDepenseSequenceRepository extends CrudRepository<IdDepenseSequenceEntity, String> {
}
