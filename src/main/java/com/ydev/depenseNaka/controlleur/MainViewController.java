package com.ydev.depenseNaka.controlleur;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.ydev.depenseNaka.configuration.StageManager;
import com.ydev.depenseNaka.repository.UtilisateurRepository;
import com.ydev.depenseNaka.service.ConnexionService;
import com.ydev.depenseNaka.service.UtilisateurService;
import com.ydev.depenseNaka.session.UserSession;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class MainViewController {

    private final ApplicationContext springContext;
    private final ConnexionService connexionService;
    private final UtilisateurService utilisateurService;
    private final Utilitaire utilitaire;

    @FXML private HBox titleBar;
    @FXML private Label lblVueActuelle;
    @FXML private Label lblDateTime;
    @FXML private Label lblUtilisateur;
    @FXML private FontIcon iconTheme;
    @FXML private VBox contentArea;
    @FXML private VBox sidebar;
    @FXML private Button btnBurger;


    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnProduits;
    @FXML
    private Button btnClient;
    @FXML
    private Button btnVentes;
    @FXML
    private Button btnFactures;
    @FXML
    private Button btnCommandes;
    @FXML
    private Button btnUtilisateurs;
    @FXML
    private Button btnStatistiques;
    @FXML
    private Button btnParametre;
    @FXML
    private Button btnDeconnexion;

    private String idSessionActuelle;
    private Stage primaryStage;
    private PauseTransition inactivityTimer;
    private EventHandler<InputEvent> userActivityFilter;

    private Button currentSelectedButton;
    private boolean isMaximized = false;
    private double prevX, prevY, prevWidth, prevHeight;

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDarkMode = true;

    @FXML
    public void initialize() {
        initDragAndDropWindow();
        initClock();
        //setActiveMenu(btnDashboard);
        highlightButton(btnDashboard);
        verifierAcces();
        switchPage("/vue/DashboardView.fxml", "Tableau de bord");

        Platform.runLater(this::setupInactivityFilter);

    }

    // Gestion du déplacement de la fenêtre sans bordure
    private void initDragAndDropWindow() {
        if (titleBar == null) {
            System.out.println("Varaiable non injectée: "+titleBar);
            return;
        }
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    //verification accès
    public void verifierAcces(){
        UserSession sessionEncour = UserSession.getInstance();
        if(sessionEncour != null && sessionEncour.getIdUtilisateur() != null){
            String idUtilisateur = sessionEncour.getIdUtilisateur();
            boolean isAdmin = utilisateurService.isAdmin(idUtilisateur);
            Utilitaire.restreindreAcces(btnStatistiques,isAdmin);
            Utilitaire.restreindreAcces(btnUtilisateurs,isAdmin);
        }
        lblUtilisateur.setText(sessionEncour.getPseudo());
    }

    // Horloge dynamique
    private void initClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e ->
                lblDateTime.setText(LocalDateTime.now().format(formatter))
        ), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void toggleSidebar() {
        boolean isVisible = sidebar.isVisible();
        sidebar.setVisible(!isVisible);
        sidebar.setManaged(!isVisible);

        // Ajustement automatique de la zone centrale quand la sidebar disparaît
        if (isVisible) {
            AnchorPane.setLeftAnchor(contentArea, 0.0);
        } else {
            AnchorPane.setLeftAnchor(contentArea, 240.0);
        }
    }


    //surbrillance du bouton actif
    private void highlightButton(Button button) {
        if (currentSelectedButton != null) {
            currentSelectedButton.getStyleClass().remove("success");
            currentSelectedButton.getStyleClass().add("flat");
        }
        button.getStyleClass().remove("flat");
        button.getStyleClass().add("success");

        currentSelectedButton = button;
    }

    // Basculement Dark/Light Theme AtlantaFX
    @FXML
    private void toggleTheme() {
        if (isDarkMode) {
            Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
            iconTheme.setIconLiteral("fas-sun");
            isDarkMode = false;
        } else {
            Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
            iconTheme.setIconLiteral("fas-moon");
            isDarkMode = true;
        }
    }

    // Méthode générique pour charger n'importe quelle sous-page FXML de manière fluide
    //charger la page quand on clique sur un bouton
    private void switchPage(String fxmlPath, String title) {
        try {
            lblVueActuelle.setText(title);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            // Liaison indispensable avec Spring Boot
            loader.setControllerFactory(springContext::getBean);
            Parent page = loader.load();

            //Ajuster la page pour occuper toutes les espaces
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);

            //remplacer le contenu dela zone centrale
            contentArea.getChildren().setAll(page);
        }catch (Exception e){
            System.err.println("Erreur lors du chargement de la vue : " + fxmlPath);
            e.printStackTrace();
        }
    }


    // Navigation dans le menu principal
    @FXML
    private void handleMenuChange(ActionEvent event) {
        Button sourceButton = (Button)event.getSource();

        if (sourceButton == btnDashboard) {
            switchPage("/vue/DashboardView.fxml", "Tableau de bord");
        } else if (sourceButton == btnProduits) {
            switchPage("/vue/ProduitView.fxml", "Gestion des produits");
        }else if (sourceButton == btnClient) {
            switchPage("/vue/ClientView.fxml", "Gestion des clients");
        }else if (sourceButton == btnVentes) {
            switchPage("/vue/VenteView.fxml", "Terminale de vente");
        }else if (sourceButton == btnStatistiques) {
            switchPage("/vue/StatistiqueView.fxml", "Statistiques ");
        }else if (sourceButton == btnCommandes) {
            switchPage("/vue/CommandeView.fxml", "Gestion des commandes");
        }else if (sourceButton == btnParametre) {
            switchPage("/vue/ParametreView.fxml", "Paramètres");
        }else if (sourceButton == btnUtilisateurs) {
            switchPage("/vue/UtilisateurView.fxml", "Gestion des utilisateurs");
        }else if (sourceButton == btnFactures) {
            switchPage("/vue/FactureView.fxml", "Gestion des factures");
        }

        highlightButton(sourceButton);
    }

    // Contrôles de la fenêtre
    @FXML
    private void handleMaximize(ActionEvent event) {
        // Récupère le Stage à partir du bouton qui a déclenché l'événement
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        if (!isMaximized) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            isMaximized = true;
        } else {
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            isMaximized = false;
        }
    }

    @FXML
    private void handleMinimize(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        idSessionActuelle = UserSession.getInstance().getIdConnexion();
        // 3. Désactiver la session dans la base de données via le service
        if (idSessionActuelle != null) {
            // Récupère l'ID réel stocké en mémoire de l'application
            connexionService.logOut(idSessionActuelle);
            SecurityContextHolder.clearContext();
            UserSession.clearSession(); // Nettoyer la mémoire
            System.out.println("Session désactivée en BDD avec succès.");
        }

        stopperEtNettoyerTimer();

        Platform.exit();
        stage.close();
    }

    @FXML
    private void handleDeconnexion(ActionEvent actionEvent) {
        lblVueActuelle.setText("Déconnexion...");

        // 1. Création de la boîte de dialogue de confirmation moderne
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de déconnexion");
        alert.setHeaderText("Fermeture de session");
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter de l'application ?");

        utilitaire.appliquerLogoAlerte(alert);

        // Application du style AVANT d'afficher
        utilitaire.styleModernAlert(alert, "fas-sign-out-alt", true);

        // Personnalisation des textes des boutons
        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.setText("Se déconnecter");

        Button cancelBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) cancelBtn.setText("Annuler");

        // 2. Récupération de l'ID de session
        idSessionActuelle = UserSession.getInstance().getIdConnexion();


        // 3. UN SEUL showAndWait() avec capture de la réponse
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (inactivityTimer != null) {
                    inactivityTimer.stop();
                }
                try {
                    // Désactiver la session en BDD
                    if (idSessionActuelle != null) {
                        connexionService.logOut(idSessionActuelle);
                        SecurityContextHolder.clearContext();
                        UserSession.clearSession();
                        System.out.println("Session désactivée en BDD avec succès.");
                    }

                    stopperEtNettoyerTimer();

                    // Charger la vue de Login (LoginView.fxml)
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/LoginView.fxml"));
                    loader.setControllerFactory(springContext::getBean);
                    Parent loginRoot = loader.load();

                    // Récupérer le Stage actuel
                    Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                    // Redimensionner et afficher
                    Scene scene = new Scene(loginRoot);
                    stage.setScene(scene);

                    stage.setMaximized(false);
                    stage.setResizable(false);

                    stage.setWidth(450);
                    stage.setHeight(500);

                    stage.centerOnScreen();

                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    // Affichage d'erreur bien stylisé (style appliqué AVANT showAndWait)
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText("Échec de la déconnexion");
                    errorAlert.setContentText("Une erreur est survenue : " + e.getMessage());
                    utilitaire.styleModernAlert(errorAlert, "fas-exclamation-triangle", true);
                    errorAlert.showAndWait();
                }
            } else {
                System.out.println("Déconnexion annulée par l'utilisateur.");
            }
        });

    }

    //*******GESTION DE SESSION
    //fermeture automatique de session et deconnection

    private void fermerSessionAutomatiquement() {
        // 1. DÉTRUIRE ET RETIRER LE TIMER D'ABORD !
        stopperEtNettoyerTimer();

        try {
            // 2. Inactiver la session en BDD
            UserSession sessionActuelle = UserSession.getInstance();
            if (sessionActuelle != null) {
                String idSession = sessionActuelle.getIdConnexion();
                if (idSession != null && connexionService != null) {
                    connexionService.logOut(idSession);
                    System.out.println("Session BDD désactivée avec succès.");
                }
            }

            // 3. Nettoyer la mémoire localement
            SecurityContextHolder.clearContext();
            UserSession.clearSession();

            // 4. Charger la vue de Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/LoginView.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent loginRoot = loader.load();

            // 5. Rediriger le Stage conservé
            if (primaryStage != null) {
                Scene scene = new Scene(loginRoot);
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                primaryStage.show();
            } else {
                return;
            }

            // 6. Dialogue d'information (le timer ne se relancera plus en arrière-plan)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Session expirée");
            alert.setHeaderText("Déconnexion automatique");
            alert.setContentText("Votre session a été fermée suite à 15 minutes d'inactivité.");
            // alert.show();

        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture automatique de la session :");
            e.printStackTrace();
        }
    }

    //Ecouteur d'évènement
    private void setupInactivityFilter() {
       // inactivityTimer = new PauseTransition(Duration.seconds(30)); // Mettre Duration.minutes(15) en prod
        inactivityTimer = new PauseTransition(Duration.minutes(15));

        inactivityTimer.setOnFinished(event -> {
            System.out.println("Inactivité détectée. Déconnexion automatique...");
            fermerSessionAutomatiquement();
        });

        // 2. Définition du filtre d'activité
        userActivityFilter = event -> {
            if (inactivityTimer != null) {
                inactivityTimer.playFromStart();
            }
        };

        if (contentArea != null) {
            contentArea.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                        if (newWindow instanceof Stage stage) {
                            this.primaryStage = stage;

                            stage.setOnCloseRequest(e -> stopperEtNettoyerTimer());
                        }
                    });

                    // Attacher le filtre d'activité
                    newScene.addEventFilter(InputEvent.ANY, userActivityFilter);
                    inactivityTimer.play();
                }
            });

            // Cas où la scène est déjà attachée
            if (contentArea.getScene() != null) {
                Scene scene = contentArea.getScene();
                if (scene.getWindow() instanceof Stage stage) {
                    this.primaryStage = stage;
                }

                scene.addEventFilter(InputEvent.ANY, userActivityFilter);
                inactivityTimer.play();
            }
        }
    }

    private void stopperEtNettoyerTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.stop();
            inactivityTimer = null; // Libérer la mémoire
        }

        // Supprimer le filtre d'événements de la scène courante
        if (contentArea != null && contentArea.getScene() != null && userActivityFilter != null) {
            contentArea.getScene().removeEventFilter(InputEvent.ANY, userActivityFilter);
        }
    }


}