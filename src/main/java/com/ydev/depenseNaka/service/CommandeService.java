package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.CommandeDTO;
import com.ydev.depenseNaka.dto.DepenseDTO;
import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.dto.UtilisateurProduitDTO;
import com.ydev.depenseNaka.exception.NotFoundCommande;
import com.ydev.depenseNaka.exception.NotFoundProduit;
import com.ydev.depenseNaka.mapper.CommandeMapper;
import com.ydev.depenseNaka.mapper.DepenseMapper;
import com.ydev.depenseNaka.mapper.UtilisateurProduitMapper;
import com.ydev.depenseNaka.model.*;
import com.ydev.depenseNaka.repository.CommandeRepository;
import com.ydev.depenseNaka.repository.IdCommandeSequenceRepository;
import com.ydev.depenseNaka.repository.ProduitRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandeService {
    private final CommandeRepository commandeRepository;
    private final IdCommandeSequenceRepository idCommandeSequenceRepository;
    private final ProduitRepository produitRepository;
    private final CommandeMapper commandeMapper;
    private final UtilisateurProduitMapper utilisateurProduitMapper;
    private final DepenseMapper depenseMapper;
    private final DepenseService depenseService;

    //Preparatin
    // 1. PRÉPARATION / CRÉATION D'UNE COMMANDE
    public CommandeDTO preparerCommande(CommandeDTO dto, UtilisateurEntity utilisateur) {
        LocalDate date = LocalDate.now();
        String currentMonthYear = date.format(DateTimeFormatter.ofPattern("MMyyyy"));
        String monthDay = date.format(DateTimeFormatter.ofPattern("MMdd"));
        String year = String.valueOf(date.getYear());

        IdCommandeSequence sequence = idCommandeSequenceRepository.findById("id_sequence")
                .orElse(new IdCommandeSequence("id_sequence", 0L, currentMonthYear));

        if (!currentMonthYear.equals(sequence.getLastMonthYear())) {
            sequence.setLastMonthYear(currentMonthYear);
            sequence.setLastValue(0L);
        }

        sequence.setLastValue(sequence.getLastValue() + 1);
        idCommandeSequenceRepository.save(sequence);

        String customId = String.format("CMD-PRD-%s-%04d/%s", monthDay, sequence.getLastValue(), year);

        CommandeEntity commande = new CommandeEntity();
        commande.setIdCommande(customId);
        commande.setDateCreationCmd(date);
        commande.setFournisseurCmd(dto.fournisseurCmd());
        commande.setDatePrevuArriveCmd(dto.datePrevuArriveCmd());
        commande.setCommentaireCmd(dto.commentaireCmd());
        commande.setStatusCmd(StatusCommande.EN_ATTENTE);
        commande.setUtilisateurCreateur(utilisateur);

        double totalProduits = 0;

        // Association des lignes de produits
        if (dto.lignesProduits() != null) {
            for (UtilisateurProduitDTO ligneDto : dto.lignesProduits()) {
                ProduitEntity produit = produitRepository.findById(ligneDto.idProduit())
                        .orElseThrow(() -> new NotFoundProduit(ligneDto.idProduit()));

                UtilisateurProduitEntity ligne = utilisateurProduitMapper.toEntity(ligneDto);
                ligne.setIdUtilisateurProduit(null);
                ligne.setProduit(produit);
                ligne.setCommande(commande);

                double montantLigne = ligneDto.prixAchatCommandeProduit() * ligneDto.quantiteCommandeProduit();
                ligne.setMontantCommandeProduit(montantLigne);
                totalProduits += montantLigne;

                commande.getLignesProduits().add(ligne);
            }
        }

        // Association optionnelle des dépenses directes
        double totalDepenses = 0;
        if (dto.listeDepenses() != null) {
            for (DepenseDTO depenseDto : dto.listeDepenses()) {
                DepenseEntity depense = depenseMapper.toEntity(depenseDto);
                depense.setIdDepense(depenseService.genererCustomId());
                depense.setCommande(commande);
                depense.setDateCreationDepense(date);
                if (depense.getMontantDepense() == 0 && depense.getPrixDepense() > 0 && depense.getQuantiteDepense() > 0) {
                    depense.setMontantDepense(depense.getPrixDepense() * depense.getQuantiteDepense());
                }
                totalDepenses += depense.getMontantDepense();
                commande.getListeDepense().add(depense);
            }
        }

        commande.setMontantCmd(totalProduits + totalDepenses);

        CommandeEntity saved = commandeRepository.save(commande);
        return commandeMapper.toDTO(saved);
    }


    // 2. MODIFICATION D'UNE COMMANDE NON VALIDÉE
    public CommandeDTO modifierCommande(String idCommande, CommandeDTO dto, UtilisateurEntity utilisateur) {
        CommandeEntity commandeExistante = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new NotFoundCommande(idCommande));

        if (StatusCommande.VALIDEE.equals(commandeExistante.getStatusCmd())) {
            throw new IllegalStateException("Impossible de modifier une commande déjà validée !");
        }

        commandeExistante.setFournisseurCmd(dto.fournisseurCmd());
        commandeExistante.setDateModificationCmd(LocalDate.now());
        commandeExistante.setDatePrevuArriveCmd(dto.datePrevuArriveCmd());
        commandeExistante.setCommentaireCmd(dto.commentaireCmd());
        commandeExistante.setUtilisateurModificateur(utilisateur);

        // Reconstitution des lignes produits
        commandeExistante.getLignesProduits().clear();
        double totalProduits = 0;

        if (dto.lignesProduits() != null) {
            for (UtilisateurProduitDTO ligneDto : dto.lignesProduits()) {
                ProduitEntity produit = produitRepository.findById(ligneDto.idProduit())
                        .orElseThrow(() -> new NotFoundProduit(ligneDto.idProduit()));

                UtilisateurProduitEntity ligne = utilisateurProduitMapper.toEntity(ligneDto);
                ligne.setIdUtilisateurProduit(null);
                ligne.setProduit(produit);
                ligne.setCommande(commandeExistante);

                double montantLigne = ligneDto.prixAchatCommandeProduit() * ligneDto.quantiteCommandeProduit();
                ligne.setMontantCommandeProduit(montantLigne);
                totalProduits += montantLigne;

                commandeExistante.getLignesProduits().add(ligne);
            }
        }

        // Reconstitution des dépenses
        commandeExistante.getListeDepense().clear();
        double totalDepenses = 0;

        if (dto.listeDepenses() != null) {
            for (DepenseDTO depenseDto : dto.listeDepenses()) {
                DepenseEntity depense = depenseMapper.toEntity(depenseDto);

                if (depense.getIdDepense() == null || depense.getIdDepense().isEmpty()) {
                    depense.setIdDepense(depenseService.genererCustomId());
                }

                depense.setCommande(commandeExistante);
                if (depense.getDateCreationDepense() == null) {
                    depense.setDateCreationDepense(LocalDate.now());
                }
                depense.setDateModificationDepense(LocalDate.now());
                if (depense.getMontantDepense() == 0 && depense.getPrixDepense() > 0 && depense.getQuantiteDepense() > 0) {
                    depense.setMontantDepense(depense.getPrixDepense() * depense.getQuantiteDepense());
                }
                totalDepenses += depense.getMontantDepense();

                commandeExistante.getListeDepense().add(depense);
            }
        }

        commandeExistante.setMontantCmd(totalProduits + totalDepenses);

        CommandeEntity updated = commandeRepository.save(commandeExistante);
        return commandeMapper.toDTO(updated);
    }

    // 3. VALIDATION DE COMMANDE (INCRÉMENTATION DES STOCKS)
    public void validerCommande(String idCommande) {
        CommandeEntity commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new NotFoundCommande(idCommande));

        if (StatusCommande.VALIDEE.equals(commande.getStatusCmd())) {
            throw new IllegalStateException("Cette commande a déjà été validée !");
        }

        for (UtilisateurProduitEntity ligne : commande.getLignesProduits()) {
            ProduitEntity produit = ligne.getProduit();
            if (produit != null) {
                double nouveauStock = produit.getQuantiteProduit() + ligne.getQuantiteCommandeProduit();
                produit.setQuantiteProduit(nouveauStock);
                produit.setDateModifProduit(LocalDate.now());
                produitRepository.save(produit);
            }
        }

        commande.setStatusCmd(StatusCommande.VALIDEE);
        commande.setDateModificationCmd(LocalDate.now());
        commandeRepository.save(commande);
    }

    // 4. SUPPRESSION
    public void deleteCommandeById(String idCommande) {
        CommandeEntity commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new NotFoundCommande(idCommande));

        if (StatusCommande.VALIDEE.equals(commande.getStatusCmd())) {
            throw new IllegalStateException("Impossible de supprimer une commande déjà validée !");
        }

        commandeRepository.delete(commande);
    }

    //compter enregitrement
    public long countCommandeValide(){
        return commandeRepository.countCommandeValide();
    }

    public long countCommandeEnAttente(){
        return commandeRepository.countCommandeEnAttente();
    }

    //Liste
    /*
    @Transactional(readOnly = true)
    public List<CommandeEntity> findAll() {
        return commandeRepository.findAll();
    }

    //liste ordonnée par date
    @Transactional(readOnly = true)
    public List<CommandeEntity> findAllByOrderByDateCreationCmdDesc() {
        return commandeRepository.findAllByOrderByDateCreationCmdDesc();
    }
    */

    // 5. LISTE TOTALE
    @Transactional(readOnly = true)
    public List<CommandeDTO> findAll() {
        return commandeRepository.findAllByOrderByDateCreationCmdDesc().stream()
                .map(commandeMapper::toDTO)
                .toList();
    }

    // 6. RECHERCHE MULTICRITÈRE PAGINÉE
    @Transactional(readOnly = true)
    public Page<CommandeDTO> recherchePageableMulticriteria(
            String idCommande, String fournisseur, LocalDate dateCreation, LocalDate dateDebut,
            LocalDate dateFin, StatusCommande status, String description, int page, int size,
            String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CommandeEntity> entityPage = commandeRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

           /* if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("lignesProduits", JoinType.LEFT);
                root.fetch("listeDepense", JoinType.LEFT);
                query.distinct(true);
            }*/

            if (idCommande != null && !idCommande.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("idCommande")), "%" + idCommande.toLowerCase() + "%"));
            }
            if (fournisseur != null && !fournisseur.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("fournisseurCmd")), "%" + fournisseur.toLowerCase() + "%"));
            }
            if (dateCreation != null) {
                predicates.add(cb.equal(root.get("dateCreationCmd"), dateCreation));
            } else if (dateDebut != null && dateFin != null) {
                predicates.add(cb.between(root.get("dateCreationCmd"), dateDebut, dateFin));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("statusCmd"), status));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("commentaireCmd")), "%" + description.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return entityPage.map(commandeMapper::toDTO);
    }

}
