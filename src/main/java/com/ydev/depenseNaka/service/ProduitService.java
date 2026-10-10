package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.exception.NotFoundCategorie;
import com.ydev.depenseNaka.exception.NotFoundProduit;
import com.ydev.depenseNaka.mapper.ProduitMapper;
import com.ydev.depenseNaka.model.CategorieEntity;
import com.ydev.depenseNaka.model.IdProduitSequence;
import com.ydev.depenseNaka.model.ProduitEntity;
import com.ydev.depenseNaka.repository.CategorieRepository;
import com.ydev.depenseNaka.repository.IdProduitSequenceRepository;
import com.ydev.depenseNaka.repository.ProduitRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final IdProduitSequenceRepository idProduitSequenceRepository;
    private final ProduitMapper produitMapper;

    //creation
    @Transactional
    public ProduitDTO createProduit(ProduitDTO produitDTO) {
        //verification exisence ctégorie

        CategorieEntity categorieEntity = null;
        if (produitDTO.categorie() != null && produitDTO.categorie().idCategorie() != null) {
            categorieEntity = categorieRepository.findById(produitDTO.categorie().idCategorie()).orElse(null);
        }

        if (categorieEntity == null && produitDTO.categorie() != null && produitDTO.categorie().nomCategorie() != null) {
            categorieEntity = categorieRepository.findByNomCategorieIgnoreCase(produitDTO.categorie().nomCategorie())
                    .orElseThrow(() -> new NotFoundCategorie("Catégorie introuvable : " + produitDTO.categorie().nomCategorie()));
        }

        //preparation de la date et categegorie
        LocalDate date = LocalDate.now();
        String currentMonthYear = date.format(DateTimeFormatter.ofPattern("MMyyyy"));
        String currentDayMonth = date.format(DateTimeFormatter.ofPattern("ddMM"));
        String year = String.valueOf(date.getYear());

        //initialisation du compteur  de sequence
        IdProduitSequence idProduitSequence = idProduitSequenceRepository.findById("id_sequence")
                .orElse(new IdProduitSequence("id_sequence", 0L, currentMonthYear));

        //modification par mois
        if (!currentMonthYear.equals(idProduitSequence.getLastMonthYear())){
            idProduitSequence.setLastMonthYear(currentMonthYear);
            idProduitSequence.setLastValue(0L);
        }

        //enregistrement des sequences
        idProduitSequence.setLastValue(idProduitSequence.getLastValue() + 1);
        idProduitSequenceRepository.save(idProduitSequence);

        //création de l'id personalisé

        String nomCat = categorieEntity.getNomCategorie();
        String prefixe = (nomCat != null && nomCat.length() >= 4)
                ? nomCat.substring(0, 4).toUpperCase()
                : "PROD";

        String customId = String.format("%s-%s/%04d", prefixe, currentDayMonth, idProduitSequence.getLastValue()); //CATE-ddMM/0001

        //creation de produit
        ProduitEntity entity = produitMapper.toEntity(produitDTO);
        entity.setCategorie(categorieEntity);
        entity.setIdProduit(customId);
        entity.setDateCreationProduit(date);
        entity.setMargeProduit(produitDTO.prixVenteProduit() - produitDTO.prixAchatProduit());
        entity.setEstActifProduit(true);

        ProduitEntity saved = produitRepository.save(entity);

        return produitMapper.toDTO(saved);
    }

    //mise à jour
    @Transactional
    public ProduitDTO updateProduit(String idProduit, ProduitDTO produitDto) {
        //verification
        ProduitEntity existingProduitEntity = produitRepository.findById(idProduit)
                .orElseThrow( ()-> new NotFoundProduit(idProduit));

        if (produitDto.nomProduit() != null && !produitDto.nomProduit().isBlank()) {
            existingProduitEntity.setNomProduit(produitDto.nomProduit());
        }

        if (produitDto.tailleProduit() != null && !produitDto.tailleProduit().isBlank()) {
            existingProduitEntity.setTailleProduit(produitDto.tailleProduit());
        }

        existingProduitEntity.setEstActifProduit(produitDto.estActifProduit());
        existingProduitEntity.setEstDisponibleProduit(produitDto.estDisponibleProduit());
        existingProduitEntity.setDateModifProduit(LocalDate.now());

        if (produitDto.quantiteProduit() != 0){
            existingProduitEntity.setQuantiteProduit(produitDto.quantiteProduit());
        }

        if (produitDto.prixVenteProduit()!= 0){
            existingProduitEntity.setPrixVenteProduit(produitDto.prixVenteProduit());
        }

        if (produitDto.prixAchatProduit() != 0){
            existingProduitEntity.setPrixAchatProduit(produitDto.prixAchatProduit());
        }

        double marge = produitDto.prixVenteProduit() - produitDto.prixAchatProduit();
        existingProduitEntity.setMargeProduit(produitDto.margeProduit() != 0 ? produitDto.margeProduit() : marge);


        if (produitDto.descriptionProduit() != null && !produitDto.descriptionProduit().isBlank()) {
            existingProduitEntity.setDescriptionProduit(produitDto.descriptionProduit());
        }

        ProduitEntity saved = produitRepository.save(existingProduitEntity);
        return produitMapper.toDTO(saved);
    }

    //delete
    @Transactional
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public void deleteProduit(String idProduit) {
        ProduitEntity produit = produitRepository.findById(idProduit)
                .orElseThrow(()-> new NotFoundProduit(idProduit));

       ProduitDTO dto =  produitMapper.toDTO(produit);

        //*****************a supprimmer en production
        produitRepository.delete(produit);

        //****************@ activer en prod
       // produit.setEstActifProduit(false); //softDelete
       // produitRepository.save(produit);

    }

    //update ou creation importation
    @Transactional
    public ProduitDTO sauvegarderOuMettreAJour(ProduitDTO nouveauDto) {
        Optional<ProduitEntity> produitExistant = Optional.empty();

        // 1. Recherche par ID si présent
        if (nouveauDto.idProduit() != null && !nouveauDto.idProduit().isBlank()) {
            produitExistant = produitRepository.findById(nouveauDto.idProduit());
        }

        // 2. Recherche par Nom si non trouvé par ID
        if (produitExistant.isEmpty() && nouveauDto.nomProduit() != null && !nouveauDto.nomProduit().isBlank()) {
            produitExistant = produitRepository.findByNomProduitIgnoreCase(nouveauDto.nomProduit());
        }

        if (produitExistant.isPresent()) {
            // --- MISE À JOUR + CUMUL ---
            ProduitEntity entity = produitExistant.get();

            // Calcul du cumul de la quantité
            //double nouvelleQuantite = entity.getQuantiteProduit() + nouveauDto.quantiteProduit();
            double nouvelleQuantite = nouveauDto.quantiteProduit();

            // Création du DTO de mise à jour avec l'ID existant et la quantité cumulée
            ProduitDTO dtoAjour = ProduitDTO.builder()
                    .idProduit(entity.getIdProduit())
                    .nomProduit(nouveauDto.nomProduit() != null ? nouveauDto.nomProduit() : entity.getNomProduit())
                    .tailleProduit(nouveauDto.tailleProduit() != null ? nouveauDto.tailleProduit() : entity.getTailleProduit())
                    .quantiteProduit(nouvelleQuantite)
                    .prixAchatProduit(nouveauDto.prixAchatProduit() > 0 ? nouveauDto.prixAchatProduit() : entity.getPrixAchatProduit())
                    .prixVenteProduit(nouveauDto.prixVenteProduit() > 0 ? nouveauDto.prixVenteProduit() : entity.getPrixVenteProduit())
                    .estActifProduit(entity.isEstActifProduit())
                    .estDisponibleProduit(nouvelleQuantite > 0)
                    .descriptionProduit(nouveauDto.descriptionProduit())
                    .categorie(nouveauDto.categorie() != null ? nouveauDto.categorie() : produitMapper.toDTO(entity).categorie())
                    .build();

            // Appeler updateProduit avec l'ID valide
            return updateProduit(entity.getIdProduit(), dtoAjour);

        } else {
            // --- CRÉATION ---
            // Appeler createProduit qui se charge de générer l'ID personnalisé (customId)
            nouveauDto.estActifProduit();
            return createProduit(nouveauDto);
        }
    }

    //afficher liste
    @Transactional(readOnly = true)
    public ProduitDTO getProduit(String idProduit) {
        return produitRepository.findById(idProduit)
                .map(produitMapper::toDTO)
                .orElseThrow(()-> new NotFoundProduit(idProduit));
    }

    public List<ProduitDTO> findListeProduitNom(String nomProduit) {
        return produitRepository.findByNomProduitContainingIgnoreCaseOrderByNomProduitAsc(nomProduit)
                .stream()
                .map(produitMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> findListeProduit() {
        return produitRepository.findAllByOrderByNomProduitAsc()
                .stream()
                .map(produitMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> findListeProduitActif() {
        return produitRepository.listeProduitActif()
                .stream()
                .map(produitMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> findListeProduitInActif() {
        return produitRepository.listeProduitInActif()
                .stream()
                .map(produitMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> findListeProduitById(String idProduit) {
        return (List<ProduitDTO>) produitRepository.findById(idProduit)
                .orElseThrow(()-> new NotFoundProduit(idProduit));
    }

    //compter nombre de produit
    public long compterProduit(){
        return produitRepository.count();
    }

    //recherche milticritère
    @Transactional(readOnly = true)
    public Page<ProduitDTO> findMulticriteriaProduit(String idproduit, String designation, String categorie,
                                                     String taille, Boolean estDisponible, Boolean estActif,
                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nomProduit").ascending());

        Page<ProduitEntity> entityPage = produitRepository.findAll((root, query, cb)->{
            List<Predicate> predicates = new ArrayList<>();

            // Solution anti-LazyInitializationException : Charger la catégorie avec FETCH si ce n'est pas un COUNT query
            if (Long.class != query.getResultType()) {
                root.fetch("categorie", jakarta.persistence.criteria.JoinType.LEFT);
            }

            if (idproduit!=null && !idproduit.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("idProduit")), "%"+idproduit.toLowerCase()+"%"));
            }

            if (designation!=null && !designation.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nomProduit")), "%"+designation.toLowerCase()+"%"));
            }

            if (categorie != null && !categorie.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("categorie").get("nomCategorie")), "%" + categorie.toLowerCase() + "%"));
            }

            if (taille!=null && !taille.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("tailleProduit")), "%"+taille.toLowerCase()+"%"));
            }

            // Filtre disponible : On applique SEULEMENT si la case est cochée
            if (Boolean.TRUE.equals(estDisponible)) {
                predicates.add(cb.equal(root.get("estDisponibleProduit"), true));
            }

            // Filtre actif : On applique SEULEMENT si la case est cochée
           /* if (Boolean.TRUE.equals(estActif)) {
                predicates.add(cb.equal(root.get("estActifProduit"), true));
            }*/
            if (estActif != null) {
                predicates.add(cb.equal(root.get("estActifProduit"), estActif));
            }

            //liste des produit actif seulement
            predicates.add(cb.equal(root.get("estActifProduit"), true));


            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        return entityPage.map(produitMapper::toDTO);
    }

}
