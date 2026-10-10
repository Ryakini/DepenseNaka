package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.DepenseDTO;
import com.ydev.depenseNaka.exception.NofFoundDepense;
import com.ydev.depenseNaka.exception.NotFoundCommande;
import com.ydev.depenseNaka.mapper.DepenseMapper;
import com.ydev.depenseNaka.model.CommandeEntity;
import com.ydev.depenseNaka.model.DepenseEntity;
import com.ydev.depenseNaka.model.IdDepenseSequenceEntity;
import com.ydev.depenseNaka.repository.CommandeRepository;
import com.ydev.depenseNaka.repository.DepenseRepository;
import com.ydev.depenseNaka.repository.IdDepenseSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepenseService {
    private final DepenseRepository depenseRepository;
    private final IdDepenseSequenceRepository idDepenseSequenceRepository;
    private final DepenseMapper  depenseMapper;
    private final CommandeRepository commandeRepository;

    //creation
    public DepenseDTO createDepense(DepenseDTO depenseDTO) {
        //creation id depnse personnalisée
        LocalDate date = LocalDate.now();
        String currentMonthYear = date.format(DateTimeFormatter.ofPattern("MMyyyy"));
        String currentDayMonth = date.format(DateTimeFormatter.ofPattern("ddMM"));
        String year = String.valueOf(date.getYear());

        IdDepenseSequenceEntity idDepenseSequenceEntity = idDepenseSequenceRepository.findById("id_sequence")
                .orElse(new IdDepenseSequenceEntity("id_sequence", 0L, currentMonthYear));

        if (!currentMonthYear.equals(idDepenseSequenceEntity.getLastMonthYear())) {
            idDepenseSequenceEntity.setLastMonthYear(currentMonthYear);
            idDepenseSequenceEntity.setLastValue(0L);
        }

        idDepenseSequenceEntity.setLastValue(idDepenseSequenceEntity.getLastValue() + 1);
        idDepenseSequenceRepository.save(idDepenseSequenceEntity);

        String customId = String.format("DEP-%s/%04d",currentMonthYear, idDepenseSequenceEntity.getLastValue());

        DepenseEntity entity = depenseMapper.toEntity(depenseDTO);
        entity.setIdDepense(customId);
        entity.setDateCreationDepense(date);

        // Association optionnelle de la commande
        if (depenseDTO.idCommande() != null && !depenseDTO.idCommande().isBlank()) {
            CommandeEntity commande = commandeRepository.findById(depenseDTO.idCommande())
                    .orElseThrow(() -> new NotFoundCommande(depenseDTO.idCommande()));
            entity.setCommande(commande);
        }

        // Calcul automatique du montant si prix et quantité sont renseignés
        if (entity.getPrixDepense() > 0 && entity.getQuantiteDepense() > 0) {
            entity.setMontantDepense(entity.getPrixDepense() * entity.getQuantiteDepense());
        }

        DepenseEntity saved = depenseRepository.save(entity);
        return depenseMapper.toDTO(saved);
    }

    // MISE À JOUR
    public DepenseDTO updateDepense(String idDepense, DepenseDTO dto) {
        DepenseEntity existDepense = depenseRepository.findById(idDepense)
                .orElseThrow(() -> new NofFoundDepense(idDepense));

        if (dto.libelleDepense() != null && !dto.libelleDepense().isBlank()) {
            existDepense.setLibelleDepense(dto.libelleDepense());
        }

        if (dto.prixDepense() >= 0) {
            existDepense.setPrixDepense(dto.prixDepense());
        }

        if (dto.quantiteDepense() >= 0) {
            existDepense.setQuantiteDepense(dto.quantiteDepense());
        }

        // Recalcul du montant
        if (existDepense.getPrixDepense() > 0 && existDepense.getQuantiteDepense() > 0) {
            existDepense.setMontantDepense(existDepense.getPrixDepense() * existDepense.getQuantiteDepense());
        } else if (dto.montantDepense() > 0) {
            existDepense.setMontantDepense(dto.montantDepense());
        }

        if (dto.descriptionDepense() != null) {
            existDepense.setDescriptionDepense(dto.descriptionDepense());
        }

        if (dto.idCommande() != null && !dto.idCommande().isBlank()) {
            CommandeEntity commande = commandeRepository.findById(dto.idCommande())
                    .orElseThrow(() -> new NotFoundCommande(dto.idCommande()));
            existDepense.setCommande(commande);
        }

        existDepense.setDateModificationDepense(LocalDate.now());

        DepenseEntity updated = depenseRepository.save(existDepense);
        return depenseMapper.toDTO(updated);
    }

    //suppression
    //@Transactional
    public void deleteDepense(String idDepense) {
        DepenseEntity depense = depenseRepository.findById(idDepense)
                .orElseThrow(()-> new NofFoundDepense(idDepense));

        depenseRepository.delete(depense);
    }

    //liste
    @Transactional(readOnly = true)
    public List<DepenseDTO> findAllDepense() {
        return depenseRepository.findAll()
                .stream()
                .map(depenseMapper::toDTO)
                .toList();
    }

    // LISTE PAR COMMANDE
    @Transactional(readOnly = true)
    public List<DepenseDTO> findByCommande(String idCommande) {
        return depenseRepository.findByCommandeIdCommande(idCommande).stream()
                .map(depenseMapper::toDTO)
                .toList();
    }

    //Générer un identifiant
    public String genererCustomId() {
        LocalDate date = LocalDate.now();
        String currentMonthYear = date.format(DateTimeFormatter.ofPattern("MMyyyy"));

        IdDepenseSequenceEntity sequence = idDepenseSequenceRepository.findById("id_sequence")
                .orElse(new IdDepenseSequenceEntity("id_sequence", 0L, currentMonthYear));

        if (!currentMonthYear.equals(sequence.getLastMonthYear())) {
            sequence.setLastMonthYear(currentMonthYear);
            sequence.setLastValue(0L);
        }

        sequence.setLastValue(sequence.getLastValue() + 1);
        idDepenseSequenceRepository.save(sequence);

        return String.format("DEP-%s/%04d", currentMonthYear, sequence.getLastValue());
    }
}
