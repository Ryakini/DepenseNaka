package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.CategorieDTO;
import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.service.CategorieService;
import com.ydev.depenseNaka.service.ProduitService;
import com.ydev.depenseNaka.service.UtilisateurService;
import com.ydev.depenseNaka.session.UserSession;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProduitControlleur {

    private final ProduitService produitService;
    private final CategorieService categorieService;
    private final UtilisateurService utilisateurService;
    private final Utilitaire utilitaire;

    // Filtres Recherche
    @FXML private TextField txtSearchId;
    @FXML private TextField txtSearchDesignation;
    @FXML private ComboBox<CategorieDTO> cbSearchCategorie;
    @FXML private TextField txtSearchTaille;
    @FXML private CheckBox chkSearchDisponible;
    @FXML private ComboBox<String> cbSearchStatutActif;

    // TableView & Colonnes
    @FXML private TableView<ProduitDTO> tableProduits;
    @FXML private TableColumn<ProduitDTO, String> colId;
    @FXML private TableColumn<ProduitDTO, String> colNom;
    @FXML private TableColumn<ProduitDTO, String> colCategorie;
    @FXML private TableColumn<ProduitDTO, String> colTaille;
    @FXML private TableColumn<ProduitDTO, Number> colQuantite;
    @FXML private TableColumn<ProduitDTO, Number> colPrixAchat;
    @FXML private TableColumn<ProduitDTO, Number> colPrixVente;
    @FXML private TableColumn<ProduitDTO, Number> colMarge;
    @FXML private TableColumn<ProduitDTO, Boolean> colDisponible;
    @FXML private TableColumn<ProduitDTO, Void> colActions;

    // Formulaire
    @FXML private Label lblFormTitre;
    @FXML private TextField txtIdProduit;
    @FXML private ComboBox<CategorieDTO> cbCategorieForm;
    @FXML private TextField txtNomProduit;
    @FXML private TextField txtTailleProduit;
    @FXML private TextField txtQuantite;
    @FXML private TextField txtPrixAchat;
    @FXML private TextField txtPrixVente;
    @FXML private CheckBox chkDisponible;
    @FXML private CheckBox chkActif;
    @FXML private TextArea txtDescription;
    @FXML private Button btnValider;

    // Injecter les nouveaux composants FXML
    @FXML private Label lblInfoPagination;
    @FXML private Label lblPageCourante;
    @FXML private Button btnPagePrecedente;
    @FXML private Button btnPageSuivante;
    @FXML private ComboBox<Integer> cbLignesParPage;

    // Gestionnaires de pagination
    private int pageCourante = 0;
   // private final int TAILLE_PAGE = 20; // Définissez la taille par page souhaitée
    private int totalPages = 1;

    private final ObservableList<ProduitDTO> produitList = FXCollections.observableArrayList();
    private final ObservableList<CategorieDTO> categorieList = FXCollections.observableArrayList();
    private boolean modeEdition = false;

    DoubleProperty qteSaisie;
    DoubleProperty prixAchatSaisie;
    DoubleProperty prixVenteSaisie;


    @FXML
    public void initialize() {
        initTableColumns();
        initSelectionListener();
        chargerCategories();
        chargerProduits();
        listenerChamp();

        cbSearchStatutActif.getItems().addAll("Tous", "Actifs", "Inactifs");
        cbSearchStatutActif.setValue("Tous");

       qteSaisie = utilitaire.configurerChampMontantDynamique(txtQuantite);
       prixAchatSaisie = utilitaire.configurerChampMontantDynamique(txtPrixAchat);
       prixVenteSaisie = utilitaire.configurerChampMontantDynamique(txtPrixVente);

        // Initialisation des choix pour le nombre de lignes par page
        cbLignesParPage.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        cbLignesParPage.setValue(20);

        // Écouter le changement de sélection dans le ComboBox
        cbLignesParPage.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                this.pageCourante = 0;
                effectuerRecherche();
            }
        });
    }

    private void initTableColumns() {
        // Lambdas directes pour la compatibilité Java Records
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().idProduit()));
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomProduit()));
        colCategorie.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().categorie() != null ? data.getValue().categorie().nomCategorie() : ""));
        colTaille.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().tailleProduit()));
        colQuantite.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().quantiteProduit()));
        colPrixAchat.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().prixAchatProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPrixAchat);
        colPrixVente.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().prixVenteProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPrixVente);
        colMarge.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().margeProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colMarge);

        colDisponible.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().estDisponibleProduit()));
        colDisponible.setCellFactory(CheckBoxTableCell.forTableColumn(colDisponible));

        ajouterColonneActions();
    }

    private void initSelectionListener() {
        tableProduits.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                chargerInFormulaire(selected);
            }
        });
    }

    private void chargerCategories() {
        List<CategorieDTO> categories = categorieService.findAllCategorie();
        categorieList.setAll(categories);
        cbCategorieForm.setItems(categorieList);
        cbSearchCategorie.setItems(categorieList);

        // Formateur personnalisé pour ComboBox
        cbCategorieForm.setConverter(createCategorieStringConverter());
        cbSearchCategorie.setConverter(createCategorieStringConverter());

        //configuration comboboxe pour la recherche
        utilitaire.configureSearchableComboBox(cbSearchCategorie, categories, CategorieDTO::nomCategorie, 6);
        utilitaire.configureSearchableComboBox(cbCategorieForm, categories, CategorieDTO::nomCategorie, 6);
    }

    private void chargerProduits() {
        this.pageCourante = 0;
        List<ProduitDTO> liste = produitService.findListeProduitActif();
        produitList.setAll(liste);
        tableProduits.setItems(produitList);
    }

    @FXML
    private void effectuerRecherche() {
        String id = txtSearchId.getText().trim();
        String des = txtSearchDesignation.getText().trim();
        String cat = cbSearchCategorie.getValue() != null ? cbSearchCategorie.getValue().nomCategorie() : "";

      /*  String cat = "";
        if(cbSearchCategorie.getValue() != null) {
            cat = cbSearchCategorie.getValue().nomCategorie();
        }else if(cbSearchCategorie.getEditor() != null) {
            cat = cbSearchCategorie.getEditor().getText().trim();
        }*/

        String taille = txtSearchTaille.getText().trim();
        Boolean dispo = chkSearchDisponible.isSelected() ? true : null;

        Boolean actif = null;
        String statutChoisi = cbSearchStatutActif.getValue();
        if ("Actifs".equalsIgnoreCase(statutChoisi)) {
            actif = true;
        } else if ("Inactifs".equalsIgnoreCase(statutChoisi)) {
            actif = false;
        }// Si "Tous", actif reste null

        int taillePage = cbLignesParPage.getValue() != null ? cbLignesParPage.getValue() : 20;

        // Récupération via le service (Première page, 20 éléments)
        var pageResult = produitService.findMulticriteriaProduit(id, des, cat, taille, dispo, actif,pageCourante, taillePage);
        produitList.setAll(pageResult.getContent());

        tableProduits.setItems(produitList);

        // Mettre à jour l'état de la pagination
        this.totalPages = pageResult.getTotalPages() > 0 ? pageResult.getTotalPages() : 1;
        mettreAJourPaginationUI(pageResult.getTotalElements());
    }

    private void mettreAJourPaginationUI(long totalElements) {
        int pageAffichage = pageCourante + 1;
        lblPageCourante.setText(String.valueOf(pageAffichage));
        lblInfoPagination.setText(String.format("Page %d sur %d (%d produits)", pageAffichage, totalPages, totalElements));

        // Désactiver le bouton "Précédent" sur la première page
        btnPagePrecedente.setDisable(pageCourante == 0);

        // Désactiver le bouton "Suivant" sur la dernière page
        btnPageSuivante.setDisable(pageCourante >= totalPages - 1);
    }

    private void listenerChamp(){
        txtSearchId.textProperty().addListener((obs, oldVal, newVal) -> {effectuerRecherche();});
        txtSearchDesignation.textProperty().addListener((observable, oldValue, newValue) -> effectuerRecherche());
        cbSearchCategorie.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {effectuerRecherche();});
        txtSearchTaille.textProperty().addListener((observable, oldValue, newValue) -> effectuerRecherche());
    }

    @FXML
    private void reinitialiserRecherche() {
        txtSearchId.clear();
        txtSearchDesignation.clear();
        cbSearchCategorie.setValue(null);
        txtSearchTaille.clear();
        chkSearchDisponible.setSelected(false);
        cbSearchStatutActif.setValue(null);
        this.pageCourante = 0;
        chargerProduits();
    }

    @FXML
    private void validerFormulaire() {
        if (cbCategorieForm.getValue() == null) {
            utilitaire.afficherAvertissement("Champ requis"," Veuillez sélectionner une catégorie.");
            return;
        }
        if (txtNomProduit.getText() == null || txtNomProduit.getText().isBlank()) {
            utilitaire.afficherAvertissement("Champ Requis","La désignation du produit est obligatoire.");
            return;
        }

        try {
                    ProduitDTO dto = ProduitDTO.builder()
                    .idProduit(txtIdProduit.getText())
                    .nomProduit(txtNomProduit.getText().trim())
                    .tailleProduit(txtTailleProduit.getText().trim())
                    .quantiteProduit(qteSaisie.get())
                    .prixAchatProduit(prixAchatSaisie.get())
                    .prixVenteProduit(prixVenteSaisie.get())
                    .margeProduit(prixVenteSaisie.get() - prixAchatSaisie.get())
                    .estDisponibleProduit(chkDisponible.isSelected())
                    .estActifProduit(chkActif.isSelected())
                    .descriptionProduit(txtDescription.getText().trim())
                    .categorie(cbCategorieForm.getValue())
                    .build();

            if (modeEdition) {
                produitService.updateProduit(dto.idProduit(), dto);
                utilitaire.afficherInformation("Succès","Les informations du produit ont été mises à jour avec succés");
                reinitialiserFormulaire();
                chargerProduits();
            } else {
                produitService.createProduit(dto);
                utilitaire.afficherInformation("Succès","Un nouveau produit a été créé ");
                reinitialiserFormulaire();
                chargerProduits();
            }

            reinitialiserFormulaire();
            chargerProduits();

        } catch (NumberFormatException e) {
            utilitaire.afficherErreur("Format numérique invalide", "Vérifiez la quantité et les prix.");
        } catch (Exception e) {
            utilitaire.afficherErreur("Erreur d'enregistrement", "Une erreur se produit car : "+e.getMessage());
        }
    }

    @FXML
    private void reinitialiserFormulaire() {
        txtIdProduit.clear();
        cbCategorieForm.setValue(null);
        txtNomProduit.clear();
        txtTailleProduit.clear();
        txtQuantite.clear();
        txtPrixAchat.clear();
        txtPrixVente.clear();
        chkDisponible.setSelected(true);
        chkActif.setSelected(true);
        txtDescription.clear();

        lblFormTitre.setText("Nouveau Produit");
        modeEdition = false;
        tableProduits.getSelectionModel().clearSelection();
    }

    private void chargerInFormulaire(ProduitDTO produit) {
        UserSession session = UserSession.getInstance();
        String idUser = session.getIdUtilisateur();
        boolean isAdmin = utilisateurService.isAdmin(idUser);
        indisponibleModification(isAdmin);

        modeEdition = true;
        lblFormTitre.setText("Modifier Produit");

        txtIdProduit.setText(produit.idProduit());
        cbCategorieForm.setValue(produit.categorie());
        txtNomProduit.setText(produit.nomProduit());
        txtTailleProduit.setText(produit.tailleProduit());
        txtQuantite.setText(String.valueOf(produit.quantiteProduit()));
        txtPrixAchat.setText(String.valueOf(produit.prixAchatProduit()));
        txtPrixVente.setText(String.valueOf(produit.prixVenteProduit()));
        chkDisponible.setSelected(produit.estDisponibleProduit());
        chkActif.setSelected(produit.estActifProduit());
        txtDescription.setText(produit.descriptionProduit());

        btnValider.setText("Modifier");
        btnValider.setGraphic( new FontIcon("fas-edit"));
    }

    private void indisponibleModification(boolean isAdmin){
        if(!isAdmin){
            txtQuantite.setEditable(false);
            txtPrixAchat.setEditable(false);
            txtPrixVente.setEditable(false);
        }
    }

    private void ajouterColonneActions() {
        UserSession session = UserSession.getInstance();
        String idUser = session.getIdUtilisateur();
        boolean isAdmin = utilisateurService.isAdmin(idUser);

        colActions.setVisible(isAdmin);

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button();
            private final HBox container = new HBox(btnDelete);

            {
                btnDelete.getStyleClass().addAll("flat", "danger", "button-icon");
                btnDelete.setGraphic(new FontIcon("fas-trash-alt"));
                container.setStyle("-fx-alignment: CENTER;");

                btnDelete.setOnAction(e -> {
                    ProduitDTO p = getTableView().getItems().get(getIndex());
                    supprimerProduit(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void supprimerProduit(ProduitDTO produit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de desactivation");
       // confirm.setHeaderText("Desactivation du produit");
       // confirm.setContentText("Desactiver le produit : " + produit.nomProduit()+ " ?");

        confirm.setHeaderText("Suppression du produit");
        confirm.setContentText("Supprimer le produit : " + produit.nomProduit()+ " ?");

        utilitaire.styleModernAlert(confirm, "fas-exclamation-triangle", true);

        // Personnalisation des libellés des boutons
        Button okBtn = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.setText("Oui, desactiver");

        Button cancelBtn = (Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) cancelBtn.setText("Annuler");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    produitService.deleteProduit(produit.idProduit());
                    utilitaire.afficherInformation("Succès", "Le produit a été supprimé.");
                    reinitialiserFormulaire();
                    chargerProduits();
                } catch (Exception e) {
                    utilitaire.afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    // --- GESTION CRUD CATÉGORIES (Fenetre Modale) ---

    @FXML
    private void ouvrirGestionCategories() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(tableProduits.getScene().getWindow()); //parent
        dialog.setAlwaysOnTop(false);
        dialog.setResizable(false);

        // --- BARRE D'EN-TÊTE DU DIALOGUE (Titre + Bouton Fermer X) ---
        Label lblTitre = new Label("Gestion des Catégories");
        lblTitre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnClose = new Button();
        btnClose.setGraphic(new FontIcon("fas-times"));
        btnClose.getStyleClass().addAll("flat", "danger", "small");
        btnClose.setOnAction(e -> dialog.close());

        HBox headerBar = new HBox(lblTitre, btnClose);
        HBox.setHgrow(lblTitre, Priority.ALWAYS);
        lblTitre.setMaxWidth(Double.MAX_VALUE);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setPadding(new Insets(10, 15, 10, 15));
        headerBar.setStyle("-fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0;");

        // --- TABLEAU DES CATÉGORIES (À GAUCHE) ---
        TableView<CategorieDTO> tableCat = new TableView<>();
        TableColumn<CategorieDTO, String> colNomCat = new TableColumn<>("Catégorie");
        colNomCat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomCategorie()));

        TableColumn<CategorieDTO, String> colDescCat = new TableColumn<>("Description");
        colDescCat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().descriptionCategorie()));

        tableCat.getColumns().addAll(colNomCat, colDescCat);
        tableCat.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCat.setMaxHeight(Double.MAX_VALUE);
        tableCat.setItems(categorieList);

        // --- FORMULAIRE ET CHAMPS DE SAISIE (À DROITE) ---
        Label lblFormTitre = new Label("Nouvelle Catégorie");
        lblFormTitre.setStyle("-fx-font-weight: bold;");

        TextField txtNomCat = new TextField();
        txtNomCat.setPromptText("Nom de la catégorie");

        TextArea txtDescCat = new TextArea();
        txtDescCat.setPromptText("Description...");
        txtDescCat.setPrefRowCount(3);
        txtDescCat.setWrapText(true);

        Button btnValider = new Button("Ajouter", new FontIcon("fas-plus"));
        btnValider.getStyleClass().add("accent");

        Button btnSupprimer = new Button("Supprimer", new FontIcon("fas-trash-alt"));
        btnSupprimer.getStyleClass().addAll("flat", "danger");
        btnSupprimer.setContentDisplay(ContentDisplay.LEFT);
        btnSupprimer.setGraphicTextGap(8);
        btnSupprimer.setUnderline(false);
        btnSupprimer.setDisable(true); // Désactivé si aucune sélection

        Button btnAnnuler = new Button("Annuler", new FontIcon("fas-undo"));
        btnAnnuler.getStyleClass().add("flat");
        btnAnnuler.setUnderline(false);

        // Variable pour enregistrer l'ID en mode édition
        final String[] selectedIdCategorie = { null };

        // Action de réinitialisation du formulaire
        Runnable resetForm = () -> {
            txtNomCat.clear();
            txtDescCat.clear();
            selectedIdCategorie[0] = null;
            lblFormTitre.setText("Nouvelle Catégorie");
            btnValider.setText("Ajouter");
            btnValider.setGraphic(new FontIcon("fas-plus"));
            btnSupprimer.setDisable(true);
            tableCat.getSelectionModel().clearSelection();
        };

        // Charger les données sélectionnées dans le formulaire (Update/Delete)
        tableCat.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                selectedIdCategorie[0] = selected.idCategorie();
                txtNomCat.setText(selected.nomCategorie());
                txtDescCat.setText(selected.descriptionCategorie() != null ? selected.descriptionCategorie() : "");
                lblFormTitre.setText("Modifier Catégorie");
                btnValider.setText("Enregistrer");
                btnValider.setGraphic(new FontIcon("fas-save"));
                btnSupprimer.setDisable(false);
            }
        });

        btnAnnuler.setOnAction(e -> resetForm.run());

        // --- ACTIONS : CRÉATION / MISE À JOUR ---
        btnValider.setOnAction(e -> {
            String nom = txtNomCat.getText().trim();
            String desc = txtDescCat.getText().trim();

            if (nom.isBlank()) {
                utilitaire.afficherAvertissement("Champ Requis", "Le nom de la catégorie est obligatoire.");
                return;
            }

            try {
                CategorieDTO dto = CategorieDTO.builder()
                        .idCategorie(selectedIdCategorie[0])
                        .nomCategorie(nom)
                        .descriptionCategorie(desc)
                        .build();

                if (selectedIdCategorie[0] == null) {
                    // Création
                    categorieService.createCategorie(dto);
                } else {
                    // Mise à jour (Update)
                    categorieService.updateCategorie(selectedIdCategorie[0], dto);
                }

                chargerCategories(); // Rafraîchit les ComboBox et la Table
                resetForm.run();
            } catch (Exception ex) {
                utilitaire.afficherErreur("Erreur Opération", "Une erreur se produit car : "+ex.getMessage());
            }
        });

        // --- ACTION : SUPPRESSION ---
        btnSupprimer.setOnAction(e -> {
            if (selectedIdCategorie[0] != null) {
                try {
                    categorieService.deleteCategorie(selectedIdCategorie[0]);
                    chargerCategories();
                    resetForm.run();
                } catch (Exception ex) {
                    utilitaire.afficherErreur("Suppression Impossible", "Il est impossible de la supprimer car: "+ex.getMessage());
                }
            }
        });

        // Layout du Formulaire (Côté Droit)
        VBox formBox = new VBox(10);
        formBox.setPrefWidth(240);
        formBox.getChildren().addAll(
                lblFormTitre,
                new Label("Nom *"), txtNomCat,
                new Label("Description"), txtDescCat,
                new HBox(8, btnValider, btnAnnuler),
                btnSupprimer
        );

        // Disposition globale : Table à gauche, Formulaire à droite
        HBox contentBox = new HBox(15, tableCat, formBox);
        HBox.setHgrow(tableCat, Priority.ALWAYS);
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        contentBox.setPadding(new Insets(15));

        // Conteneur Principal
        VBox mainLayout = new VBox(headerBar, contentBox);

        Scene scene = new Scene(mainLayout, 750, 400);
        Scene parentScene = tableProduits.getScene();

        if (parentScene != null) {
            // A. Copie de toutes les feuilles de style CSS (ex: style.css, dark-theme.css, etc.)
            scene.getStylesheets().addAll(parentScene.getStylesheets());

            // B. Copie de la classe de thème (ex: "dark-mode" ou "light-mode") appliquée sur le root parent
            if (parentScene.getRoot() != null) {
                mainLayout.getStyleClass().addAll(parentScene.getRoot().getStyleClass());
            }
        } else {
            // Option de secours (Fallback) si la scène parente n'est pas encore accessible
            String cssPath = getClass().getResource("/vue/cssFile/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        }

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private javafx.util.StringConverter<CategorieDTO> createCategorieStringConverter() {
        return new javafx.util.StringConverter<>() {
            @Override
            public String toString(CategorieDTO cat) {
                return cat != null ? cat.nomCategorie() : "";
            }

            @Override
            public CategorieDTO fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                // Recherche de la catégorie correspondante dans la liste chargée
                return categorieList.stream()
                        .filter(c -> c.nomCategorie() != null && c.nomCategorie().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null); // Ou vous pouvez retourner un DTO temporaire si besoin : CategorieDTO.builder().nomCategorie(string).build()
            }
        };
    }


    @FXML
    private void pagePrecedente() {
        if (pageCourante > 0) {
            pageCourante--;
            effectuerRecherche();
        }
    }

    @FXML
    private void pageSuivante() {
        if (pageCourante < totalPages - 1) {
            pageCourante++;
            effectuerRecherche();
        }
    }
}
