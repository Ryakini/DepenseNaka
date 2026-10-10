package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.session.UserSession;
import com.ydev.depenseNaka.dto.CommandeDTO;
import com.ydev.depenseNaka.dto.DepenseDTO;
import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.dto.UtilisateurProduitDTO;
import com.ydev.depenseNaka.model.StatusCommande;
import com.ydev.depenseNaka.model.UtilisateurEntity;
import com.ydev.depenseNaka.service.CommandeService;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandeControlleur {

    private final CommandeService commandeService;
    private final ProduitService produitService;
    private final Utilitaire utilitaire;

    @FXML private TabPane tabPaneCommande;

    // --- ONGLET 1 : PREPARATION ---
    @FXML private TextField txtPrepFournisseur;
    @FXML private DatePicker dpPrepDateArrivee;
    @FXML private TextField txtPrepCommentaire;

    @FXML private ComboBox<ProduitDTO> cbPrepProduit;
    @FXML private TextField txtPrepPrixProduit;
    @FXML private TextField txtPrepQteProduit;
    @FXML private TableView<UtilisateurProduitDTO> tablePrepProduits;
    @FXML private TableColumn<UtilisateurProduitDTO, String> colPrepProdNom;
    @FXML private TableColumn<UtilisateurProduitDTO, Number> colPrepProdPrix;
    @FXML private TableColumn<UtilisateurProduitDTO, Double> colPrepProdQte;
    @FXML private TableColumn<UtilisateurProduitDTO, Number> colPrepProdTotal;
    @FXML private TableColumn<UtilisateurProduitDTO, Void> colPrepProdAction;

    @FXML private TextField txtPrepDepLibelle;
    @FXML private TextField txtPrepDepPrix;
    @FXML private TextField txtPrepDepQte;
    @FXML private TableView<DepenseDTO> tablePrepDepenses;
    @FXML private TableColumn<DepenseDTO, String> colPrepDepLibelle;
    @FXML private TableColumn<DepenseDTO, Number> colPrepDepPrix;
    @FXML private TableColumn<DepenseDTO, Double> colPrepDepQte;
    @FXML private TableColumn<DepenseDTO, Number> colPrepDepMontant;
    @FXML private TableColumn<DepenseDTO, Void> colPrepDepAction;

    @FXML private Label lblPrepTotalProd;
    @FXML private Label lblPrepTotalDep;
    @FXML private Label lblPrepGrandTotal;
    @FXML private Button btnV;
    @FXML private Button btnA;

    private final ObservableList<UtilisateurProduitDTO> prepProduitsList = FXCollections.observableArrayList();
    private final ObservableList<DepenseDTO> prepDepensesList = FXCollections.observableArrayList();

    // --- ONGLET 2 : PREPAREES ---
    @FXML private TableView<CommandeDTO> tableCmdEnAttente;
    @FXML private TableColumn<CommandeDTO, String> colAttId;
    @FXML private TableColumn<CommandeDTO, String> colAttDate;
    @FXML private TableColumn<CommandeDTO, String> colAttFournisseur;
    @FXML private TableColumn<CommandeDTO, String> colAttPrevu;
    @FXML private TableColumn<CommandeDTO, Number> colAttMontant;
    @FXML private TableColumn<CommandeDTO, String> colAttCommentaire;
    @FXML private TableColumn<CommandeDTO, Void> colAttActions;

    private final ObservableList<CommandeDTO> enAttenteList = FXCollections.observableArrayList();

    // --- ONGLET 3 : VALIDÉES & DÉTAILS ---
    @FXML private TextField txtSearchId;
    @FXML private TextField txtSearchFournisseur;
    @FXML private DatePicker dpSearchDebut;
    @FXML private DatePicker dpSearchFin;
    @FXML private ComboBox<StatusCommande> cbSearchStatus;

    @FXML private TableView<CommandeDTO> tableCmdValidees;
    @FXML private TableColumn<CommandeDTO, String> colValId;
    @FXML private TableColumn<CommandeDTO, String> colValDate;
    @FXML private TableColumn<CommandeDTO, String> colValDateModif;
    @FXML private TableColumn<CommandeDTO, String> colValFournisseur;
    @FXML private TableColumn<CommandeDTO, Number> colValMontant;
    @FXML private TableColumn<CommandeDTO, String> colValStatus;
    @FXML private TableColumn<CommandeDTO, String> colValCreateur;
    @FXML private TableColumn<CommandeDTO, String> colValModificateur;

    @FXML private Label lblValDetailHeader;
    @FXML private TableView<UtilisateurProduitDTO> tableValDetailProduits;
    @FXML private TableColumn<UtilisateurProduitDTO, String> colValProdNom;
    @FXML private TableColumn<UtilisateurProduitDTO, Number> colValProdPrix;
    @FXML private TableColumn<UtilisateurProduitDTO, Number> colValProdQte;
    @FXML private TableColumn<UtilisateurProduitDTO, Number> colValProdTotal;

    @FXML private TableView<DepenseDTO> tableValDetailDepenses;
    @FXML private TableColumn<DepenseDTO, String> colValDepLibelle;
    @FXML private TableColumn<DepenseDTO, Number> colValDepPrix;
    @FXML private TableColumn<DepenseDTO, Number> colValDepQte;
    @FXML private TableColumn<DepenseDTO, Number> colValDepMontant;

    private String commandeEnCoursId = null;

    @FXML private ComboBox<Integer> cbValPageSize;
    @FXML private Label lblValPageInfo;
    @FXML private Button btnValPrevPage;
    @FXML private Button btnValNextPage;

    // --- Variables d'état de la pagination ---
    private int valCurrentPage = 0;
    private int valTotalPages = 1;

    private final ObservableList<CommandeDTO> valideesList = FXCollections.observableArrayList();
    private final ObservableList<ProduitDTO> produitList = FXCollections.observableArrayList();

    DoubleProperty prepPrixProduit;
    DoubleProperty prepQteProduit;
    DoubleProperty prepDepPrix;
    DoubleProperty prepDepQte;


    @FXML
    public void initialize() {
        chargerCbProduit();
        initTabPreparation();
        initTabEnAttente();
        initTabValidees();
        initListener();
        chargerDonneesInitiales();

        btnA.setVisible(false);
        txtSearchId.requestFocus();

        prepPrixProduit = utilitaire.configurerChampMontantDynamique(txtPrepPrixProduit);
        prepQteProduit = utilitaire.configurerChampMontantDynamique(txtPrepQteProduit);
        prepDepPrix = utilitaire.configurerChampMontantDynamique(txtPrepDepPrix);
        prepDepQte = utilitaire.configurerChampMontantDynamique(txtPrepDepQte);

        cbValPageSize.setItems(FXCollections.observableArrayList(20, 25, 50, 100));
        cbValPageSize.setValue(20); // Valeur par défaut

        // Recharger à la 1ère page lors du changement de taille
        cbValPageSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                valCurrentPage = 0;
                filtrerValidees();
            }
        });
    }

    private void chargerDonneesInitiales() {
        cbSearchStatus.setItems(FXCollections.observableArrayList(StatusCommande.values()));
        chargerCommandesEnAttente();
        filtrerValidees();

    }

    private void chargerCbProduit(){
        List<ProduitDTO> liste = produitService.findListeProduit();
        produitList.setAll(liste); //la liste observable

        cbPrepProduit.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProduitDTO produit) {
                return (produit != null) ? produit.nomProduit() : "";
            }

            @Override
            public ProduitDTO fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                // Recherche de la catégorie correspondante dans la liste chargée
                return produitList.stream()
                        .filter(c -> c.nomProduit() != null && c.nomProduit().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        utilitaire.configureSearchableComboBox(cbPrepProduit, liste, ProduitDTO::nomProduit, 6);
    }

    // --- TAB 1 : INITIALISATION & LOGIQUE ---
    private void initTabPreparation() {
        tablePrepProduits.setItems(prepProduitsList);
        colPrepProdNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nomProduit()));

        colPrepProdPrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixAchatCommandeProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPrepProdPrix);

        colPrepProdQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().quantiteCommandeProduit()).asObject());
        colPrepProdQte.setCellFactory(TextFieldTableCell.forTableColumn(Utilitaire.getSafeDoubleConverter()));
        colPrepProdQte.setOnEditCommit(event -> {
            Double nouvelleQte = event.getNewValue();
            UtilisateurProduitDTO produitExistant = event.getRowValue();
            int index = event.getTablePosition().getRow();

            // Validation : Empêcher une quantité nulle ou négative
            if (nouvelleQte == null || nouvelleQte <= 0) {
                utilitaire.afficherErreur("Quantité invalide", "La quantité doit être supérieure à 0.");
                tablePrepProduits.refresh(); // Annule visuellement la modification dans la cellule
                return;
            }

            // Reconstruction du DTO avec la nouvelle quantité
            UtilisateurProduitDTO dtoMisAJour = UtilisateurProduitDTO.builder()
                    .idProduit(produitExistant.idProduit())
                    .nomProduit(produitExistant.nomProduit())
                    .prixAchatCommandeProduit(produitExistant.prixAchatCommandeProduit())
                    .quantiteCommandeProduit(nouvelleQte)
                    .build();

            // Remplacement dans l'ObservableList
            prepProduitsList.set(index, dtoMisAJour);

            // Recalcul des totaux généraux de la préparation
            calculerTotauxPrep();

        });

        colPrepProdTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixAchatCommandeProduit() * d.getValue().quantiteCommandeProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPrepProdTotal);

        // Bouton Suppression Ligne Produit
        colPrepProdAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().addAll("flat", "danger");
                btn.setGraphic(new FontIcon("fas-trash-alt"));
                btn.setOnAction(e -> {
                    prepProduitsList.remove(getTableView().getItems().get(getIndex()));
                    calculerTotauxPrep();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablePrepDepenses.setItems(prepDepensesList);
        colPrepDepLibelle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().libelleDepense()));

        colPrepDepPrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixDepense()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPrepDepPrix);

        colPrepDepQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().quantiteDepense()).asObject());
        colPrepDepQte.setCellFactory(TextFieldTableCell.forTableColumn(Utilitaire.getSafeDoubleConverter()));
        colPrepDepQte.setOnEditCommit(event -> {
            Double nouvelleQte = event.getNewValue();
            DepenseDTO depenseExistante = event.getRowValue();
            int index = event.getTablePosition().getRow();

            // Validation : Empêcher une quantité nulle ou négative
            if (nouvelleQte == null || nouvelleQte <= 0) {
                utilitaire.afficherErreur("Quantité invalide", "La quantité doit être supérieure à 0.");
                tablePrepDepenses.refresh(); // Annule visuellement la modification
                return;
            }

            // Recalcul du montant de la dépense (Prix * Nouvelle Quantité)
            double nouveauMontant = depenseExistante.prixDepense() * nouvelleQte;

            // Reconstruction du DTO avec la nouvelle quantité et le nouveau montant
            DepenseDTO depenseMiseAJour = DepenseDTO.builder()
                    .idDepense(depenseExistante.idDepense())
                    .libelleDepense(depenseExistante.libelleDepense())
                    .prixDepense(depenseExistante.prixDepense())
                    .quantiteDepense(nouvelleQte)
                    .montantDepense(nouveauMontant)
                    .build();

            // Mettre à jour l'ObservableList à l'index exact
            prepDepensesList.set(index, depenseMiseAJour);

            // Recalculer les totaux de la préparation
            calculerTotauxPrep();

        });

        colPrepDepMontant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().montantDepense()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colPrepDepMontant);

        colPrepDepAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().addAll("flat","danger");
                btn.setGraphic(new FontIcon("fas-trash-alt"));
                btn.setOnAction(e -> {
                    prepDepensesList.remove(getTableView().getItems().get(getIndex()));
                    calculerTotauxPrep();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        cbPrepProduit.setOnAction(e -> {
            ProduitDTO sel = cbPrepProduit.getValue();
            if (sel != null) {
                txtPrepPrixProduit.setText(String.valueOf(sel.prixAchatProduit()));
            }
        });
    }

    @FXML
    private void ajouterLigneProduitPrep() {
        ProduitDTO p = cbPrepProduit.getValue();
        if (p == null) return;
        try {
            double px = prepPrixProduit.get();
            double qte = prepQteProduit.get();

            boolean existproduit = prepProduitsList.stream()
                    .anyMatch(prod ->prod.idProduit() != null && prod.idProduit().equalsIgnoreCase(p.idProduit()));

            if (existproduit){
                utilitaire.afficherAvertissement("Produit existant", "Ce produit :"+p.nomProduit()+" existe dejà dans la commande");
                return;
            }

            if (px == 0 || qte == 0) {
                utilitaire.afficherErreur("Erreur","Veuiller à remplir correctement les champs");
                return;
            }

            UtilisateurProduitDTO dto = UtilisateurProduitDTO.builder()
                    .idProduit(p.idProduit())
                    .nomProduit(p.nomProduit())
                    .prixAchatCommandeProduit(px)
                    .quantiteCommandeProduit(qte)
                   // .montantCommandeProduit(px * qte)
                    .build();

            prepProduitsList.add(dto);
            calculerTotauxPrep();
            txtPrepQteProduit.clear();
        } catch (NumberFormatException ex) {
           // afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Saisissez des nombres valides.");
            utilitaire.afficherErreur("Erreur","Saisissez des nombres valides.");
        }
    }

    @FXML
    private void ajouterDepensePrep() {
        try {
            String lib = txtPrepDepLibelle.getText().trim();

            double px = prepDepPrix.get();
            double qte = prepDepQte.get();

            if (lib.isBlank()) return;

            boolean existDepense = prepDepensesList.stream()
                    .anyMatch(dep -> dep.libelleDepense() != null
                            && dep.libelleDepense().equalsIgnoreCase(lib));

            if (existDepense) {
                utilitaire.afficherAvertissement("Dépense existante","Cette libellée : "+lib+" exite déjà dns la liste");
                return;
            }

            if (px == 0 || qte == 0) {
                utilitaire.afficherErreur("Erreur","Veuiller à remplir correctement les champs");
                return;
            }

            DepenseDTO dto = DepenseDTO.builder()
                    .libelleDepense(lib)
                    .prixDepense(px)
                    .quantiteDepense(qte)
                    .montantDepense(px * qte)
                    .build();

            prepDepensesList.add(dto);
            calculerTotauxPrep();
            txtPrepDepLibelle.clear();
            txtPrepDepPrix.clear();
            txtPrepDepQte.clear();
        } catch (NumberFormatException ex) {
            //afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Valeurs numériques incorrectes.");
            utilitaire.afficherErreur("Erreur","Saisissez des nombres valides.");
        }
    }

    private void calculerTotauxPrep() {
        double totP = prepProduitsList.stream().mapToDouble(l -> l.prixAchatCommandeProduit() * l.quantiteCommandeProduit()).sum();
        double totD = prepDepensesList.stream().mapToDouble(DepenseDTO::montantDepense).sum();

        lblPrepTotalProd.setText(utilitaire.appliquerSeparateurMillierAr(totP));
        lblPrepTotalDep.setText( utilitaire.appliquerSeparateurMillierAr(totD));
        lblPrepGrandTotal.setText(utilitaire.appliquerSeparateurMillierAr(totP + totD));
    }

    @FXML
    private void enregistrerCommandePreparee() {
        // 1. Validation des champs obligatoires
        if (txtPrepFournisseur.getText().isBlank()) {
            utilitaire.afficherAvertissement("Champs requis","Veuillez renseigner le nom du fournisseur.");
            return;
        }

        // 2. Construction du DTO
        UtilisateurEntity user = new UtilisateurEntity(){};
        user.setIdUtilisateur(UserSession.getInstance().getIdUtilisateur());

        CommandeDTO dto = new CommandeDTO(
                this.commandeEnCoursId, // Conserve l'ID si modification, sinon null
                LocalDate.now(),
                LocalDate.now(),
                dpPrepDateArrivee.getValue(),
                txtPrepFournisseur.getText().trim(),
                txtPrepCommentaire.getText(),
                StatusCommande.EN_ATTENTE,
                0,
                user.getPseudoUtilisateur(),
                user.getPseudoUtilisateur(),
                new ArrayList<>(prepProduitsList),
                new ArrayList<>(prepDepensesList)
        );

        if (prepProduitsList.isEmpty()) {
            utilitaire.afficherErreur("Erreur","Commande vide");
            return;
        }

        // 3. Traitement selon le mode (Création vs Modification)
        if (this.commandeEnCoursId == null) {
            // --- MODE CRÉATION ---
            commandeService.preparerCommande(dto,user);
            utilitaire.afficherInformation("Succès","Commande créée avec succès !");
        } else {
            // --- MODE MODIFICATION ---
            commandeService.modifierCommande(this.commandeEnCoursId, dto, user);
            utilitaire.afficherInformation("Succés", "Commande mise à jour avec succès !");
        }

        // 4. Réinitialisation complète du formulaire et rechargement
        reinitialiserFormulairePrep();
        chargerCommandesEnAttente();

        // 5. Basculer vers l'onglet des commandes préparées (Tab 2)
        tabPaneCommande.getSelectionModel().select(1);
    }

    // --- TAB 2 : COMMANDES PREPARÉES ---
    private void initTabEnAttente() {
        tableCmdEnAttente.setItems(enAttenteList);
        colAttId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().idCommande()));
        colAttDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().dateCreationCmd() != null ? d.getValue().dateCreationCmd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"));
        colAttFournisseur.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fournisseurCmd()));
        colAttPrevu.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().datePrevuArriveCmd() != null ? d.getValue().datePrevuArriveCmd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"));
        colAttMontant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().montantCmd()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colAttMontant);
        colAttCommentaire.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().commentaireCmd()));

        colAttActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnValider = new Button();
            private final Button btnSupprimer = new Button();
            private final Button btnModifier = new Button();
            private final HBox container = new HBox(8,btnModifier ,btnValider, btnSupprimer);

            {
                btnModifier.getStyleClass().addAll("accent", "button-icon","flat");
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(13);
                btnModifier.setGraphic(editIcon);
                btnModifier.setTooltip(new Tooltip("Modifier la commande"));


                btnValider.getStyleClass().addAll("success", "button-icon","flat");
                FontIcon validIcon = new FontIcon("fas-check");
                validIcon.setIconSize(13);
                btnValider.setGraphic(validIcon);
                btnValider.setTooltip(new Tooltip("Valider la commande"));

                btnSupprimer.getStyleClass().addAll("danger",  "button-icon","flat");
                FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                validIcon.setIconSize(13);
                btnSupprimer.setGraphic(deleteIcon);
                btnSupprimer.setTooltip(new Tooltip("Supprimer la commande"));

                container.setAlignment(Pos.CENTER);

                // Action de modification
                btnModifier.setOnAction(e -> {
                    CommandeDTO c = getTableView().getItems().get(getIndex());
                    chargerCommandeDansFormulairePrep(c);
                });

               // btnValider.getStyleClass().add("success");
                //btnSupprimer.getStyleClass().add("danger");

                btnValider.setOnAction(e -> {
                    CommandeDTO c = getTableView().getItems().get(getIndex());
                    commandeService.validerCommande(c.idCommande());
                    utilitaire.afficherInformation("Succès","Commande validée ! Le stock a été mis à jour.");
                    chargerCommandesEnAttente();
                    filtrerValidees();
                });

                btnSupprimer.setOnAction(e -> {
                    CommandeDTO c = getTableView().getItems().get(getIndex());
                    commandeService.deleteCommandeById(c.idCommande());
                    utilitaire.afficherInformation("Succès","Commande supprimée !");
                    chargerCommandesEnAttente();
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void chargerCommandesEnAttente() {
        enAttenteList.setAll(commandeService.findAll().stream()
                .filter(c -> StatusCommande.EN_ATTENTE.equals(c.statusCmd()))
                .toList());
    }

    private void chargerCommandeDansFormulairePrep(CommandeDTO commande) {
        if (commande == null) return;

        // 1. Stocker l'ID de la commande en édition
        this.commandeEnCoursId = commande.idCommande();

        // 2. Remplir les champs d'informations générales
        txtPrepFournisseur.setText(commande.fournisseurCmd());
        dpPrepDateArrivee.setValue(commande.datePrevuArriveCmd());
        txtPrepCommentaire.setText(commande.commentaireCmd());

        // 3. Charger les produits et dépenses associés
        if (commande.lignesProduits() != null) {
            prepProduitsList.setAll(commande.lignesProduits());
        } else {
            prepProduitsList.clear();
        }

        if (commande.listeDepenses() != null) {
            prepDepensesList.setAll(commande.listeDepenses());
        } else {
            prepDepensesList.clear();
        }

        btnV.setText("Modifier la commande");
        btnV.setGraphic(new FontIcon("fas-edit"));

        btnA.setVisible(true);
        btnA.setManaged(true);

        // 4. Recalculer les totaux de l'onglet préparation
        calculerTotauxPrep();

        // 5. Basculer automatiquement vers le Tab 1 ("Préparation Commande")
        tabPaneCommande.getSelectionModel().select(0);
    }


    private void reinitialiserFormulairePrep() {
        this.commandeEnCoursId = null; // Réinitialiser le mode édition

        txtPrepFournisseur.clear();
        dpPrepDateArrivee.setValue(null);
        txtPrepCommentaire.clear();

        // Champs de saisie rapides
        if (cbPrepProduit != null) cbPrepProduit.setValue(null);
        if (txtPrepPrixProduit != null) txtPrepPrixProduit.clear();
        if (txtPrepQteProduit != null) txtPrepQteProduit.clear();
        if (txtPrepDepLibelle != null) txtPrepDepLibelle.clear();
        if (txtPrepDepPrix != null) txtPrepDepPrix.clear();
        if (txtPrepDepQte != null) txtPrepDepQte.clear();

        prepProduitsList.clear();
        prepDepensesList.clear();

        // Remettre le bouton de validation en mode "Enregistrement"
        btnV.setText("Enregistrer la Commande");
        btnV.setGraphic(new FontIcon("fas-save"));

        // Masquer le bouton d'annulation
        btnA.setVisible(false);
        btnA.setManaged(false);

        calculerTotauxPrep();
    }

    @FXML
    private void annulerCommandePrepare(){
        // Réinitialise tous les champs et repasse en mode création
        reinitialiserFormulairePrep();
        utilitaire.afficherInformation("Annulation", "La modification a été annulée.");
    }

    // --- TAB 3 : VALIDÉES & RECHERCHE MULTICRITÈRE ---
    private void initTabValidees() {
        tableCmdValidees.setItems(valideesList);
        colValId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().idCommande()));

        colValDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().dateCreationCmd() != null ? d.getValue().dateCreationCmd().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"));

        colValDateModif.setCellValueFactory(d ->{
            LocalDate date = d.getValue().dateModificationCmd();
                return new SimpleStringProperty(d.getValue().dateModificationCmd() != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        colValFournisseur.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fournisseurCmd()));

        colValMontant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().montantCmd()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colValMontant);

        colValStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().statusCmd().name()));
        utilitaire.appliquerColorationStatut(colValStatus);

        // Affichage de l'utilisateur créateur
        colValCreateur.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().utilisateurCreationPseudo() != null ? d.getValue().utilisateurCreationPseudo() : "-"));

        // Affichage de l'utilisateur modificateur
        colValModificateur.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().utilisateurModificationPseudo() != null ? d.getValue().utilisateurModificationPseudo() : "-"));

        colValProdNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nomProduit()));

        colValProdPrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixAchatCommandeProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colValProdPrix);

        colValProdQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().quantiteCommandeProduit()));

        colValProdTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixAchatCommandeProduit() * d.getValue().quantiteCommandeProduit()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colValProdTotal);

        colValDepLibelle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().libelleDepense()));

        colValDepPrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().prixDepense()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colValDepPrix);

        colValDepQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().quantiteDepense()));

        colValDepMontant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().montantDepense()));
        utilitaire.appliquerFormatSeparateurMilliersSpecial(colValDepMontant);

        tableCmdValidees.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                lblValDetailHeader.setText("Commande : " + newSel.idCommande() + " | Fournisseur: " + newSel.fournisseurCmd());
                tableValDetailProduits.setItems(FXCollections.observableArrayList(newSel.lignesProduits() != null ? newSel.lignesProduits() : new ArrayList<>()));
                tableValDetailDepenses.setItems(FXCollections.observableArrayList(newSel.listeDepenses() != null ? newSel.listeDepenses() : new ArrayList<>()));
            }
        });
    }

    @FXML
    private void filtrerValidees() {
        String id = txtSearchId.getText().trim().isBlank() ? null : txtSearchId.getText().trim();
        String four = txtSearchFournisseur.getText().trim().isBlank() ? null : txtSearchFournisseur.getText().trim();
        LocalDate dDebut = dpSearchDebut.getValue();
        LocalDate dFin = dpSearchFin.getValue();
        StatusCommande status = cbSearchStatus.getValue();
        int pageSize = (cbValPageSize.getValue() != null) ? cbValPageSize.getValue() : 20;

        Page<CommandeDTO> pageRes = commandeService.recherchePageableMulticriteria(id, four, null, dDebut, dFin, status, null, valCurrentPage, pageSize, "dateCreationCmd", "DESC");
        valideesList.setAll(pageRes.getContent());
        tableCmdValidees.setItems(valideesList);

        // Mise à jour des indicateurs et boutons de navigation
        this.valTotalPages = pageRes.getTotalPages() > 0 ? pageRes.getTotalPages() : 1;
        long totalElements = pageRes.getTotalElements();

        lblValPageInfo.setText(String.format("Page %d / %d (%d éléments)", valCurrentPage + 1, valTotalPages, totalElements));
        btnValPrevPage.setDisable(valCurrentPage <= 0);
        btnValNextPage.setDisable(valCurrentPage >= valTotalPages - 1);
    }

    private void initListener(){
        txtSearchId.textProperty().addListener((observable, oldValue, newValue) ->
                filtrerValidees()
                );

        txtSearchFournisseur.textProperty().addListener((observable, oldValue, newValue) ->
                filtrerValidees()
                );
    }

    @FXML
    private void reinitialiserFiltres() {
        txtSearchId.clear();
        txtSearchFournisseur.clear();
        dpSearchDebut.setValue(null);
        dpSearchFin.setValue(null);
        cbSearchStatus.setValue(null);
        this.valCurrentPage = 0;
        filtrerValidees();
    }

    @FXML
    private void valPagePrecedente() {
        if (valCurrentPage > 0) {
            valCurrentPage--;
            filtrerValidees();
        }
    }

    @FXML
    private void valPageSuivante() {
        if (valCurrentPage < valTotalPages - 1) {
            valCurrentPage++;
            filtrerValidees();
        }
    }

}