package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.EvolutionStatDTO;
import com.ydev.depenseNaka.dto.MargeBeneficiaireDTO;
import com.ydev.depenseNaka.dto.MargeMensuelDTO;
import com.ydev.depenseNaka.dto.MeilleurClientDTO;
import com.ydev.depenseNaka.service.StatistiqueService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor // <-- Injection automatique par constructeur via Lombok
public class StatistiqueControlleur {

    private final StatistiqueService statistiqueService;
    private final Utilitaire utilitaire;

    // Filtres
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;

    // LineChart Évolutions
    @FXML private LineChart<String, Number> chartEvolution;

    // Tableau & Graphique Marges
    @FXML private TableView<MargeBeneficiaireDTO> tvMarges;
    @FXML private TableColumn<MargeBeneficiaireDTO, String> colMargeCommande;
    @FXML private TableColumn<MargeBeneficiaireDTO, String> colMargeClient;
    @FXML private TableColumn<MargeBeneficiaireDTO, Number> colMargeVente;
    @FXML private TableColumn<MargeBeneficiaireDTO, Number> colMargeDepense;
    @FXML private TableColumn<MargeBeneficiaireDTO, Number> colMargeNet;
    @FXML private TableColumn<MargeBeneficiaireDTO, String> colMargePourcentage;
    @FXML private BarChart<String, Number> chartMarges;

    // Tableau Meilleurs Clients
    @FXML private TableView<MeilleurClientDTO> tvMeilleursClients;
    @FXML private TableColumn<MeilleurClientDTO, String> colClientId;
    @FXML private TableColumn<MeilleurClientDTO, String> colClientNom;
    @FXML private TableColumn<MeilleurClientDTO, Number> colClientNbCommandes;
    @FXML private TableColumn<MeilleurClientDTO, Number> colClientTotalDepense;

    // --- Composants FXML Marge Mensuelle ---
    @FXML private TableView<MargeMensuelDTO> tvMargesMensuelles;
    @FXML private TableColumn<MargeMensuelDTO, String> colMoisAnnee;
    @FXML private TableColumn<MargeMensuelDTO, Number> colMoisVente;
    @FXML private TableColumn<MargeMensuelDTO, Number> colMoisDepense;
    @FXML private TableColumn<MargeMensuelDTO, Number> colMoisMargeNette;
    @FXML private TableColumn<MargeMensuelDTO, String> colMoisPourcentage;

    @FXML
    public void initialize() {
        // Initialiser la plage par défaut (30 derniers jours)
        dpFin.setValue(LocalDate.now());
        dpDebut.setValue(LocalDate.now().minusDays(30));

        initTableMarges();
        initTableMeilleursClients();
        initTableMargesMensuelles();

        chargerToutesLesStatistiques();
    }

    private void initTableMarges() {
        colMargeCommande.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().idCommande()));
        colMargeClient.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientNom()));
        colMargeVente.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().montantVente()));
        colMargeDepense.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().totalDepenses()));
        colMargeNet.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().margeBeneficiaire()));
        colMargePourcentage.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.2f %%", data.getValue().pourcentageMarge())
        ));
    }

    private void initTableMeilleursClients() {
        colClientId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().idClient()));
        colClientNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomComplet()));
        colClientNbCommandes.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().nombreCommandes()));
        colClientTotalDepense.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().totalDepense()));
    }

    private void initTableMargesMensuelles() {
        colMoisAnnee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().moisAnnee()));
        colMoisVente.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().totalVentes()));
        colMoisDepense.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().totalDepenses()));
        colMoisMargeNette.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().margeNette()));
        colMoisPourcentage.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.2f %%", data.getValue().pourcentageMarge())
        ));

        // Application du format de milliers
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colMoisVente);
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colMoisDepense);
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colMoisMargeNette);
    }

    private void chargerMargesMensuelles() {
        List<MargeMensuelDTO> margesMensuelles = statistiqueService.obtenirMargesMensuelles();
        tvMargesMensuelles.setItems(FXCollections.observableArrayList(margesMensuelles));
    }

    @FXML
    private void handleActualiser() {
        chargerToutesLesStatistiques();
    }

    private void chargerToutesLesStatistiques() {
        chargerEvolutionChart();
        chargerMarges();
        chargerMeilleursClients();
        chargerMargesMensuelles();
    }

    private void chargerEvolutionChart() {
        chartEvolution.getData().clear();

        XYChart.Series<String, Number> seriesVentes = new XYChart.Series<>();
        seriesVentes.setName("Chiffre d'Affaires");

        XYChart.Series<String, Number> seriesCommandes = new XYChart.Series<>();
        seriesCommandes.setName("Nombre de Commandes");

        List<EvolutionStatDTO> stats = statistiqueService.obtenirEvolutionGlobal(dpDebut.getValue(), dpFin.getValue());

        for (EvolutionStatDTO stat : stats) {
            String dateStr = stat.date().toString();
            seriesVentes.getData().add(new XYChart.Data<>(dateStr, stat.totalVentes()));
            seriesCommandes.getData().add(new XYChart.Data<>(dateStr, stat.nombreCommandes()));
        }

        chartEvolution.getData().addAll(seriesVentes, seriesCommandes);
    }

    private void chargerMarges() {
        List<MargeBeneficiaireDTO> marges = statistiqueService.obtenirMargesBeneficiaires();
        tvMarges.setItems(FXCollections.observableArrayList(marges));

        // Mise à jour du graphique en barres
        chartMarges.getData().clear();
        XYChart.Series<String, Number> seriesVentes = new XYChart.Series<>();
        seriesVentes.setName("Vente");

        XYChart.Series<String, Number> seriesDepenses = new XYChart.Series<>();
        seriesDepenses.setName("Dépense");

        for (MargeBeneficiaireDTO dto : marges) {
            seriesVentes.getData().add(new XYChart.Data<>(dto.idCommande(), dto.montantVente()));
            seriesDepenses.getData().add(new XYChart.Data<>(dto.idCommande(), dto.totalDepenses()));
        }

        chartMarges.getData().addAll(seriesVentes, seriesDepenses);
    }

    private void chargerMeilleursClients() {
        List<MeilleurClientDTO> meilleursClients = statistiqueService.obtenirMeilleursClients();
        tvMeilleursClients.setItems(FXCollections.observableArrayList(meilleursClients));
    }
}