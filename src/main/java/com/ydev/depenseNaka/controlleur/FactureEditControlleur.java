package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.FactureDTO;
import com.ydev.depenseNaka.dto.FactureProduitDTO;
import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.model.ModePaiement;
import com.ydev.depenseNaka.model.StatusPaiement;
import com.ydev.depenseNaka.service.FactureService;
import com.ydev.depenseNaka.service.ProduitService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@NoArgsConstructor
public class FactureEditControlleur {

        @Autowired
        private Utilitaire utilitaire;
        @Autowired
        private  FactureService factureService;

        @FXML
        private ComboBox<ProduitDTO> cbProduits;
        @FXML private TextField txtQuantite;
        @FXML private TableView<FactureProduitDTO> tableLignes;
        @FXML private TableColumn<FactureProduitDTO, String> colProduit;
        @FXML private TableColumn<FactureProduitDTO, Number> colPrix, colTotal;
        @FXML private TableColumn<FactureProduitDTO, Double> colQte;
        @FXML private TableColumn<FactureProduitDTO, Void> colAction;

        @FXML private Label lblNumeroFacture;
        @FXML private Label lblNomClient;
        @FXML private Label lblAvance;
        @FXML private Label lblReste;
        @FXML private TextField txtMontantPayer;
        @FXML private ComboBox<ModePaiement> cbModePaiement;
        @FXML private Label lblTotalFacture;

        private ObservableList<FactureProduitDTO> listeLignes = FXCollections.observableArrayList();
        private FactureDTO factureOriginale;
        private DoubleProperty montantRecu;
        private List<ProduitDTO> produitsDisponibles;

        public void initData(FactureDTO facture, List<ProduitDTO> produitsDisponibles, FactureService factureService, Utilitaire utilitaire) {
            this.produitsDisponibles = produitsDisponibles;
            this.utilitaire = utilitaire;
            this.factureOriginale = facture;
            this.factureService = factureService;
            this.cbProduits.setItems(FXCollections.observableArrayList(produitsDisponibles));
            montantRecu = utilitaire.configurerChampMontantDynamique(txtMontantPayer);

            // Initialisation du numéro et du client
            if (facture != null) {
                this.lblNumeroFacture.setText("N° : " + (facture.idFacture() != null ? facture.idFacture() : "-"));
                this.lblNomClient.setText("Client : " + (facture.client() != null ? facture.client().nomClient() : "Passager"));
                this.lblAvance.setText(String.format("Avance : %.2f Ar", facture.avanceFacture()));
                this.lblReste.setText(String.format("Reste : %.2f Ar", facture.resteFacture()));
            }

            // Initialisation de la ComboBox des modes de paiement
            this.cbModePaiement.setItems(FXCollections.observableArrayList(ModePaiement.values()));
            if (facture != null && facture.modePaiement() != null) {
                this.cbModePaiement.setValue(facture.modePaiement());
            }

            if (facture.factureProduitEntityList() != null) {
                this.listeLignes.addAll(facture.factureProduitEntityList());
            }

            tableLignes.setItems(listeLignes);
             Utilitaire.configurerChampMontantDynamiqueStatique(txtMontantPayer);
             Utilitaire.configurerChampMontantDynamiqueStatique(txtQuantite);
            initColonnes();
            calculerTotal();
            initChamp();
        }


    private void initColonnes() {
        tableLignes.setEditable(true);

        colProduit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nomProduit()));
        colPrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixVenteProduit()));
        colTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().sousTotalVenteProduit()));

        // Utiliser .asObject() pour convertir ReadOnlyDoubleProperty en ObservableValue<Double>
        colQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().quantiteVenteProduit()).asObject());

        // Configuration de la cellule éditable
        colQte.setCellFactory(TextFieldTableCell.forTableColumn(Utilitaire.getSafeDoubleConverter()));
        colQte.setEditable(true);

        // Événement d'édition
        colQte.setOnEditCommit(event -> {
            FactureProduitDTO ancienneLigne = event.getRowValue();
            Double nouvelleQte = event.getNewValue();

            if (nouvelleQte == null || nouvelleQte <= 0) {
                tableLignes.refresh();
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
                    // Afficher une alerte ou un message d'erreur

                    utilitaire.afficherAvertissement("Stock Insuffisant", "La quantité saisie"
                            +nouvelleQte+"dépasse le stock disponible (Max :"+qteMaxAutorisee+")");

                    // Annuler la modification et rafraîchir la table
                    tableLignes.refresh();
                    return;
                }
            }

            double prixU = ancienneLigne.prixVenteProduit();
            double nouveauSousTotal = prixU * nouvelleQte;

            double margeUnitaire = (ancienneLigne.quantiteVenteProduit() > 0)
                    ? ancienneLigne.margeVenteProduit() / ancienneLigne.quantiteVenteProduit()
                    : 0;
            double nouvelleMargeTotale = margeUnitaire * nouvelleQte;

            // Recréer le record avec la nouvelle quantité
            FactureProduitDTO nouvelleLigne = new FactureProduitDTO(
                    ancienneLigne.idFactureProduit(),
                    prixU,
                    nouvelleQte,
                    nouveauSousTotal,
                    nouvelleMargeTotale,
                    ancienneLigne.idProduit(),
                    ancienneLigne.nomProduit()
            );

           // factureService.updateFacture()

            int index = event.getTablePosition().getRow();
            listeLignes.set(index, nouvelleLigne);
            calculerTotal();
        });
        initColonneAction();
    }

        public void initChamp(){
            // 4. Converter (nécessaire si le ComboBox est éditable ou pour la recherche texte)
            cbProduits.setConverter(new StringConverter<ProduitDTO>() {
                @Override
                public String toString(ProduitDTO produit) {
                    // Affichage de la désignation dans la liste déroulante
                    return (produit != null) ? produit.nomProduit() : "";
                }

                @Override
                public ProduitDTO fromString(String string) {
                    // Inutile pour un ComboBox non éditable
                    return null;
                }
            });

        }

    private void initColonneAction() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnSupprimer = new Button();
            private final HBox container = new HBox(8, btnSupprimer);

            {
                btnSupprimer.getStyleClass().addAll("danger", "button-icon", "flat"); // Style CSS optionnel
                FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                deleteIcon.setIconSize(13);
                btnSupprimer.setGraphic(deleteIcon);
                btnSupprimer.setTooltip(new Tooltip("Supprimer (Mode DEV)"));

                btnSupprimer.setOnAction(event -> {
                    FactureProduitDTO ligne = getTableView().getItems().get(getIndex());
                    listeLignes.remove(ligne);
                    calculerTotal(); // Recalcul du total de la facture
                });
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSupprimer);
                }
            }
        });
    }

    @FXML
    private void handleAjouterProduit() {
        ProduitDTO p = cbProduits.getValue();
        if (p == null) return;

        // Récupération sécurisée de la quantité saisie (fallback à 1.0 si vide)
        double qteSaisie = 1.0;
        try {
            qteSaisie = Double.parseDouble(txtQuantite.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            qteSaisie = 1.0;
        }
         if ( p.quantiteProduit() < qteSaisie ) {
             //erreur
             System.out.println("Quantité icorrecte");
             return;
         }

        // Recherche si le produit est déjà présent dans la liste
        int indexExistant = -1;
        for (int i = 0; i < listeLignes.size(); i++) {
            if (listeLignes.get(i).idProduit().equals(p.idProduit())) {
                indexExistant = i;
                break;
            }
        }

        if (indexExistant != -1) {
            // Le produit existe : cumuler les quantités
            FactureProduitDTO ligneExistante = listeLignes.get(indexExistant);
            double nouvelleQte = ligneExistante.quantiteVenteProduit() + qteSaisie;
            double nouveauSousTotal = p.prixVenteProduit() * nouvelleQte;

            FactureProduitDTO ligneMiseAJour = FactureProduitDTO.builder()
                    .idProduit(p.idProduit())
                    .nomProduit(p.nomProduit())
                    .prixVenteProduit(p.prixVenteProduit())
                    .quantiteVenteProduit(nouvelleQte)
                    .sousTotalVenteProduit(nouveauSousTotal)
                    .build();

            listeLignes.set(indexExistant, ligneMiseAJour);
        } else {
            // Nouveau produit : création d'une nouvelle ligne
            double total = p.prixVenteProduit() * qteSaisie;

            FactureProduitDTO nouvelleLigne = FactureProduitDTO.builder()
                    .idProduit(p.idProduit())
                    .nomProduit(p.nomProduit())
                    .prixVenteProduit(p.prixVenteProduit())
                    .quantiteVenteProduit(qteSaisie)
                    .sousTotalVenteProduit(total)
                    .build();

            listeLignes.add(nouvelleLigne);
        }

        calculerTotal();
        txtQuantite.setText("1"); // Réinitialiser la saisie
    }

        private void calculerTotal() {
            double total = listeLignes.stream().mapToDouble(FactureProduitDTO::sousTotalVenteProduit).sum();
            lblTotalFacture.setText(String.format("Total : %.2f Ar", total));
        }

    @FXML
    private void handleSauvegarder() {
        try {
            // 1. Calcul des nouveaux totaux à partir de la liste modifiée
            double nouveauMontantTotal = listeLignes.stream()
                    .mapToDouble(FactureProduitDTO::sousTotalVenteProduit)
                    .sum();

            double nouvelleMargeTotale = listeLignes.stream()
                    .mapToDouble(FactureProduitDTO::margeVenteProduit)
                    .sum();

            //double avanceActuelle = factureOriginale.avanceFacture();
           // Utilitaire.configureNumericField(txtMontantPayer);
            //double avanceActuelle = Double.valueOf(txtMontantPayer.getText());

            double avanceActuelle = montantRecu.get();
            double nouveauReste = nouveauMontantTotal - montantRecu.get();

            // Determination du statut de paiement selon le nouveau reste
            StatusPaiement nouveauStatut;
            if (nouveauReste <= 0) {
                nouveauStatut = StatusPaiement.PAYE; // Ou votre enum équivalent
            } else if (avanceActuelle > 0) {
                nouveauStatut = StatusPaiement.PARTIEL;
            } else {
                nouveauStatut = StatusPaiement.NON_PAYE;
            }

            // 2. Reconstitution du FactureDTO mis à jour avec le Builder
            FactureDTO factureMiseAJour = FactureDTO.builder()
                    .idFacture(factureOriginale.idFacture())
                    .dateFacture(factureOriginale.dateFacture())
                    .dateModifFacture(LocalDate.now()) // Date de modification du jour
                    .montantFacture(nouveauMontantTotal)
                    .avanceFacture(avanceActuelle)
                    .resteFacture(Math.max(0, nouveauReste))
                    .margeFacture(nouvelleMargeTotale)
                    .modePaiement(cbModePaiement.getValue())
                    .status(nouveauStatut)
                    .client(factureOriginale.client())
                    .factureProduitEntityList(new ArrayList<>(listeLignes)) // Nouvelle liste d'articles
                    .build();

            // 3. Appel du service backend
            factureService.updateFacture(factureOriginale.idFacture(), factureMiseAJour);

            // 4. Fermeture de la fenêtre modale
            ((Stage) tableLignes.getScene().getWindow()).close();

        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

        @FXML
        private void handleAnnuler() {
            ((Stage) tableLignes.getScene().getWindow()).close();
        }

}
