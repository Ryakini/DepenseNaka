package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.ConnexionDTO;
import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.exception.ImpossibleDeleteClient;
import com.ydev.depenseNaka.model.RoleUtilisateur;
import com.ydev.depenseNaka.service.ConnexionService;
import com.ydev.depenseNaka.service.UtilisateurService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class UtilisateurControlleur {

    private final UtilisateurService utilisateurService;
    private final ConnexionService connexionService;
    private final Utilitaire utilitaire;

    // Champs de recherche
    @FXML private TextField txtRecherche;

    // Table Utilisateurs
    @FXML private TableView<UtilisateurDTO> tvUtilisateurs;
    @FXML private TableColumn<UtilisateurDTO, String> colId;
    @FXML private TableColumn<UtilisateurDTO, String> colNom;
    @FXML private TableColumn<UtilisateurDTO, String> colPrenom;
    @FXML private TableColumn<UtilisateurDTO, String> colPseudo;
    @FXML private TableColumn<UtilisateurDTO, String> colRole;
    @FXML private TableColumn<UtilisateurDTO, String> colTelephone;
    @FXML private TableColumn<UtilisateurDTO, String> colAdresse;
    @FXML private TableColumn<UtilisateurDTO, Void> colAction;


    // Table Historique
    @FXML private TableView<ConnexionDTO> tvHistorique;
    @FXML private TableColumn<ConnexionDTO, String> colHistSession;
    @FXML private TableColumn<ConnexionDTO, String> colHistDebut;
    @FXML private TableColumn<ConnexionDTO, String> colHistFin;
    @FXML private TableColumn<ConnexionDTO, String> colHistStatut;

    // Formulaire
    @FXML
    private VBox formPane;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtPseudo;
    @FXML private PasswordField txtMdp;
    @FXML private ComboBox<RoleUtilisateur> cbRole;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtTelephone;

    private final ObservableList<UtilisateurDTO> masterData = FXCollections.observableArrayList();
    private FilteredList<UtilisateurDTO> filteredData;
    private UtilisateurDTO selectedUtilisateur;

    @FXML
    public void initialize() {
        initTableUtilisateurs();
        initTableHistorique();
        initFormulaire();
        initEcouteurRecherche();
        chargerDonnees();
    }

    private void initTableUtilisateurs() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().idUtilisateur()));
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomUtilisateur()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().prenomUtilisateur()));
        colPseudo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().pseudoUtilisateur()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().roleUtilisateur() != null ? data.getValue().roleUtilisateur().name() : ""
        ));
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().telephoneUtilisateur()));
        utilitaire.appliquerFormatTelephone(colTelephone);
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().adresseUtilisateur()));

        ajouterColonneActions();

        // Écouteur de sélection de ligne
        tvUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedUtilisateur = newSel;
                chargerHistorique(newSel.idUtilisateur());
            }
        });
    }

    private void ajouterColonneActions() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button();
            private final Button btnEdit = new Button();
            private final HBox container = new HBox(btnEdit,btnDelete);

            {
                btnDelete.getStyleClass().addAll("flat", "danger", "button-icon");
                btnDelete.setGraphic(new FontIcon("fas-trash-alt"));

                btnEdit.getStyleClass().addAll("flat", "accent", "button-icon");
                btnEdit.setGraphic(new FontIcon("fas-edit"));


                container.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    UtilisateurDTO utilisateurDTO = getTableView().getItems().get(getIndex());//recupère l'utilisateur selectionné
                    //affecter au table utilisateur
                    tvUtilisateurs.getSelectionModel().select(utilisateurDTO);
                    if (utilisateurDTO != null) {
                        afficherFormulaire(true);
                        remplirFormulaire(utilisateurDTO);
                    }
                });

                btnDelete.setOnAction(e -> {
                    UtilisateurDTO utilisateur = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(utilisateur);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void supprimerUtilisateur(UtilisateurDTO utilisateurDTO) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Suppression d'un utilisateur");
        confirm.setContentText("Supprimer définitivement " + utilisateurDTO.nomUtilisateur()+ " ?");
        utilitaire.appliquerLogoAlerte(confirm);

        // Application du style moderne AtlantaFX (isDanger = true -> bouton rouge)
        utilitaire.styleModernAlert(confirm, "fas-exclamation-triangle", true);

        // Personnalisation des libellés des boutons
        Button okBtn = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.setText("Oui, supprimer");

        Button cancelBtn = (Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) cancelBtn.setText("Annuler");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    utilisateurService.deleteUtilisateur(utilisateurDTO.idUtilisateur());
                    utilitaire.afficherInformation("Succès","Utilisateur("+utilisateurDTO.pseudoUtilisateur()+") supprimé");
                   initFormulaire();
                    chargerDonnees();
                } catch (ImpossibleDeleteClient e) {
                    utilitaire.afficherErreur("Suppression impossible","Cet utilisateur est lié à une commande et ne peut pas être supprimé.");
                } catch (Exception e) {
                    utilitaire.afficherErreur("Erreur",e.getMessage());
                }
            }
        });
    }


    private void initTableHistorique() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        colHistSession.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomConnexion()));
        colHistDebut.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().dateDebutConnexion() != null ? data.getValue().dateDebutConnexion().format(formatter) : "-"
        ));
        colHistFin.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().dateFinConnexion() != null ? data.getValue().dateFinConnexion().format(formatter) : "-"
        ));

        colHistStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActive() ? "CONNECTE" :"DECONNECTE"));
        utilitaire.appliquerColorationStatut(colHistStatut);
    }

    private void initFormulaire() {
        cbRole.setItems(FXCollections.observableArrayList(RoleUtilisateur.values()));
    }

    private void initEcouteurRecherche() {
        filteredData = new FilteredList<>(masterData, p -> true);
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(utilisateur -> {
                if (newValue == null || newValue.isBlank()) return true;
                String lowerFilter = newValue.toLowerCase();

                return (utilisateur.nomUtilisateur() != null && utilisateur.nomUtilisateur().toLowerCase().contains(lowerFilter))
                        || (utilisateur.prenomUtilisateur() != null && utilisateur.prenomUtilisateur().toLowerCase().contains(lowerFilter))
                        || (utilisateur.pseudoUtilisateur() != null && utilisateur.pseudoUtilisateur().toLowerCase().contains(lowerFilter));
            });
        });
        tvUtilisateurs.setItems(filteredData);
    }

    private void chargerDonnees() {
        masterData.setAll(utilisateurService.listeUtilisateur());
    }

    private void chargerHistorique(String idUtilisateur) {
        tvHistorique.setItems(FXCollections.observableArrayList(
                connexionService.obtenirHistoriqueConnexion(idUtilisateur)
        ));
    }

    private void remplirFormulaire(UtilisateurDTO user) {
        txtNom.setText(user.nomUtilisateur());
        txtPrenom.setText(user.prenomUtilisateur());
        txtPseudo.setText(user.pseudoUtilisateur());
        txtMdp.clear(); // Le mot de passe n'est pas réaffiché en clair
        cbRole.setValue(user.roleUtilisateur());
        txtAdresse.setText(user.adresseUtilisateur());
        txtTelephone.setText(user.telephoneUtilisateur());
    }

    @FXML
    private void handleNouveau() {
        selectedUtilisateur = null;
        tvUtilisateurs.getSelectionModel().clearSelection();
        tvHistorique.getItems().clear();
        viderFormulaire();

        // Afficher le formulaire s'il est masqué
        afficherFormulaire(true);
    }

    @FXML
    private void handleEnregistrer() {
        try {
            UtilisateurDTO dto = UtilisateurDTO.builder()
                    .idUtilisateur(selectedUtilisateur != null ? selectedUtilisateur.idUtilisateur() : null)
                    .nomUtilisateur(txtNom.getText())
                    .prenomUtilisateur(txtPrenom.getText())
                    .pseudoUtilisateur(txtPseudo.getText())
                    .mdpUtilisateur(txtMdp.getText())
                    .roleUtilisateur(cbRole.getValue())
                    .adresseUtilisateur(txtAdresse.getText())
                    .telephoneUtilisateur(txtTelephone.getText())
                    .build();

            if (selectedUtilisateur == null) {
                utilisateurService.createUtilisateur(dto);
                afficherFormulaire(false);
            } else {
                utilisateurService.updateUtilisateur(selectedUtilisateur.idUtilisateur(), dto);
                afficherFormulaire(false);
            }

            chargerDonnees();
            handleNouveau();
        } catch (Exception e) {
           utilitaire.afficherErreur("Erreur","Une erreur car: "+e.getMessage());
          //  Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
           // alert.showAndWait();
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedUtilisateur != null) {
            try {
                utilisateurService.deleteUtilisateur(selectedUtilisateur.idUtilisateur());
                chargerDonnees();
                handleNouveau();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        viderFormulaire();
        afficherFormulaire(false);
        //handleNouveau();
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtPrenom.clear();
        txtPseudo.clear();
        txtMdp.clear();
        cbRole.setValue(null);
        txtAdresse.clear();
        txtTelephone.clear();
    }

    // Méthode utilitaire pour afficher/masquer le formulaire
    private void afficherFormulaire(boolean visible) {
        formPane.setVisible(visible);
        formPane.setManaged(visible);
    }
}