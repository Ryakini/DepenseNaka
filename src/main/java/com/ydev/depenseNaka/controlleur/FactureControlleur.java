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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class FactureControlleur {

    private final FactureService factureService;
    private final Utilitaire utilitaire;
    private final ProduitService produitService;

    // Filtres
    @FXML private TextField txtSearchId;
    @FXML private TextField txtSearchNom;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private ComboBox<StatusPaiement> cbStatus;
    @FXML private ComboBox<ModePaiement> cbModePaiement;

    // Table Principale
    @FXML private TableView<FactureDTO> tableFactures;
    @FXML private TableColumn<FactureDTO, Void> colACtion;
    @FXML private TableColumn<FactureDTO, String> colId;
    @FXML private TableColumn<FactureDTO, String> colDate;
    @FXML private TableColumn<FactureDTO, String> colClient;
    @FXML private TableColumn<FactureDTO, Number> colMontant;
    @FXML private TableColumn<FactureDTO, Number> colAvance;
    @FXML private TableColumn<FactureDTO, Number> colReste;
    @FXML private TableColumn<FactureDTO, String> colMode;
    @FXML private TableColumn<FactureDTO, String> colStatus;

    // Panneau Détail
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailClient;
    @FXML private Label lblDetailDate;
    @FXML private Label lblDetailReste;
    @FXML private TableView<FactureProduitDTO> tableLignesFacture;
    @FXML private TableColumn<FactureProduitDTO, String> colLigneProduit;
    @FXML private TableColumn<FactureProduitDTO, Number> colLignePrix;
    @FXML private TableColumn<FactureProduitDTO, Number> colLigneQte;
    @FXML private TableColumn<FactureProduitDTO, Number> colLigneTotal;

    @FXML private TextField txtEditAvance;
    @FXML private ComboBox<ModePaiement> cbEditModePaiement;

    // --- VARIABLES D'ÉTAT DE LA PAGINATION ---
    @FXML private ComboBox<Integer> cbPageSize;
    @FXML private Label lblPageInfo;
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;
    private int currentPage = 0;
    private int totalPages = 1;

    private final ObservableList<FactureDTO> facturesList = FXCollections.observableArrayList();
    private final ObservableList<FactureProduitDTO> lignesList = FXCollections.observableArrayList();
    private FactureDTO factureSelectionnee;
    // Passer à false lors du déploiement en production
    private static final boolean IS_DEV_MODE = false;

    DoubleProperty montantRecu;


    @FXML
    public void initialize() {
        initFiltres();
        initTableFactures();
        initColonneActions();
        initTableLignes();
        chargerFactures();
        initListener();

        montantRecu = utilitaire.configurerChampMontantDynamique(txtEditAvance);

        // Initialiser le ComboBox pour les tailles de page
        cbPageSize.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        cbPageSize.setValue(10); // Valeur par défaut

        // Écouter le changement de taille de page pour recharger la première page
        cbPageSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                currentPage = 0;
                chargerFactures();
            }
        });
    }

    private void initFiltres() {
        cbStatus.setItems(FXCollections.observableArrayList(StatusPaiement.values()));
        cbModePaiement.setItems(FXCollections.observableArrayList(ModePaiement.values()));
        cbEditModePaiement.setItems(FXCollections.observableArrayList(ModePaiement.values()));
    }

    private void initTableFactures() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().idFacture()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().dateFacture() != null ? d.getValue().dateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
        colClient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().client() != null ? d.getValue().client().nomClient() + " " +
                                                (d.getValue().client().prenomClient() != null ? d.getValue().client().prenomClient() : "") : "Client Inconnu"));
        colMontant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().montantFacture()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colMontant);
        colAvance.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().avanceFacture()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colAvance);
        colReste.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().resteFacture()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colReste);
        colMode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().modePaiement() != null ? d.getValue().modePaiement().name() : ""));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status() != null ? d.getValue().status().name() : ""));
        utilitaire.appliquerColorationStatut(colStatus);

        // Événement Clic sur une ligne
        tableFactures.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                afficherDetailsFacture(newSel);
            }
        });
    }

    private void initTableLignes() {
        colLigneProduit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nomProduit()));
        colLignePrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixVenteProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colLignePrix);
        colLigneQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().quantiteVenteProduit()));
        colLigneTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().sousTotalVenteProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colLigneTotal);
        tableLignesFacture.setItems(lignesList);
    }

    private void initColonneActions() {
        colACtion.setCellFactory(param -> new TableCell<FactureDTO, Void>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox container = new HBox(8, btnEdit);

            {
                // Styling & Icônes du bouton Modifier
                btnEdit.getStyleClass().addAll("accent", "button-icon","flat");
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(13);
                btnEdit.setGraphic(editIcon);
                btnEdit.setTooltip(new Tooltip("Modifier la facture"));

                // Si on est en mode Dev, on ajoute le bouton Supprimer
                if (IS_DEV_MODE) {
                    btnDelete.getStyleClass().addAll("danger", "button-icon", "flat");
                    FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                    deleteIcon.setIconSize(13);
                    btnDelete.setGraphic(deleteIcon);
                    btnDelete.setTooltip(new Tooltip("Supprimer (Mode DEV)"));
                    container.getChildren().add(btnDelete);
                }

                container.setAlignment(Pos.CENTER);

                // Action du bouton Modifier : Sélectionne la ligne et remplit la zone de mise à jour
                btnEdit.setOnAction(e -> {
                    FactureDTO facture = getTableView().getItems().get(getIndex());
                    tableFactures.getSelectionModel().select(facture);
                    ouvrirModalModification(facture);
                });

                // Action du bouton Supprimer (Dev Mode)
                btnDelete.setOnAction(e -> {
                    FactureDTO facture = getTableView().getItems().get(getIndex());
                    supprimerFacture(facture);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
               // setGraphic(empty ? null : container);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    // 1. Récupération de la facture associée à cette ligne
                    FactureDTO facture = getTableView().getItems().get(getIndex());

                    // 2. Vérification du statut
                    if (facture != null && facture.status() != null) {

                        // Remplacez "PAYE" ou StatutEnum.PAYE selon le type de votre champ status
                        boolean estPaye = "PAYE".equalsIgnoreCase(facture.status().toString());

                        // setDisable(true) rend le bouton grisé, visible mais non cliquable
                        btnEdit.setDisable(estPaye);

                        // Optionnel : Mise à jour du Tooltip pour expliquer la désactivation
                        if (estPaye) {
                            btnEdit.setTooltip(new Tooltip("Impossible de modifier une facture payée"));
                        } else {
                            btnEdit.setTooltip(new Tooltip("Modifier cette facture"));
                        }
                    } else {
                        btnEdit.setDisable(false);
                    }

                    setGraphic(container);
                }
            }
        });
    }

    private void supprimerFacture(FactureDTO facture) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression (DEV)");
        confirm.setHeaderText("Supprimer la facture N° " + facture.idFacture() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                factureService.deleteFacture(facture.idFacture());
                tableFactures.getItems().remove(facture);
            }
        });
    }

    private void chargerFactures() {
        this.currentPage = 0;
        filtrerFactures();
    }

    private void initListener(){
        txtSearchId.textProperty().addListener((observable, oldValue, newValue) ->
                filtrerFactures()
        );

        txtSearchNom.textProperty().addListener((observable, oldValue, newValue) ->
            filtrerFactures()
        );

        cbStatus.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filtrerFactures();
        });

        cbEditModePaiement.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filtrerFactures();
        });

    }

    @FXML
    private void filtrerFactures() {
        String idSearch = (txtSearchId.getText() != null && !txtSearchId.getText().isBlank()) ? txtSearchId.getText().trim() : null;
        String nomSearch = (txtSearchNom.getText() != null && !txtSearchNom.getText().isBlank() ) ? txtSearchNom.getText().trim() : "";
        LocalDate dDebut = dpDateDebut.getValue();
        LocalDate dFin = dpDateFin.getValue();
        StatusPaiement status = cbStatus.getValue();
        ModePaiement mode = cbModePaiement.getValue();
        int pageSize = (cbPageSize.getValue() != null) ? cbPageSize.getValue() : 10;

        Page<FactureDTO> pageResult = factureService.findMultiCriteriaFacture(idSearch, nomSearch,dDebut, dFin, status, mode, currentPage, pageSize);
        facturesList.setAll(pageResult.getContent());
        tableFactures.setItems(facturesList);

        // Mise à jour de l'état de la pagination
        this.totalPages = pageResult.getTotalPages() > 0 ? pageResult.getTotalPages() : 1;
        long totalElements = pageResult.getTotalElements();

        // Mise à jour des contrôles GUI (Labels et Boutons Précédent/Suivant)
        lblPageInfo.setText(String.format("Page %d / %d (%d éléments)", currentPage + 1, totalPages, totalElements));
        btnPrevPage.setDisable(currentPage <= 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1);

    }

    @FXML
    private void reinitialiserFiltres() {
        txtSearchId.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        cbStatus.setValue(null);
        cbModePaiement.setValue(null);
        this.currentPage = 0;
        filtrerFactures();
    }

    private void afficherDetailsFacture(FactureDTO dto) {
        this.factureSelectionnee = dto;
        lblDetailId.setText(dto.idFacture());
        lblDetailClient.setText(dto.client() != null ? dto.client().nomClient() : "-");
        lblDetailDate.setText(dto.dateFacture() != null ? dto.dateFacture().toString() : "");
        lblDetailReste.setText(dto.resteFacture() != 0 ? utilitaire.appliquerSeparateurMillierAr(dto.resteFacture()) : "0.0");

        txtEditAvance.setText(String.valueOf(dto.avanceFacture()));
        cbEditModePaiement.setValue(dto.modePaiement());

        if (dto.factureProduitEntityList() != null) {
            lignesList.setAll(dto.factureProduitEntityList());
        } else {
            lignesList.clear();
        }
    }

    //fenetre modale pour la modification
    private void ouvrirModalModification(FactureDTO facture) {
        try {
            // 1. Chargement de la vue d'édition
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/FactureEditView.fxml"));

            // 2. Permettre à Spring d'injecter les dépendances dans FactureEditControlleur
           // loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // 3. Récupération du contrôleur enfant pour lui passer les données
            FactureEditControlleur editController = loader.getController();

            // Récupérer la liste de tous les produits (via le service) pour alimenter la ComboBox
            List<ProduitDTO> produitsDisponibles = produitService.findListeProduitActif();
           // editController.initData(facture, produitsDisponibles);
            editController.initData(facture, produitsDisponibles, this.factureService, this.utilitaire);

            // 4. Configuration de la fenêtre Modale (Stage)
            Stage modalStage = new Stage();
            modalStage.setTitle("Modifier la Facture N° " + facture.idFacture());

            modalStage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre arrière-plan
            modalStage.initOwner(tableFactures.getScene().getWindow()); // Fenêtre parente
            modalStage.initStyle(StageStyle.UNDECORATED); // Style sans bordure Windows si souhaité

            Scene scene = new Scene(root);

            // --- INHÉRITANCE DU THÈME / CSS PARENT ---
            scene.getStylesheets().addAll(tableFactures.getScene().getStylesheets());

            modalStage.setScene(scene);

            // 5. Afficher et attendre la fermeture de la modale
            modalStage.showAndWait();

            // 6. Rafraîchir la table des factures après la fermeture pour voir les changements
            chargerFactures();

        } catch (Exception e) {
            utilitaire.afficherErreur("Erreur", "Erreur lors de l'ouverture de la modale de modification :"+ e.getMessage());
            System.err.println("Erreur lors de l'ouverture de la modale de modification :");
            e.printStackTrace();
        }
    }

    @FXML
    private void enregistrerModificationFacture() {
        if (factureSelectionnee == null) {
            //afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une facture à modifier.");
            utilitaire.afficherAvertissement("Auccune selection", "Veuillez sélectionner une facture à modifier.");
            return;
        }

        try {
            //double nouvelleAvance = Double.parseDouble(txtEditAvance.getText().trim());
           // DoubleProperty nouvelleAvance = utilitaire.configurerChampMontantDynamique(txtEditAvance);
            ModePaiement nouveauMode = cbEditModePaiement.getValue();

            FactureDTO dtoUpdated = FactureDTO.builder()
                    .client(factureSelectionnee.client())
                    .avanceFacture(montantRecu.get())
                    .modePaiement(nouveauMode)
                    .factureProduitEntityList(factureSelectionnee.factureProduitEntityList())
                    .build();

            // Appel au Service Spring
            FactureDTO resultat = factureService.updateFacture(factureSelectionnee.idFacture(), dtoUpdated);

           // afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Facture mise à jour avec succès !");
            utilitaire.afficherInformation("Succès", "Facture mise à jour avec succès !");
            filtrerFactures();
            afficherDetailsFacture(resultat);

        } catch (NumberFormatException e) {
           // afficherAlerte(Alert.AlertType.ERROR, "Format invalide", "Veuillez saisir un montant valide pour l'avance.");
            utilitaire.afficherErreur("Format invalide","Veuillez saisir un montant valide pour l'avance.");
        } catch (Exception e) {
           // afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            utilitaire.afficherErreur("Erreur", e.getMessage());
        }
    }

    @FXML
    private void imprimerTicketCaisse() {
        if (factureSelectionnee == null) {
            utilitaire.afficherAvertissement("Sélection requise", "Sélectionnez une facture à imprimer.");
            return;
        }

        String contenuTicket = genererTexteTicket(factureSelectionnee);

        Text textTicket = new Text(contenuTicket);
        textTicket.setFont(Font.font("Monospaced", 11));
        textTicket.getStyleClass().add("text-primary");

        // Envelopper le Text dans un conteneur VBox pour le centrer horizontalement
        VBox ticketContainer = new VBox(textTicket);
        ticketContainer.setAlignment(Pos.CENTER); // Centrage horizontal en haut
        ticketContainer.setPadding(new Insets(10));


        ScrollPane scrollPane = new ScrollPane(ticketContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(340, 480);

        // On retire le style inline pour laisser le CSS / thème gérer l'arrière-plan du ScrollPane
        scrollPane.getStyleClass().add("edge-to-edge");

        // 1. --- BARRE DE TITRE ---
        Label lblTitre = new Label("Aperçu avant impression - Facture N° " + factureSelectionnee.idFacture());
        lblTitre.getStyleClass().addAll("title", "h5"); // Classes standard des thèmes modernes (ex: AtlantaFX)

        Button btnFermerIcone = new Button("✕");
        btnFermerIcone.getStyleClass().addAll("flat", "danger", "small");
        btnFermerIcone.setUnderline(false);

        HBox barreTitre = new HBox(lblTitre, new Region(), btnFermerIcone);
        HBox.setHgrow(barreTitre.getChildren().get(1), Priority.ALWAYS); // Espaceur dynamique
        barreTitre.setAlignment(Pos.CENTER_LEFT);
        barreTitre.setPadding(new Insets(5, 5, 5, 10));

        // 2. --- BOUTONS D'ACTION ---
        Button btnImprimer = new Button("Imprimer");
        btnImprimer.getStyleClass().addAll("accent", "success");

        Button btnAnnuler = new Button("Fermer");
        btnAnnuler.getStyleClass().addAll("flat","danger");
        btnAnnuler.setUnderline(false);

        HBox actionsBox = new HBox(10, btnImprimer, btnAnnuler);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        // 3. --- LAYOUT ---
        VBox layout = new VBox(10, barreTitre, scrollPane, actionsBox);
        layout.setPadding(new Insets(15));


        layout.getStyleClass().addAll("dialog-pane", "modal-dialog");

        // 4. --- FENÊTRE ET SCÈNE ---
        Stage previewStage = new Stage();
        Scene parentScene = tableFactures.getScene();

        previewStage.initOwner(parentScene.getWindow());
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.initStyle(StageStyle.UNDECORATED);

        btnImprimer.setOnAction(e -> {
            lancerImpression(textTicket);
            previewStage.close();
        });
        btnAnnuler.setOnAction(e -> previewStage.close());
        btnFermerIcone.setOnAction(e -> previewStage.close());

        Scene scene = new Scene(layout);

        // 5. --- INJECTION CRITIQUE DU THÈME ET DU CSS PARENT ---
        if (parentScene != null) {
            // A. Copie des fichiers .css chargés sur la scène principale
            scene.getStylesheets().addAll(parentScene.getStylesheets());

            // C'est ici que sont définies les variables de thème (ex: .dark, .primer-dark, etc.)
            if (parentScene.getRoot() != null) {
                layout.getStyleClass().addAll(parentScene.getRoot().getStyleClass());

                // C. Copie du style inline du Root s'il y a un gestionnaire de thème dynamique
                if (parentScene.getRoot().getStyle() != null && !parentScene.getRoot().getStyle().isBlank()) {
                    layout.setStyle(parentScene.getRoot().getStyle());
                }
            }
        }

        // Gestion du déplacement à la souris pour la fenêtre Undecorated
        final double[] delta = new double[2];
      /*  barreTitre.setOnMousePressed(m -> {
            delta[0] = previewStage.getX() - m.getScreenX();
            delta[1] = previewStage.getY() - m.getScreenY();
        });
        barreTitre.setOnMouseDragged(m -> {
            previewStage.setX(m.getScreenX() + delta[0]);
            previewStage.setY(m.getScreenY() + delta[1]);
        });*/

        previewStage.setScene(scene);
        previewStage.showAndWait();
    }

    private String genererTexteTicket(FactureDTO facture) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        String dt = sdf.format(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------------- \n");
        sb.append(" DEPENSENAKA | Toamasina ").append(dt).append("\n");
        sb.append("          Doit: ").append(facture.client() != null ? facture.client().nomClient() : "Passager").append("\n");
        sb.append("-------------------------------------------- \n");
        sb.append("     Désignation\n");
        sb.append("       Qte" + "       PU"+"           Montant(Ar)  \n");
        sb.append("-------------------------------------------- \n");

        if (facture.factureProduitEntityList() != null) {
            for (FactureProduitDTO p : facture.factureProduitEntityList()) {
                sb.append(String.format("     %-25s\n", p.nomProduit()));
                sb.append(String.format("        %-6.0f %10.2f %12.2f\n",
                        p.quantiteVenteProduit(),
                        p.prixVenteProduit(),
                        p.sousTotalVenteProduit()));
            }
        }

        sb.append("-------------------------------------------- \n");
        sb.append(String.format("  Prix total :               %.2f \n", facture.montantFacture()));
        sb.append(String.format("  Avance :                   %.2f \n", facture.avanceFacture()));
        sb.append(String.format("  Reste :                    %.2f \n", facture.resteFacture()));
        sb.append("-------------------------------------------- \n");
        sb.append("               | Merci beaucoup |            \n");
        sb.append("-------------------------------------------- \n");

        return sb.toString();
    }

   private void lancerImpression(Text textNode) {
       PrinterJob job = PrinterJob.createPrinterJob();
       if (job != null && job.showPrintDialog(tableFactures.getScene().getWindow())) {
           textNode.setStyle("-fx-fill: black;"); // Passer en noir pour le papier
           boolean success = job.printPage(textNode);
           if (success) {
               job.endJob();
           }
       }
   }

    //----------pagination
    @FXML
    private void pagePrecedente() {
        if (currentPage > 0) {
            currentPage--;
            chargerFactures();
        }
    }

    @FXML
    private void pageSuivante() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            chargerFactures();
        }
    }

}