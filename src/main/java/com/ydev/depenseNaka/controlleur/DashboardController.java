package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.ProduitVenduDTO;
import com.ydev.depenseNaka.service.ClientService;
import com.ydev.depenseNaka.service.CommandeService;
import com.ydev.depenseNaka.service.FactureService;
import com.ydev.depenseNaka.service.StatistiqueService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DashboardController {
    private final ClientService clientService;
    private final CommandeService commandeService;
    private final FactureService factureService;
    private final StatistiqueService statistiqueService;
    private final Utilitaire utilitaire;

    @FXML private Label lblVentesJour;
    @FXML private Label lblFactureImp;
    @FXML private Label lblCommandesCours;
    @FXML private Label lblCommandesValide;
    @FXML private Label lblAlerteStock;
    @FXML private Label lblFactureNPPP;
    @FXML private Label lblTotalClients;
    @FXML private LineChart<String, Number> chartVente;

    @FXML private LineChart<String, Number> chartVentes;
    @FXML private TableView<ProduitVenduDTO> tableTopProduits;
    @FXML private TableColumn<ProduitVenduDTO, String> colNomProduit;
    @FXML private TableColumn<ProduitVenduDTO, Long> colQuantite;
    @FXML private TableColumn<ProduitVenduDTO, String> colMontantTotal;

    @FXML
    public void initialize() {
        initTableauTopProduits();
        chargerDonneesStatistiques();
        chargerGraphiqueVentesMois();
        chargerTopProduits();

    }

    private void chargerDonneesStatistiques() {
        //Montant facture du mois
        Double montantMoisActuel = factureService.montantFactureDuMoisEnCour();
        Double montantFactureNP = factureService.montantFactureNomPaye();
        lblVentesJour.setText(utilitaire.appliquerSeparateurMillier(montantMoisActuel));
        lblFactureImp.setText(utilitaire.appliquerSeparateurMillier(montantFactureNP));

        //nombre commande en attente
        long nbrCmdAttente = commandeService.countCommandeEnAttente();
        long nbrCmdValide = commandeService.countCommandeValide();
        lblCommandesCours.setText(nbrCmdAttente+"");
        lblCommandesValide.setText("" + nbrCmdValide);

        //nombre commande en attente
        long nbrFactureNp = factureService.countFactureNonPaye();
        long nbrTotalFacture = factureService.countFactures();
        long nbrFacturePayePartiel = factureService.countFacturePayePartiel();
        long nbrFacturePaye = factureService.countFacturePaye();
        lblAlerteStock.setText(nbrTotalFacture + " | " + nbrFacturePaye);
        lblFactureNPPP.setText(nbrFacturePayePartiel + " | " + nbrFactureNp);

        //nombre des clients
       long nombreClient = clientService.compterNbrClient();
        lblTotalClients.setText(nombreClient+"");
    }

    private void initTableauTopProduits() {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        colNomProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteVendue"));

        colMontantTotal.setCellValueFactory(cellData -> {
            Double total = cellData.getValue() != null ? cellData.getValue().getMontantTotal() : null;
            String valStr = (total != null) ? formatter.format(total) : "0";
            return new SimpleStringProperty(valStr);
        });

        utilitaire.appliquerColorationTopVenteDashboard(colMontantTotal);
    }

    private void chargerGraphiqueVentesMois() {
        chartVentes.getData().clear();

        Map<LocalDate, Double> ventesJour = statistiqueService.getVentesJourMoisEnCours();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes du mois");

        LocalDate aujourdhui = LocalDate.now();
        int joursDansLeMois = aujourdhui.lengthOfMonth();

        for (int i = 1; i <= joursDansLeMois; i++) {
            LocalDate dateJour = aujourdhui.withDayOfMonth(i);
            Double montant = ventesJour.getOrDefault(dateJour, 0.0);

            String labelJour = String.format("%02d", i);
            series.getData().add(new XYChart.Data<>(labelJour, montant));
        }

        chartVentes.getData().add(series);

        // Appliquer les tooltips uniquement quand l'affichage UI est prêt
        Platform.runLater(() -> {
            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    String montantStr = formatter.format(data.getYValue()) + " MGA";
                    Tooltip tooltip = new Tooltip("Jour " + data.getXValue() + " : " + montantStr);
                    tooltip.setShowDelay(Duration.millis(50));
                    Tooltip.install(data.getNode(), tooltip);

                    data.getNode().setOnMouseEntered(e -> data.getNode().setStyle("-fx-scale-x: 1.3; -fx-scale-y: 1.3; -fx-cursor: hand;"));
                    data.getNode().setOnMouseExited(e -> data.getNode().setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
                }
            }
        });
    }

    private void chargerTopProduits() {
        List<ProduitVenduDTO> topProduits = statistiqueService.getTopProduitsMois(10);
        System.out.println("Taille de la liste Top Produits : " + (topProduits != null ? topProduits.size() : "NULL"));
        tableTopProduits.setItems(FXCollections.observableArrayList(topProduits));
    }

}