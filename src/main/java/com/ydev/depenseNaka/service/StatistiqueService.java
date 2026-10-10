package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.*;
import com.ydev.depenseNaka.model.CommandeEntity;
import com.ydev.depenseNaka.model.DepenseEntity;
import com.ydev.depenseNaka.model.FactureEntity;
import com.ydev.depenseNaka.repository.ClientRepository;
import com.ydev.depenseNaka.repository.CommandeRepository;
import com.ydev.depenseNaka.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueService {

    // Injecter vos repositories (ex: CommandeRepository, ClientRepository, DepenseRepository)
     private final CommandeRepository commandeRepository;
     private final ClientRepository clientRepository;
     private final FactureRepository factureRepository;

    public List<EvolutionStatDTO> obtenirEvolutionGlobal(LocalDate dateDebut, LocalDate dateFin) {
        // TODO: Implémenter la requête aggrégée SQL / JPQL selon votre base de données
        //recupération des données entre plage de valeur
        List<CommandeEntity> commandes = commandeRepository.findByDateCreationCmdBetween(dateDebut, dateFin);
        List<FactureEntity> factures = factureRepository.findByDateFactureBetween(dateDebut, dateFin);

        //recurperer toutes les dates distincte dans l'intervalle
        HashSet<LocalDate> dates = new HashSet<>();
        commandes.forEach(commande -> {
            dates.add(commande.getDateCreationCmd());
        });
        factures.forEach(facture -> {
            dates.add(facture.getDateFacture());
        });

        List<EvolutionStatDTO> evolutionStatDTOs = new ArrayList<>();
        for (LocalDate date : dates.stream().sorted().toList()) {
            //nombre de commandes d'une date
            long nbrCmd = commandes.stream()
                    .filter(c -> c.getDateCreationCmd().equals(date))
                    .count();

            //montant des ventes
            double montantFacture = factures.stream()
                    .filter(f -> f.getDateFacture().equals(date))
                    .mapToDouble(FactureEntity::getMontantFacture)
                    .sum();

            //nombre de client apparus dans cette date
            long nouveauxClient = factures.stream()
                    .filter(f -> f.getDateFacture().equals(date) && f.getClient() != null)
                    .map(f-> f.getClient().getIdClient())
                    .distinct()
                    .count();

            evolutionStatDTOs.add(new EvolutionStatDTO(date, nbrCmd, nouveauxClient, montantFacture));
        }
       // return List.of();
        return evolutionStatDTOs;
    }

    public List<MargeBeneficiaireDTO> obtenirMargesBeneficiaires() {
        // TODO: Calculer (Prix total commande - Somme des dépenses associées)
        List<CommandeEntity> commandes = commandeRepository.findAll();

        return commandes.stream().map(cmd -> {
            double montantVente = cmd.getMontantCmd();

            // Calcul du total des dépenses liées à la commande
            double totalDepenses = (cmd.getListeDepense() != null) ?
                    cmd.getListeDepense().stream().mapToDouble(DepenseEntity::getMontantDepense).sum() : 0.0;

            double margeNette = montantVente - totalDepenses;

            // Calcul du pourcentage de marge : (Marge Nette / Montant Vente) * 100
            double pourcentageMarge = (montantVente > 0) ? (margeNette / montantVente) * 100 : 0.0;

            String nomClient = (cmd.getFournisseurCmd() != null && !cmd.getFournisseurCmd().isBlank())
                    ? cmd.getFournisseurCmd() : "N/A";

            return new MargeBeneficiaireDTO(
                    cmd.getIdCommande(),
                    nomClient,
                    montantVente,
                    totalDepenses,
                    margeNette,
                    pourcentageMarge
            );
        }).collect(Collectors.toList());
       // return List.of();
    }

    public List<MeilleurClientDTO> obtenirMeilleursClients() {
        // TODO: Requete GROUP BY client_id ORDER BY SUM(montant) DESC

        List<FactureEntity> factures = factureRepository.findAll();

        // Regroupement par Client
        Map<String, List<FactureEntity>> facturesParClient = factures.stream()
                .filter(f -> f.getClient() != null)
                .collect(Collectors.groupingBy(f -> f.getClient().getIdClient()));

        List<MeilleurClientDTO> meilleursClients = new ArrayList<>();

        facturesParClient.forEach((clientId, listeFactures) -> {
            var client = listeFactures.get(0).getClient();
            String nomComplet = client.getNomClient() + " " + (client.getPrenomClient() != null ? client.getPrenomClient() : "");

            long nbAchats = listeFactures.size();
            double totalDepense = listeFactures.stream().mapToDouble(FactureEntity::getMontantFacture).sum();

            meilleursClients.add(new MeilleurClientDTO(
                    client.getIdClient(),
                    nomComplet.trim(),
                    nbAchats,
                    totalDepense
            ));
        });

        // Tri par montant cumulé décroissant
        meilleursClients.sort(Comparator.comparingDouble(MeilleurClientDTO::totalDepense).reversed());

        return meilleursClients;
        //return List.of();
    }

    //Marge beneficiêre
    public List<MargeMensuelDTO> obtenirMargesMensuelles() {
        List<FactureEntity> factures = factureRepository.findAll();
        List<CommandeEntity> commandes = commandeRepository.findAll();

        //1.Grouper les ventes par mois
        Map<YearMonth, Double> ventesParMois = factures.stream()
                .filter(f -> f.getDateFacture() != null )
                .collect(Collectors.groupingBy(
                        f -> YearMonth.from(f.getDateFacture()),
                        Collectors.summingDouble(FactureEntity::getMontantFacture)));

        //2.Grouper les dépenses associés à une commande par mois
        Map<YearMonth, Double> depensesParMois = new HashMap<>();
        for (CommandeEntity commande : commandes) {
            if (commande.getDateCreationCmd() != null && commande.getListeDepense() != null){
                YearMonth date = YearMonth.from(commande.getDateCreationCmd());
                double totalDepense = commande.getListeDepense().stream()
                        .mapToDouble(DepenseEntity::getMontantDepense)
                        .sum();
                depensesParMois.merge(date, totalDepense, Double::sum);
            }
        }

        // 3. Fusionner toutes les clés (mois) existantes
        Set<YearMonth> tousLesMois = new HashSet<>(ventesParMois.keySet());
        tousLesMois.addAll(depensesParMois.keySet());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        List<MargeMensuelDTO> resultats = new ArrayList<>();

        for (YearMonth ym : tousLesMois.stream().sorted(Comparator.reverseOrder()).toList()) {
            double totalVentes = ventesParMois.getOrDefault(ym, 0.0);
            double totalDepenses = depensesParMois.getOrDefault(ym, 0.0);
            double margeNette = totalVentes - totalDepenses;
            double pourcentage = (totalVentes > 0) ? (margeNette / totalVentes) * 100 : 0.0;

            // Capitaliser la première lettre du mois (ex: "Janvier 2026")
            String labelMois = ym.format(formatter);
            labelMois = labelMois.substring(0, 1).toUpperCase() + labelMois.substring(1);

            resultats.add(new MargeMensuelDTO(
                    labelMois,
                    totalVentes,
                    totalDepenses,
                    margeNette,
                    pourcentage
            ));
        }
        return resultats;
    }

    /*
    public Map<LocalDate, Double> getVentesJourMoisEnCours() {
        LocalDate maintenant = LocalDate.now();

        // Formatage : "2026" pour l'année, "09" pour le mois
        String anneeStr = String.valueOf(maintenant.getYear());
        String moisStr = String.format("%02d", maintenant.getMonthValue());

        List<Object[]> results = factureRepository.findVentesJournalieresDuMois(anneeStr, moisStr);

        Map<LocalDate, Double> mapVentes = new LinkedHashMap<>();
        for (Object[] row : results) {
            if (row[0] != null && row[1] != null) {
                LocalDate date;
                if (row[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) row[0]).toLocalDate();
                } else if (row[0] instanceof String) {
                    date = LocalDate.parse((String) row[0]);
                } else {
                    date = (LocalDate) row[0];
                }
                Double total = ((Number) row[1]).doubleValue();
                mapVentes.put(date, total);
            }
        }
        return mapVentes;
    }

    public List<ProduitVenduDTO> getTopProduitsMois(int limit) {
        LocalDate maintenant = LocalDate.now();

        String anneeStr = String.valueOf(maintenant.getYear());
        String moisStr = String.format("%02d", maintenant.getMonthValue());

        List<Object[]> results = factureRepository.findTopProduitsDuMois(
                anneeStr,
                moisStr,
                PageRequest.of(0, limit)
        );

        return results.stream()
                .map(row -> new ProduitVenduDTO(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() : 0L,
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }
    */

    public Map<LocalDate, Double> getVentesJourMoisEnCours() {
        LocalDate maintenant = LocalDate.now();
        LocalDate debutMois = maintenant.withDayOfMonth(1);
        LocalDate finMois = maintenant.withDayOfMonth(maintenant.lengthOfMonth());

        List<Object[]> results = factureRepository.findVentesJournalieresParPeriode(debutMois, finMois);

        Map<LocalDate, Double> mapVentes = new LinkedHashMap<>();
        for (Object[] row : results) {
            if (row[0] != null && row[1] != null) {
                LocalDate date = (row[0] instanceof java.sql.Date) ?
                        ((java.sql.Date) row[0]).toLocalDate() :
                        (row[0] instanceof String ? LocalDate.parse((String) row[0]) : (LocalDate) row[0]);

                Double total = ((Number) row[1]).doubleValue();
                mapVentes.put(date, total);
            }
        }
        return mapVentes;
    }

    public List<ProduitVenduDTO> getTopProduitsMois(int limit) {
        LocalDate maintenant = LocalDate.now();
        LocalDate debutMois = maintenant.withDayOfMonth(1);
        LocalDate finMois = maintenant.withDayOfMonth(maintenant.lengthOfMonth());

        List<Object[]> results = factureRepository.findTopProduitsParPeriode(debutMois, finMois, PageRequest.of(0, limit));

        return results.stream()
                .map(row -> new ProduitVenduDTO(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() : 0L,
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

}
