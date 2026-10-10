package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.CategorieDTO;
import com.ydev.depenseNaka.exception.CategorieExistant;
import com.ydev.depenseNaka.exception.ImpossibleDeleteCategorie;
import com.ydev.depenseNaka.exception.NotFoundCategorie;
import com.ydev.depenseNaka.mapper.CategorieMapper;
import com.ydev.depenseNaka.model.CategorieEntity;
import com.ydev.depenseNaka.model.ProduitEntity;
import com.ydev.depenseNaka.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    //creation
    @Transactional
    public CategorieDTO createCategorie(CategorieDTO categorieDTO) {
        //verification
        if (categorieRepository.existsByNomCategorieContainingIgnoreCase(categorieDTO.nomCategorie())) {
            throw new CategorieExistant(categorieDTO.nomCategorie());
        }

        //creation
        CategorieEntity categorieEntity = categorieMapper.toEntity(categorieDTO);
        categorieEntity.setNomCategorie(categorieDTO.nomCategorie().toUpperCase());
        categorieEntity.setDescriptionCategorie(categorieDTO.descriptionCategorie());

        CategorieEntity saved = categorieRepository.save(categorieEntity);
        return categorieMapper.toDTO(saved);
    }

    //mise à jour
    @Transactional
    public CategorieDTO updateCategorie(String id, CategorieDTO categorieDTO) {
        //verification
        CategorieEntity existCategorie = categorieRepository.findById(id)
                .orElseThrow( () -> new NotFoundCategorie(id));

        //mise à jour
        if (categorieDTO.nomCategorie() != null && !categorieDTO.nomCategorie().isBlank()) {
            existCategorie.setNomCategorie(categorieDTO.nomCategorie().toUpperCase());
        }

        if (categorieDTO.descriptionCategorie() != null && !categorieDTO.descriptionCategorie().isBlank()) {
            existCategorie.setDescriptionCategorie(categorieDTO.descriptionCategorie());
        }

        CategorieEntity update = categorieRepository.save(existCategorie);
        return categorieMapper.toDTO(update);
    }

    //suppression
    @Transactional
    public void deleteCategorie(String id) {
        //verification xistance
       CategorieEntity categorieEntity = categorieRepository.findById(id)
               .orElseThrow(() ->new NotFoundCategorie(id));

        if ( categorieEntity.getProduit() != null && !categorieEntity.getProduit().isEmpty()) {
                throw new ImpossibleDeleteCategorie(categorieEntity.getNomCategorie());
        }else {
            categorieRepository.deleteById(categorieEntity.getIdCategorie());
        }
    }

    //liste
    @Transactional(readOnly = true)
    public List<CategorieDTO> findAllCategorie() {
        return categorieRepository.findAllByOrderByNomCategorieAsc()
                .stream()
                .map(categorieMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategorieDTO> findAllCategorieSearch(String nomCategorie) {
        return categorieRepository.findByNomCategorieContainingIgnoreCaseOrderByNomCategorieAsc(nomCategorie)
                .stream()
                .map(categorieMapper::toDTO)
                .toList();
    }
}
