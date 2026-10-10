package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.FactureDTO;
import com.ydev.depenseNaka.dto.FactureProduitDTO;
import com.ydev.depenseNaka.exception.NotFoundClient;
import com.ydev.depenseNaka.exception.NotFoundFacture;
import com.ydev.depenseNaka.exception.NotFoundProduit;
import com.ydev.depenseNaka.mapper.FactureMapper;
import com.ydev.depenseNaka.mapper.FactureProduitMapper;
import com.ydev.depenseNaka.model.*;
import com.ydev.depenseNaka.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final IdSequenceFactureRepository idSequenceFactureRepository;
    private final FactureProduitRepository factureProduitRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final FactureMapper factureMapper;
    private final FactureProduitMapper factureProduitMapper;


    // CRÉATION DE FACTURE (Retourne FactureDTO, prend FactureDTO)
    @Transactional
    public FactureDTO createFacture(FactureDTO dto) {
        //verification client
        if(dto == null || dto.client().idClient() == null){
            throw new NotFoundFacture("La facture doit être ratché à un client");
        }

        ClientEntity existingClient = clientRepository.findById(dto.client().idClient())
                .orElseThrow(() -> new NotFoundClient(dto.client().idClient()));


        LocalDate date = LocalDate.now();
        String currentMonthYear = date.format(DateTimeFormatter.ofPattern("MMyyyy"));
        String monthday = date.format(DateTimeFormatter.ofPattern("MMdd"));
        String year = String.valueOf(date.getYear());

        IdsequenceFacture idsequenceFacture = idSequenceFactureRepository.findById("id_sequence")
                .orElse(new IdsequenceFacture("id_sequence", 0L, currentMonthYear));

        if (!currentMonthYear.equals(idsequenceFacture.getLastMonthYear())) {
            idsequenceFacture.setLastValue(0L);
            idsequenceFacture.setLastMonthYear(currentMonthYear);
        }

        idsequenceFacture.setLastValue(idsequenceFacture.getLastValue() + 1);
        idSequenceFactureRepository.save(idsequenceFacture);

        String customId = String.format("FAC-%s-%04d/%s", monthday, idsequenceFacture.getLastValue(), year);

        FactureEntity facture = new FactureEntity();
        facture.setIdFacture(customId);
        facture.setDateFacture(date);
        facture.setClient(existingClient);
        facture.setModePaiement(dto.modePaiement());
        facture.setAvanceFacture(dto.avanceFacture());

        double cumulSoutotal = 0;
        double cumulMarge = 0;

        if (dto.factureProduitEntityList() != null && !dto.factureProduitEntityList().isEmpty()) {
            for (FactureProduitDTO ligneDto : dto.factureProduitEntityList()) {
                ProduitEntity prod = produitRepository.findById(ligneDto.idProduit())
                        .orElseThrow(() -> new NotFoundProduit(ligneDto.idProduit()));

                if (prod.getQuantiteProduit() >= ligneDto.quantiteVenteProduit()) {
                    FactureProduitEntity ligne = factureProduitMapper.toEntity(ligneDto);
                    ligne.setProduit(prod);
                    ligne.setPrixVenteProduit(prod.getPrixVenteProduit());
                    ligne.setQuantiteVenteProduit(ligneDto.quantiteVenteProduit());

                    double sousTotal = prod.getPrixVenteProduit() * ligneDto.quantiteVenteProduit();
                    double soustotalMarge = ligneDto.quantiteVenteProduit() * prod.getMargeProduit();

                    ligne.setSousTotalVenteProduit(sousTotal);
                    ligne.setMargeVenteProduit(soustotalMarge);

                    cumulSoutotal += sousTotal;
                    cumulMarge += soustotalMarge;

                    ligne.setFacture(facture);
                    facture.getFactureProduitEntityList().add(ligne);

                    // Mise à jour du stock
                    prod.setQuantiteProduit(prod.getQuantiteProduit() - ligneDto.quantiteVenteProduit());
                    produitRepository.save(prod);
                }
            }
        }

        facture.setMontantFacture(cumulSoutotal);
        facture.setMargeFacture(cumulMarge);
        facture.setResteFacture(Math.max(0, cumulSoutotal - dto.avanceFacture()));

        double reste = cumulSoutotal - dto.avanceFacture();
        if (reste <= 0) {
            facture.setStatus(StatusPaiement.PAYE);
        } else if (facture.getMontantFacture() == facture.getResteFacture()) {
            facture.setStatus(StatusPaiement.NON_PAYE);
        } else {
            facture.setStatus(StatusPaiement.PARTIEL);
        }

        FactureEntity saved = factureRepository.save(facture);
        return factureMapper.toDTO(saved);
    }

    //mise à jour
    /*
    @Transactional
    public FactureEntity updateFacture(String idFacture, FactureEntity facture) {
        //verification existence facture
        FactureEntity existFacture = factureRepository.findById(idFacture)
                .orElseThrow(()-> new NotFoundFacture(idFacture));

        //restituer les quantités dans la pécédente facture
        for (FactureProduitEntity ancienneLigne : existFacture.getFactureProduitEntityList()){
            ProduitEntity produitARendre = ancienneLigne.getProduit();
            double quantiteARendre = ancienneLigne.getQuantiteVenteProduit();

            produitARendre.setQuantiteProduit(quantiteARendre + produitARendre.getQuantiteProduit());
            produitRepository.save(produitARendre);
        }

        //nettoyage de l'ancienne facture
        if (existFacture.getFactureProduitEntityList() != null && !existFacture.getFactureProduitEntityList().isEmpty()) {
            existFacture.getFactureProduitEntityList().clear();
        }

        //nouvelle ligne
        double nouveauCumulSoutotal = 0;
        double nouveauCumulMarge = 0;

        for (FactureProduitEntity nouvelleLigne : facture.getFactureProduitEntityList()){
            ProduitEntity nouveauProduit = produitRepository.findById(nouvelleLigne.getProduit().getIdProduit())
                    .orElseThrow(()-> new NotFoundProduit(nouvelleLigne.getProduit().getIdProduit()));

            double qteDemandee = nouvelleLigne.getQuantiteVenteProduit();

            if (nouveauProduit.getQuantiteProduit() != 0){
                nouvelleLigne.setPrixVenteProduit(nouveauProduit.getPrixVenteProduit());
                double sousTotal = nouveauProduit.getPrixVenteProduit() * qteDemandee;
                nouvelleLigne.setSousTotalVenteProduit(sousTotal);
                double soustotalMarge  = qteDemandee * nouveauProduit.getMargeProduit();

                nouveauCumulSoutotal += sousTotal;
                nouveauCumulMarge += soustotalMarge;

                nouvelleLigne.setFacture(existFacture);
                existFacture.getFactureProduitEntityList().add(nouvelleLigne);

                factureProduitRepository.save(nouvelleLigne);

                //modificationstock en base
                nouveauProduit.setQuantiteProduit(nouveauProduit.getQuantiteProduit() - qteDemandee);
                produitRepository.save(nouveauProduit);
            }
        }

        existFacture.setDateModifFacture(LocalDate.now());
        existFacture.setMontantFacture(nouveauCumulSoutotal);
        existFacture.setMargeFacture(nouveauCumulMarge);
        existFacture.setAvanceFacture(facture.getAvanceFacture());
        existFacture.setResteFacture(Math.max(0, nouveauCumulSoutotal - facture.getAvanceFacture()));

        //status
        double reste = nouveauCumulSoutotal - existFacture.getAvanceFacture();
        if (reste <= 0){
            existFacture.setStatus(StatusPaiement.PAYE);
        } else if (existFacture.getMontantFacture() == existFacture.getResteFacture()) {
            existFacture.setStatus(StatusPaiement.NON_PAYE);
        }else {
            existFacture.setStatus(StatusPaiement.PARTIEL);
        }
        System.out.println("STATUS exist facture: "+existFacture.getStatus());
        System.out.println("STATUS  facture: "+facture.getStatus());
        if (facture.getModePaiement() != null){
            existFacture.setModePaiement(facture.getModePaiement());
        }

        return  factureRepository.save(existFacture);
    }

     */

    @Transactional
    public FactureDTO updateFacture(String idFacture, FactureDTO dto) {
        FactureEntity existFacture = factureRepository.findById(idFacture)
                .orElseThrow(() -> new NotFoundFacture(idFacture));

        // 1. Restituer les anciens stocks
        for (FactureProduitEntity ancienneLigne : existFacture.getFactureProduitEntityList()) {
            ProduitEntity produitARendre = ancienneLigne.getProduit();
            produitARendre.setQuantiteProduit(produitARendre.getQuantiteProduit() + ancienneLigne.getQuantiteVenteProduit());
            produitRepository.save(produitARendre);
        }

        // Mise à jour du client si fourni dans le DTO
        if (dto.client() != null && dto.client().idClient() != null) {
            ClientEntity nouveauClient = clientRepository.findById(dto.client().idClient())
                    .orElseThrow(() -> new NotFoundClient(dto.client().idClient()));
            existFacture.setClient(nouveauClient);
        }

        // 2. Vider les anciennes lignes
        existFacture.getFactureProduitEntityList().clear();

        // 3. Reconstruire avec les nouvelles lignes DTO
        double nouveauCumulSoutotal = 0;
        double nouveauCumulMarge = 0;

        if (dto.factureProduitEntityList() != null) {
            for (FactureProduitDTO nouvelleLigneDto : dto.factureProduitEntityList()) {
                ProduitEntity produit = produitRepository.findById(nouvelleLigneDto.idProduit())
                        .orElseThrow(() -> new NotFoundProduit(nouvelleLigneDto.idProduit()));

                double qteDemandee = nouvelleLigneDto.quantiteVenteProduit();

                if (produit.getQuantiteProduit() >= qteDemandee) {
                    FactureProduitEntity nouvelleLigne = factureProduitMapper.toEntity(nouvelleLigneDto);
                    nouvelleLigne.setProduit(produit);
                    nouvelleLigne.setPrixVenteProduit(produit.getPrixVenteProduit());

                    double sousTotal = produit.getPrixVenteProduit() * qteDemandee;
                    double soustotalMarge = qteDemandee * produit.getMargeProduit();

                    nouvelleLigne.setSousTotalVenteProduit(sousTotal);
                    nouvelleLigne.setMargeVenteProduit(soustotalMarge);

                    nouveauCumulSoutotal += sousTotal;
                    nouveauCumulMarge += soustotalMarge;

                    nouvelleLigne.setFacture(existFacture);
                    existFacture.getFactureProduitEntityList().add(nouvelleLigne);

                    produit.setQuantiteProduit(produit.getQuantiteProduit() - qteDemandee);
                    produitRepository.save(produit);
                }
            }
        }

        existFacture.setDateModifFacture(LocalDate.now());
        existFacture.setMontantFacture(nouveauCumulSoutotal);
        existFacture.setMargeFacture(nouveauCumulMarge);
        existFacture.setAvanceFacture(dto.avanceFacture());
        existFacture.setResteFacture(Math.max(0, nouveauCumulSoutotal - dto.avanceFacture()));

        double reste = nouveauCumulSoutotal - dto.avanceFacture();
        if (reste <= 0) {
            existFacture.setStatus(StatusPaiement.PAYE);
        } else if (existFacture.getMontantFacture() == existFacture.getResteFacture()) {
            existFacture.setStatus(StatusPaiement.NON_PAYE);
        } else {
            existFacture.setStatus(StatusPaiement.PARTIEL);
        }

        if (dto.modePaiement() != null) {
            existFacture.setModePaiement(dto.modePaiement());
        }

        FactureEntity updated = factureRepository.save(existFacture);
        return factureMapper.toDTO(updated);
    }


    //compter
    public long countFactures() {
        return factureRepository.count();
    }

    public long countFactureNonPaye(){
        return factureRepository.countFactureNomPaye();
    }

    public long countFacturePayePartiel(){
        return factureRepository.countFacturePayePartiel();
    }

    public long countFacturePaye(){
        return factureRepository.countFacturePaye();
    }

    public Double montantFactureNomPaye() {
        Double montant = factureRepository.countMontantFactureNomPaye();
        return montant != null ? montant : 0.0;
    }

    public Double montantFactureDuMoisEnCour() {
        LocalDate maintenant= LocalDate.now();

        //Calcul du premier jour du mois
        // 2. Calculer le premier jour du mois à 00:00:00
        LocalDate debutMois = maintenant.with(TemporalAdjusters.firstDayOfMonth());

        // 3. Calculer le dernier jour du mois à 23:59:59
        LocalDate finMois = maintenant.with(TemporalAdjusters.lastDayOfMonth());

        // 4. Exécuter la requête
        Double total = factureRepository.montantfacturePayeParMoisEnCour(debutMois, finMois);

        return total != null ? total : 0.0;
    }

    public void deleteFacture(String idFacture){
        if (factureRepository.existsById(idFacture)) {
            factureRepository.deleteById(idFacture);
        }
    }

    //liste facture
    @Transactional(readOnly = true)
    public List<FactureDTO> findAll() {
        return factureRepository.findAll()
                .stream()
                .map(factureMapper::toDTO)
                .toList();
    }

    //recherche milticritère
    @Transactional(readOnly = true)
    public Page<FactureDTO> findMultiCriteriaFacture(String idFacture, String nomPrenom,LocalDate dateDebutFacture, LocalDate dateFinFacture, StatusPaiement statusPaiement,
                                                     ModePaiement modePaiement, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"dateFacture"));

        Page<FactureEntity> entityPage = factureRepository.findAll((root, query, cb)->{
            List<Predicate> predicates = new ArrayList<>();

            // Prévention de LazyInitializationException lors de la conversion DTO
            if (Long.class != query.getResultType()) {
              //  root.fetch("factureProduitEntityList", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("client", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true); // Évite les doublons générés par le JOIN FETCH
            }

            if (idFacture != null){
                predicates.add(cb.like(cb.lower(root.get("idFacture")), "%"+idFacture.toLowerCase()+"%"));
            }

            if (dateDebutFacture != null && dateFinFacture != null){
                predicates.add(cb.between(root.get("dateFacture"), dateDebutFacture, dateFinFacture));
            }else if (dateDebutFacture != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateFacture"), dateDebutFacture));
            }else if (dateFinFacture != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateFacture"), dateFinFacture));
            }

            if (statusPaiement!=null){
                predicates.add(cb.equal(root.get("status"), statusPaiement));
            }

            if (modePaiement!=null){
                predicates.add(cb.equal(root.get("modePaiement"), modePaiement));
            }

            if (nomPrenom != null && !nomPrenom.isEmpty()){
                 Join<FactureEntity, ClientEntity> join = root.join("client", JoinType.LEFT);
                 String searchPattern = "%"+nomPrenom.toLowerCase().trim()+"%";

                Predicate nomClient = cb.like(cb.lower(join.get("nomClient")), searchPattern);
                Predicate prenomClient = cb.like(cb.lower(join.get("prenomClient")), searchPattern);
                predicates.add(cb.or(nomClient, prenomClient));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        },pageable);

        return entityPage.map(factureMapper::toDTO);
    }
}
