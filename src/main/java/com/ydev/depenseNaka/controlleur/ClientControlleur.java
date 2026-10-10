package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.ClientDTO;
import com.ydev.depenseNaka.exception.ImpossibleDeleteClient;
import com.ydev.depenseNaka.service.ClientService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClientControlleur {

    private final ClientService clientService;
    private final Utilitaire utilitaire;

    // Champs de recherche multi-critères
    @FXML private TextField txtSearchId;
    @FXML private TextField txtSearchNom;
    @FXML private TextField txtSearchPrenom;
    @FXML private TextField txtSearchTelephone;
    @FXML private TextField txtSearchAdresse;

    // TableView & Colonnes
    @FXML private TableView<ClientDTO> tableClients;
    @FXML private TableColumn<ClientDTO, String> colId;
    @FXML private TableColumn<ClientDTO, String> colNom;
    @FXML private TableColumn<ClientDTO, String> colPrenom;
    @FXML private TableColumn<ClientDTO, String> colTelephone;
    @FXML private TableColumn<ClientDTO, String> colAdresse;
    @FXML private TableColumn<ClientDTO, Void> colActions;

    // Champs du formulaire
    @FXML private Label lblFormTitre;
    @FXML private TextField txtIdClient;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;

    private final ObservableList<ClientDTO> clientList = FXCollections.observableArrayList();
    private boolean modeEdition = false;


    @FXML
    public void initialize() {
        initTableColumns();
        initSearchListeners();
        initSelectionListener();
        chargerClients();
        ecouteChamSaisie();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().idClient()));

        colNom.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().nomClient()));

        colPrenom.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().prenomClient()));

        colTelephone.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().telephoneClient()));

        utilitaire.appliquerFormatTelephone(colTelephone);

        colAdresse.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().adresseClient()));

        ajouterColonneActions();
    }

    //Ecouteur de champ de saisie
    private void ecouteChamSaisie(){
        // 1. Force la saisie de chiffres uniquement (Optionnel mais recommandé)
        txtTelephone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")){
                txtTelephone.setText(newValue.replaceAll("[^\\d]",""));
            }
        });

        // 2. Formate automatiquement dès que l'utilisateur clique ailleurs
        txtTelephone.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue){
                String numeroTelephone = txtTelephone.getText();
                txtTelephone.setText(utilitaire.formaterTelephoneMalgache(numeroTelephone));
            }
        });
    }

    // Écouteurs en temps réel sur tous les champs de recherche
    private void initSearchListeners() {
        txtSearchId.textProperty().addListener((obs, oldV, newV) -> filtrerClients());
        txtSearchNom.textProperty().addListener((obs, oldV, newV) -> filtrerClients());
        txtSearchPrenom.textProperty().addListener((obs, oldV, newV) -> filtrerClients());
        txtSearchTelephone.textProperty().addListener((obs, oldV, newV) -> filtrerClients());
        txtSearchAdresse.textProperty().addListener((obs, oldV, newV) -> filtrerClients());
    }

    // Chargement automatique dans le formulaire lors de la sélection dans la table
    private void initSelectionListener() {
        tableClients.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedClient) -> {
            if (selectedClient != null) {
                chargerInFormulaire(selectedClient);
            }
        });
    }

    private void chargerClients() {
        clientList.clear();
        clientList.addAll(clientService.findAllByOrderByNomClientAsc());
        tableClients.setItems(clientList);
    }

    // Filtre multi-critères en mémoire (instantané)
    private void filtrerClients() {
        String sId = txtSearchId.getText().toLowerCase().trim();
        String sNom = txtSearchNom.getText().toLowerCase().trim();
        String sPrenom = txtSearchPrenom.getText().toLowerCase().trim();
        String sTel = txtSearchTelephone.getText().toLowerCase().trim();
        String sAdr = txtSearchAdresse.getText().toLowerCase().trim();

        List<ClientDTO> tousLesClients = clientService.findAllByOrderByNomClientAsc();

        List<ClientDTO> meillons = tousLesClients.stream().filter(c ->
                (sId.isEmpty() || (c.idClient() != null && c.idClient().toLowerCase().contains(sId))) &&
                        (sNom.isEmpty() || (c.nomClient() != null && c.nomClient().toLowerCase().contains(sNom))) &&
                        (sPrenom.isEmpty() || (c.prenomClient() != null && c.prenomClient().toLowerCase().contains(sPrenom))) &&
                        (sTel.isEmpty() || (c.telephoneClient() != null && c.telephoneClient().toLowerCase().contains(sTel))) &&
                        (sAdr.isEmpty() || (c.adresseClient() != null && c.adresseClient().toLowerCase().contains(sAdr)))
        ).toList();

        clientList.setAll(meillons);
    }

    @FXML
    private void reinitialiserRecherche() {
        txtSearchId.clear();
        txtSearchNom.clear();
        txtSearchPrenom.clear();
        txtSearchTelephone.clear();
        txtSearchAdresse.clear();
        chargerClients();
    }

    @FXML
    private void validerFormulaire() {
        if (txtNom.getText() == null || txtNom.getText().isBlank()) {
           // afficherAlerte(Alert.AlertType.WARNING, "Champ requis", "Le nom du client est obligatoire.");
            utilitaire.afficherAvertissement("Champ requis","Le nom du client est obligatoire.");
            return;
        }

        ClientDTO dto = ClientDTO.builder()
                .idClient(txtIdClient.getText())
                .nomClient(txtNom.getText().trim())
                .prenomClient(txtPrenom.getText().trim())
                .telephoneClient(txtTelephone.getText().trim())
                .adresseClient(txtAdresse.getText().trim())
                .build();

        try {
            if (modeEdition) {
                clientService.updateClient(dto.idClient(), dto);
                utilitaire.afficherInformation("Succès","Les informations du client ont été mises à jour avec succés");
                reinitialiserFormulaire();
                chargerClients();
            } else {
                clientService.createClient(dto);
                utilitaire.afficherInformation("Succès","Un nouveau client a été créé ");
                reinitialiserFormulaire();
                chargerClients();
            }
            reinitialiserFormulaire();
            chargerClients();
        } catch (Exception e) {
           // afficherAlerte(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement", e.getMessage());
            utilitaire.afficherErreur("Erreur lors de l'enregistrement", "Une erreur se produit car: " + e.getMessage());
        }
    }

    @FXML
    private void reinitialiserFormulaire() {
        txtIdClient.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtTelephone.clear();
        txtAdresse.clear();

        lblFormTitre.setText("Nouveau Client");
        modeEdition = false;
        tableClients.getSelectionModel().clearSelection();
    }

    private void chargerInFormulaire(ClientDTO client) {
        modeEdition = true;
        lblFormTitre.setText("Modifier Client");

        txtIdClient.setText(client.idClient());
        txtNom.setText(client.nomClient());
        txtPrenom.setText(client.prenomClient());
        txtTelephone.setText(client.telephoneClient());
        txtAdresse.setText(client.adresseClient());
    }

    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button();
            private final HBox container = new HBox(btnDelete);

            {
                btnDelete.getStyleClass().addAll("flat", "danger", "button-icon");
                btnDelete.setGraphic(new FontIcon("fas-trash-alt"));
                container.setStyle("-fx-alignment: CENTER;");

                btnDelete.setOnAction(e -> {
                    ClientDTO client = getTableView().getItems().get(getIndex());
                    supprimerClient(client);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void supprimerClient(ClientDTO client) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Suppression du client");
        confirm.setContentText("Supprimer définitivement " + client.nomClient()+ " ?");

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
                    clientService.deleteClient(client.idClient());
                    utilitaire.afficherInformation("Succès","Client("+client.nomClient()+") supprimé");
                    reinitialiserFormulaire();
                    chargerClients();
                } catch (ImpossibleDeleteClient e) {
                   // afficherAlerte(Alert.AlertType.ERROR, "Suppression impossible", "Ce client possède des factures liées et ne peut pas être supprimé.");
                    utilitaire.afficherErreur("Suppression impossible","Ce client est lié à un facture et ne peut pas être supprimé.");
                } catch (Exception e) {
                   // afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                    utilitaire.afficherErreur("Erreur",e.getMessage());
                }
            }
        });
    }

   /* private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }*/
}
