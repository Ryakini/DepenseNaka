package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.DepenseNakaApplication;
import com.ydev.depenseNaka.dto.CategorieDTO;
import com.ydev.depenseNaka.dto.ProduitDTO;
import com.ydev.depenseNaka.service.ProduitService;
import com.ydev.depenseNaka.service.UtilisateurService;
import com.ydev.depenseNaka.session.UserSession;
import com.ydev.depenseNaka.update.BackupScheduler;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

@Controller
@RequiredArgsConstructor
public class ParametreControlleur {

    private final ProduitService produitService;
    private final UtilisateurService utilisateurService;
    private final Utilitaire utilitaire;
    private final BackupScheduler backupScheduler;

    @FXML private TabPane conteneurPane;
    @FXML private Tab tabSynchronisation;
    @FXML private Tab tabUpdate;

    // --- Onglet 1 : Elements UI ---
    @FXML private Label lblNomFichier;
    @FXML private Button btnChargerFichier, btnSauvegarderBase;
    @FXML private ProgressIndicator progressImport;
    @FXML private TableView<ProduitImportModel> tableImportProduits;
    @FXML private TableColumn<ProduitImportModel, String> colTaille, colImpNom, colImpCategorie, colImpStatut;
    @FXML private TableColumn<ProduitImportModel, Boolean> colImpDispo;
    @FXML private TableColumn<ProduitImportModel, Double> colImpPrixAchat, colImpPrixVente, colImpQte;

    // --- Onglet 2 : Elements UI ---
    @FXML private TextField txtEmailUser;
    @FXML private PasswordField txtCodeGoogleApp;
    @FXML private ProgressIndicator progressSync;
    @FXML private Label lblStatutSync;

    // --- Onglet 3 : Elements UI ---
    @FXML private Label lblVersionActuelle, lblStatutMAJ;
    @FXML private TextField txtUrlGit;
    @FXML private ProgressBar progressMAJ;

    private File fichierExcelSelectionne;
    private final ObservableList<ProduitImportModel> listeImport = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTableImport();
        chargerParametresParDefaut();
        accesOnglet();
       // demarrerPlanificateurNotifications();
    }

    private void initTableImport() {
        colTaille.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDto().tailleProduit()));
        colImpNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDto().nomProduit()));
        colImpCategorie.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDto().categorie() != null ? d.getValue().getDto().categorie().nomCategorie() : "-"));
        colImpPrixAchat.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getDto().prixAchatProduit()).asObject());
        colImpPrixVente.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getDto().prixVenteProduit()).asObject());
        colImpQte.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getDto().quantiteProduit()).asObject());
        colImpDispo.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().getDto().estDisponibleProduit()));
        colImpStatut.setCellValueFactory(d -> d.getValue().statutProperty());

        tableImportProduits.setItems(listeImport);
    }

    private void chargerParametresParDefaut() {
        txtEmailUser.setText("mon.email.defaut@gmail.com");
        txtCodeGoogleApp.setText("xxxx-xxxx-xxxx-xxxx");
        lblVersionActuelle.setText("v1.0.0");
        txtUrlGit.setText("https://github.com/votre-repo/depenseNaka.git");
    }

    //restreindre accès
    private void accesOnglet(){
        UserSession session = UserSession.getInstance();
        String idUser = session.getIdUtilisateur();
        boolean isAdmin = utilisateurService.isAdmin(idUser);

        utilitaire.restreindreOnglet(conteneurPane, tabSynchronisation, isAdmin);
        utilitaire.restreindreOnglet(conteneurPane, tabUpdate, isAdmin);
    }

    // ----------------------------------------------------------------------------------
    // 1. ONGLET 1 : IMPORTATION
    // ----------------------------------------------------------------------------------

    private File choisirFichier(String titre){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(titre);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel / CSV", "*.xlsx", "*.xls", "*.csv"));

        fichierExcelSelectionne = fileChooser.showOpenDialog(lblNomFichier.getScene().getWindow());

        if (fichierExcelSelectionne != null) {
            lblNomFichier.setText(fichierExcelSelectionne.getName());
            btnChargerFichier.setDisable(false);
        }
        return fichierExcelSelectionne;
    }

    @FXML
    private void handleChoisirFichier() {
        File selectedFile = choisirFichier("Selectionner le fichier excel de la liste des produits");
        if (selectedFile != null) {
            lblNomFichier.setText(selectedFile.getName());
            btnChargerFichier.setDisable(false);
        }
    }

    @FXML
    private void handleChargerContenu() {
        String nomFichier = lblNomFichier.getText();

        if (nomFichier == null || nomFichier.isBlank()) {
            utilitaire.afficherErreur("Fichier manquant", "Veuillez sélectionner un fichier Excel ou CSV.");
            return;
        }

        String cheminFichier = fichierExcelSelectionne.getAbsolutePath();
        File file = new File(cheminFichier);
        if (!file.exists()) {
            utilitaire.afficherErreur("Fichier introuvable", "Le fichier spécifié n'existe pas.");
            return;
        }

        // Réinitialiser la liste avant le nouveau chargement
        listeImport.clear();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Ignorer la ligne d'en-tête
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Lecture des cellules
                String designation = utilitaire.getCellValueAsString(row.getCell(0));
                String categorieNom = utilitaire.getCellValueAsString(row.getCell(1));
                String taille = utilitaire.getCellValueAsString(row.getCell(2));
                String prixAchat = utilitaire.getCellValueAsString(row.getCell(3));
                String prixVente = utilitaire.getCellValueAsString(row.getCell(4));
                String quantite = utilitaire.getCellValueAsString(row.getCell(5));
                String estDispoStr = utilitaire.getCellValueAsString(row.getCell(6));

                if (designation != null && !designation.isBlank()) {
                    // Parsing sécurisé des valeurs numériques et booléennes
                    double prixA = (prixAchat != null && !prixAchat.isBlank()) ? Double.parseDouble(prixAchat) : 0.0;
                    double prixV = (prixVente != null && !prixVente.isBlank()) ? Double.parseDouble(prixVente) : 0.0;
                    double quantiteV = (quantite != null && !quantite.isBlank()) ? Double.parseDouble(quantite) : 0.0;
                    boolean estDispo = "true".equalsIgnoreCase(estDispoStr) || "1".equals(estDispoStr) || "oui".equalsIgnoreCase(estDispoStr);
                    //boolean estDispo = true;

                    // 1. Instancier le DTO de la Catégorie
                    CategorieDTO categorieDTO = CategorieDTO.builder()
                            .nomCategorie(categorieNom)
                            .build();

                    // 2. Construire le ProduitDTO
                    ProduitDTO produitDTO = ProduitDTO.builder()
                            .nomProduit(designation)
                            .tailleProduit(taille)
                            .prixAchatProduit(prixA)
                            .prixVenteProduit(prixV)
                            .quantiteProduit(quantiteV)
                            .estDisponibleProduit(estDispo)
                            .categorie(categorieDTO)
                            .build();

                    // 3. Créer le model wrapper pour le tableau JavaFX
                    // Assurez-vous que ProduitImportModel prend en paramètre (ProduitDTO, String statut)
                    ProduitImportModel importModel = new ProduitImportModel(produitDTO, "En attente");

                    listeImport.add(importModel);
                }
            }

            if (listeImport.isEmpty()) {
                utilitaire.afficherAvertissement("Aperçu vide", "Aucune donnée valide n'a été trouvée dans le fichier !");
                btnSauvegarderBase.setDisable(true);
            } else {
                btnSauvegarderBase.setDisable(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            utilitaire.afficherInformation("Erreur de lecture", "Erreur lors du chargement du fichier : " + e.getMessage());
        }
    }

    @FXML
    private void handleSauvegarderEnBase(ActionEvent event) {
        btnSauvegarderBase.setDisable(true);
        progressImport.setVisible(true);

        Task<Void> tacheSauvegarde = new Task<>() {
            @Override
            protected Void call() {
                int total = listeImport.size();
                int i = 0;

                for (ProduitImportModel item : listeImport) {
                    try {
                        ProduitDTO dto = item.getDto();

                        // Traitement via le service intelligent
                        ProduitDTO produitResultat = produitService.sauvegarderOuMettreAJour(dto);

                        // Si la quantité finale est supérieure à celle importée, c'est une mise à jour avec cumul
                        if (produitResultat.quantiteProduit() > dto.quantiteProduit()) {
                            Platform.runLater(() -> item.setStatut("Mis à jour (Qté: " + produitResultat.quantiteProduit() + ")"));
                        } else {
                            Platform.runLater(() -> item.setStatut("Ajouté avec succès"));
                        }

                    } catch (Exception ex) {
                        utilitaire.afficherErreur("Erreur","Car: "+ex.getMessage());
                        Platform.runLater(() -> item.setStatut("Erreur : " + ex.getMessage()));
                        System.out.println("Erreur: "+ex.getMessage());
                    }

                    i++;
                    updateProgress(i, total);
                }
                return null;
            }
        };

        tacheSauvegarde.setOnSucceeded(e -> {
            progressImport.setVisible(false);
            utilitaire.afficherInformation("Importation terminée", "Les produits ont été traités avec succès.");
        });

        tacheSauvegarde.setOnFailed(e -> {
            progressImport.setVisible(false);
            btnSauvegarderBase.setDisable(false);
            utilitaire.afficherErreur("Erreur", "L'enregistrement a échoué : " + tacheSauvegarde.getException().getMessage());
        });

        new Thread(tacheSauvegarde).start();
    }

    @FXML
    private void handleAnnulerImportation(ActionEvent event) {
        listeImport.clear();
        btnSauvegarderBase.setDisable(true);
        btnChargerFichier.setDisable(true);
        lblNomFichier.setText("Aucun fichier sélectionné");
    }

    // ----------------------------------------------------------------------------------
    // 2. ONGLET 2 : SYNCHRONISATION (EMAIL & ONEDRIVE)
    // ----------------------------------------------------------------------------------

    //charger configuration initiale
    private void configSynchro(){
        Preferences prefs = Preferences.userNodeForPackage(BackupScheduler.class);

        // Lecture des valeurs (avec fallback sur les valeurs par défaut)
        String email = prefs.get("SYNC_EMAIL", "ralaiseheno25yakini@gmail.com");
        String googlePass = prefs.get("SYNC_GOOGLE_PASSWORD", "");
        String lastSync = prefs.get("LAST_BACKUP_DATE", "Inconnue");

        // Affichage dans les champs de saisie
        txtEmailUser.setText(email);
        txtCodeGoogleApp.setText(googlePass);
        lblStatutSync.setText("Dernière synchro : " + lastSync);
    }

    //enregistrer dans la base de registre de l'application des config
    @FXML
    private void handleEnregistrerConfigSync(ActionEvent event) {
        // Sauvegarder les clés en base ou fichier properties
        String email = txtEmailUser.getText().trim();
        String googlePassword = txtCodeGoogleApp.getText().trim();

        if (email.isBlank() || googlePassword.isBlank()) {
            utilitaire.afficherErreur("Champs manquants", "Veuillez saisir l'adresse e-mail et le mot de passe d'application Google.");
            return;
        }

        Preferences prefs = Preferences.userNodeForPackage(DepenseNakaApplication.class);
        prefs.put("SYNC_EMAIL", email);
        prefs.put("SYNC_GOOGLE_PASSWORD", googlePassword);/*otdt fctu wtoz flwv*/

        utilitaire.afficherInformation("Configuration enregistrée", "Vos paramètres de synchronisation ont été sauvegardés.");
    }

    @FXML
    private void handleLancerSyncManuelle(ActionEvent event) {
        progressSync.setVisible(true);

        Task<Boolean> syncTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return backupScheduler.effectuerSauvegardeManuelOuAuto();
            }
        };

        syncTask.setOnSucceeded(e -> {
            progressSync.setVisible(false);
            boolean succes = syncTask.getValue();
            if (succes) {
                String dateNow = LocalDate.now().toString();
                lblStatutSync.setText("Dernière synchro : " + dateNow);
                utilitaire.afficherInformation("Succès", "Synchronisation terminée avec succès (OneDrive & Email) !");
            } else {
                lblStatutSync.setText("Échec de la dernière synchro");
                utilitaire.afficherErreur("Échec", "Impossible de réaliser la synchronisation.");
            }
        });

        syncTask.setOnFailed(e -> {
            progressSync.setVisible(false);
            lblStatutSync.setText("Échec de la dernière synchro");
            utilitaire.afficherErreur("Erreur", "Une erreur inattendue est survenue lors de la synchronisation.");
        });

        new Thread(syncTask).start();
    }

    private void demarrerPlanificateurNotifications() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Rappel toutes les 15 minutes à partir du démarrage
        scheduler.scheduleAtFixedRate(() -> {
            // Vérifier si la synchro mensuelle a été faite
            boolean synchroMensuelleRequise = true;

            if (synchroMensuelleRequise) {
                // Interagir avec l'interface graphique JavaFX via Platform.runLater
                Platform.runLater(() ->
                        //afficherAlerteRappel("Rappel : La synchronisation mensuelle de la base de données n'a pas été effectuée !"));
                        utilitaire.afficherInformation("Rappel"," La synchronisation mensuelle de la base de données n'a pas été effectuée !"));
            }
        }, 15, 15, TimeUnit.MINUTES);
    }

    // ----------------------------------------------------------------------------------
    // 3. ONGLET 3 : MISE À JOUR GIT
    // ----------------------------------------------------------------------------------
    @FXML
    private void handleTelechargerMaj(ActionEvent event) {
        String urlSaisie = txtUrlGit.getText() != null ? txtUrlGit.getText().trim() : "";

        // 1. Validation syntaxique de l'URL
        if (urlSaisie.isBlank() || !urlSaisie.startsWith("http")) {
            utilitaire.afficherErreur("URL invalide", "Veuillez saisir une URL valide (ex: https://github.com/user/repo/releases/download/v1.1/update.zip).");
            return;
        }

        progressMAJ.setVisible(true);
        progressMAJ.progressProperty().unbind();
        progressMAJ.setProgress(-1); // mode indéterminé
        lblStatutMAJ.setText("Vérification de la mise à jour...");

        Task<Boolean> majTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // 2. Vérification HTTP (HEAD request pour contrôler l'existence sans tout télécharger)
                URL url = new URI(urlSaisie).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(5000); // 5s timeout
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("❌ Erreur serveur HTTP : " + responseCode);
                    return false;
                }

                // Vérification de la taille du fichier (optionnel)
                long fileSize = connection.getContentLengthLong();
                if (fileSize <= 0) {
                    System.err.println("❌ Fichier de mise à jour vide ou invalide.");
                    return false;
                }

                // 3. Téléchargement réel du fichier
                updateMessage("Téléchargement de la mise à jour...");
                HttpURLConnection downloadConn = (HttpURLConnection) url.openConnection();
                downloadConn.setRequestMethod("GET");

                File tempFile = new File(System.getProperty("java.io.tmpdir"), "update_depensenaka.zip");

                try (InputStream in = downloadConn.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile)) {

                    byte[] buffer = new byte[4096];
                    long bytesDownloaded = 0;
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        bytesDownloaded += bytesRead;

                        // Mise à jour de la barre de progression JavaFX
                        if (fileSize > 0) {
                            updateProgress(bytesDownloaded, fileSize);
                        }
                    }
                }

                // 4. Vérification post-téléchargement (Le fichier existe-t-il et fait-il plus de 0 Ko ?)
                return tempFile.exists() && tempFile.length() > 0;
            }
        };

        progressMAJ.progressProperty().bind(majTask.progressProperty());

        majTask.setOnSucceeded(e -> {
            progressMAJ.setVisible(false);
            boolean succes = majTask.getValue();

            if (succes) {
                lblStatutMAJ.setText("Mise à jour téléchargée avec succès. Redémarrez l'application.");
                utilitaire.afficherInformation("Succès", "La mise à jour a été téléchargée dans votre dossier temporaire.");
            } else {
                lblStatutMAJ.setText("Échec de la mise à jour : Fichier introuvable ou invalide.");
                utilitaire.afficherErreur("Échec", "L'URL indiquée ne contient pas une mise à jour valide.");
            }
        });

        majTask.setOnFailed(e -> {
            progressMAJ.setVisible(false);
            lblStatutMAJ.setText("Erreur de connexion.");
            utilitaire.afficherErreur("Erreur réseau", "Impossible d'atteindre l'URL saisie. Vérifiez la connexion ou l'adresse.");
        });

        new Thread(majTask).start();
    }


    // --- Utilitaires ---
    private void afficherInformation(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.show();
    }



    // Class wrapper pour la TableView
    public static class ProduitImportModel {
        private  ProduitDTO dto;
        private  SimpleStringProperty statut;

        public ProduitImportModel(ProduitDTO dto, String statut) {
            this.dto = dto;
            this.statut = new SimpleStringProperty(statut);
        }

        public ProduitImportModel(String designation, String categorie, String categorie1, String taille, double prixA, double prixV, double quantiteV, String estDispo, String enAttente) {
        }

        public ProduitDTO getDto() { return dto; }
        public String getStatut() { return statut.get(); }
        public SimpleStringProperty statutProperty() { return statut; }
        public void setStatut(String val) { this.statut.set(val); }
    }
}