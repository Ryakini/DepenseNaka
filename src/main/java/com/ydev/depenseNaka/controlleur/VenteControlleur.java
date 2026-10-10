package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.ClientDTO;
import com.ydev.depenseNaka.dto.FactureDTO;
import com.ydev.depenseNaka.dto.FactureProduitDTO;
import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.model.ModePaiement;
import com.ydev.depenseNaka.service.ClientService;
import com.ydev.depenseNaka.service.FactureService;
import com.ydev.depenseNaka.service.ProduitService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class VenteControlleur {

    private final FactureService factureService;
    private final ProduitService produitService;
    private final ClientService clientService;
    private final Utilitaire utilitaire;

    // Catalogue Produits
    @FXML private TextField txtSearchProduit;
    @FXML private TableView<ProduitDTO> tableCatalogueProduits;
    @FXML private TableColumn<ProduitDTO, String> colCatId;
    @FXML private TableColumn<ProduitDTO, String> colCatNom;
    @FXML private TableColumn<ProduitDTO, String> colCatCategorie;
    @FXML private TableColumn<ProduitDTO, Number> colCatPrix;
    @FXML private TableColumn<ProduitDTO, Number> colCatStock;
    @FXML private TableColumn<ProduitDTO, Void> colCatAction;

    // Panier & Encaissement
    @FXML private ComboBox<ClientDTO> cbClient;
    @FXML private TableView<FactureProduitDTO> tablePanier;
    @FXML private TableColumn<FactureProduitDTO, String> colPanierNom;
    @FXML private TableColumn<FactureProduitDTO, Number> colPanierPrix;
    @FXML private TableColumn<FactureProduitDTO, Double> colPanierQte;
    @FXML private TableColumn<FactureProduitDTO, Number> colPanierTotal;
    @FXML private TableColumn<FactureProduitDTO, Void> colPanierAction;

    @FXML private Label lblMontantTotal;
    @FXML private TextField txtAvance;
    @FXML private Label txtReste;
    @FXML private ComboBox<ModePaiement> cbModePaiement;

    private final ObservableList<ClientDTO> clientList = FXCollections.observableArrayList();
    private final ObservableList<ProduitDTO> catalogueList = FXCollections.observableArrayList();
    private final ObservableList<FactureProduitDTO> panierList = FXCollections.observableArrayList();
    private  List<ProduitDTO> produitsDisponibles;

    DoubleProperty avanceSaisie;

    @FXML
    public void initialize() {
        initCatalogueTable();
        initPanierTable();
        initFormControls();
        chargerDonnees();

        txtSearchProduit.requestFocus();

        avanceSaisie = utilitaire.configurerChampMontantDynamique(txtAvance);

        avanceSaisie.addListener(e->{
            calculerTotaux();
        });
    }

    private void initCatalogueTable() {
        colCatId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().idProduit()));
        colCatNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomProduit()));
        colCatCategorie.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().categorie() != null ? data.getValue().categorie().nomCategorie() : ""));
        colCatPrix.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().prixVenteProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colCatPrix);
        colCatStock.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().quantiteProduit()));
        utilitaire.appliquerBadgeStockDisponibilite(colCatStock);

        // Bouton d'ajout au panier
        colCatAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAdd = new Button();
            {
                btnAdd.getStyleClass().addAll("flat", "accent", "small");
                btnAdd.setGraphic(new FontIcon("fas-plus-circle"));
                btnAdd.setOnAction(e -> {
                    ProduitDTO p = getTableView().getItems().get(getIndex());
                    ajouterAuPanier(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAdd);
            }
        });

        // Recherche en temps réel
        txtSearchProduit.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                catalogueList.setAll(produitService.findListeProduitActif());
            } else {
                catalogueList.setAll(produitService.findListeProduitNom(newVal.trim()));
            }
        });
    }

    private void initPanierTable() {
        tablePanier.setEditable(true);

        colPanierNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomProduit()));
        colPanierPrix.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().prixVenteProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPanierPrix);
        colPanierTotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().sousTotalVenteProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPanierTotal);

        // Quantité éditable directement dans le tableau
        colPanierQte.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().quantiteVenteProduit()).asObject());
        colPanierQte.setCellFactory(TextFieldTableCell.forTableColumn(Utilitaire.getSafeDoubleConverter()));
        colPanierQte.setOnEditCommit(event -> {
            FactureProduitDTO ancienneLigne = event.getRowValue();
            double nouvelleQte = event.getNewValue();

            produitsDisponibles = produitService.findListeProduitActif();

            if (nouvelleQte <= 0) {
                tablePanier.refresh();
                return;
            }
            //verification
            ProduitDTO produitEnStock = produitsDisponibles.stream()
                    .filter(p -> p.idProduit().equals(ancienneLigne.idProduit()))
                    .findFirst()
                    .orElse(null);

            if (produitEnStock != null) {
                // En modification de facture, le stock disponible réel est :
                // Stock actuel en BD + Quantité déjà réservée sur cette ligne de facture
                double qteDejaReservee = ancienneLigne.quantiteVenteProduit();
                double qteMaxAutorisee = produitEnStock.quantiteProduit() + qteDejaReservee;

                if (nouvelleQte > qteMaxAutorisee) {
                    utilitaire.afficherAvertissement("Stock Insuffisant", "La quantité saisie"
                            +nouvelleQte+"dépasse le stock disponible (Max :"+qteMaxAutorisee+")");

                    // Annuler la modification et rafraîchir la table
                    tablePanier.refresh();
                    return;
                }
            }

            double prixU = ancienneLigne.prixVenteProduit();
            double nouveauSousTotal = prixU * nouvelleQte;

            double margeUnitaire = (ancienneLigne.quantiteVenteProduit() > 0)
                    ? ancienneLigne.margeVenteProduit() / ancienneLigne.quantiteVenteProduit()
                    : 0;
            double nouvelleMargeTotale = margeUnitaire * nouvelleQte;


                double sousTotal = ancienneLigne.prixVenteProduit() * nouvelleQte;
                FactureProduitDTO updated = FactureProduitDTO.builder()
                        .idProduit(ancienneLigne.idProduit())
                        .nomProduit(ancienneLigne.nomProduit())
                        .prixVenteProduit(ancienneLigne.prixVenteProduit())
                        .quantiteVenteProduit(nouvelleQte)
                        .sousTotalVenteProduit(sousTotal)
                        .build();

            int index = panierList.indexOf(ancienneLigne);
            panierList.set(index, updated);
            calculerTotaux();
        });

        // Bouton de suppression de ligne
        colPanierAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button();
            {
                btnRemove.getStyleClass().addAll("flat", "danger", "small");
                btnRemove.setGraphic(new FontIcon("fas-trash-alt"));
                btnRemove.setOnAction(e -> {
                    FactureProduitDTO item = getTableView().getItems().get(getIndex());
                    panierList.remove(item);
                    calculerTotaux();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
            }
        });

        tablePanier.setItems(panierList);
    }

    private void initFormControls() {
        cbModePaiement.setItems(FXCollections.observableArrayList(ModePaiement.values()));
        cbModePaiement.setValue(ModePaiement.ESPECE);

        List<ClientDTO> liste = clientService.findAllByOrderByNomClientAsc();
        cbClient.setConverter(new StringConverter<>() {
            @Override
            public String toString(ClientDTO client) {
                return client != null ? client.nomClient() + " " + (client.prenomClient() != null ? client.prenomClient() : "") : "";
            }

            @Override
            public ClientDTO fromString(String string) {

                return clientList.stream()
                        .filter(clientDTO -> clientDTO.nomClient() != null && clientDTO.nomClient().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        utilitaire.configureSearchableComboBox(cbClient, liste, ClientDTO::nomClient, 6);

        // Recalcul automatique du reste à payer quand l'avance change
        txtAvance.textProperty().addListener((obs, oldVal, newVal) -> calculerTotaux());
    }

    private void chargerDonnees() {
        catalogueList.setAll(produitService.findListeProduitActif());
        tableCatalogueProduits.setItems(catalogueList);

        cbClient.setItems(FXCollections.observableArrayList(clientService.findAllByOrderByNomClientAsc()));
    }

    private void ajouterAuPanier(ProduitDTO produit) {
        if (produit.quantiteProduit() <= 0) {
           // afficherAlerte(Alert.AlertType.WARNING, "Stock Epuisé", "Ce produit n'est plus en stock.");
            utilitaire.afficherAvertissement("Stock Epuisé", "Ce produit n'est plus en stock.");
            return;
        }

        // Vérifier si le produit existe déjà dans le panier
        for (int i = 0; i < panierList.size(); i++) {
            FactureProduitDTO item = panierList.get(i);
            if (item.idProduit().equals(produit.idProduit())) {
                double nouvelleQte = item.quantiteVenteProduit() + 1;
                if (nouvelleQte > produit.quantiteProduit()) {
                    //afficherAlerte(Alert.AlertType.WARNING, "Limite Stock", "Quantité maximale en stock atteinte.");
                    utilitaire.afficherAvertissement("Limite Stock", "Quantité maximale en stock atteinte.");
                    return;
                }
                double sousTotal = item.prixVenteProduit() * nouvelleQte;
                panierList.set(i, FactureProduitDTO.builder()
                        .idProduit(item.idProduit())
                        .nomProduit(item.nomProduit())
                        .prixVenteProduit(item.prixVenteProduit())
                        .quantiteVenteProduit(nouvelleQte)
                        .sousTotalVenteProduit(sousTotal)
                        .build());
                calculerTotaux();
                return;
            }
        }

        // Sinon ajouter une nouvelle ligne
        panierList.add(FactureProduitDTO.builder()
                .idProduit(produit.idProduit())
                .nomProduit(produit.nomProduit())
                .prixVenteProduit(produit.prixVenteProduit())
                .quantiteVenteProduit(1.0)
                .sousTotalVenteProduit(produit.prixVenteProduit())
                .build());

        calculerTotaux();
    }

    private void calculerTotaux() {
        double total = panierList.stream().mapToDouble(FactureProduitDTO::sousTotalVenteProduit).sum();
        lblMontantTotal.setText(utilitaire.appliquerSeparateurMillierAr(total));

        double avance = 0;
        try {
            if (txtAvance.getText() != null && !txtAvance.getText().isBlank()) {
                avance = Double.parseDouble(txtAvance.getText().trim());
            }
        } catch (NumberFormatException e) {
            // Ignorer si format incomplet pendant la saisie
        }

        double reste = Math.max(0, total - avance);
       // txtReste.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
        txtReste.setText(utilitaire.appliquerSeparateurMillierAr(reste));
    }


    @FXML
    private void reinitialiserRechercheProduit() {
        txtSearchProduit.clear();
    }

    @FXML
    private void annulerVente() {
        panierList.clear();
        txtAvance.clear();
        txtReste.setText("0.0");
        lblMontantTotal.setText("0.00 Ar");
        cbClient.setValue(null);
    }

    @FXML
    private void validerVente() {
        if (cbClient.getValue() == null) {
           // afficherAlerte(Alert.AlertType.WARNING, "Client requis", "Veuillez sélectionner un client pour effectuer la vente.");
            utilitaire.afficherAvertissement("Client requis","Veuillez sélectionner un client pour effectuer la vente.");
            return;
        }

        if (panierList.isEmpty()) {
            //afficherAlerte(Alert.AlertType.WARNING, "Panier vide", "Veuillez ajouter au moins un produit au panier.");
            utilitaire.afficherAvertissement("Panier vide","Veuillez ajouter au moins un produit au panier.");
            return;
        }

        try {
            //double avance = txtAvance.getText().isBlank() ? 0 : Double.parseDouble(txtAvance.getText().trim());

            FactureDTO factureDTO = FactureDTO.builder()
                    .client(cbClient.getValue())
                    .avanceFacture(avanceSaisie.get())
                    .modePaiement(cbModePaiement.getValue())
                    .factureProduitEntityList(new ArrayList<>(panierList))
                    .build();

            // Création via le service Spring
            FactureDTO enregistree = factureService.createFacture(factureDTO);

            //afficherAlerte(Alert.AlertType.INFORMATION, "Vente Réussie", "Facture enregistrée avec succès : " + enregistree.idFacture());
            utilitaire.afficherAvertissement("Vente validée","Facture enregistrée avec succès : " + enregistree.idFacture());
            // Réinitialisation & Mise à jour des stocks affichés
            annulerVente();
            chargerDonnees();

        } catch (Exception e) {
           // afficherAlerte(Alert.AlertType.ERROR, "Erreur lors de la vente", e.getMessage());
            utilitaire.afficherErreur("Erreur lors de la validation",e.getMessage());
        }
    }

}
