package com.ydev.depenseNaka.controlleur;

import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.exception.NotFoundUtilisateur;
import com.ydev.depenseNaka.service.UtilisateurService;
import com.ydev.depenseNaka.session.UserSession;
import com.ydev.depenseNaka.configuration.StageManager;
import com.ydev.depenseNaka.dto.ConnexionDTO;
import com.ydev.depenseNaka.exception.IncorrectLogin;
import com.ydev.depenseNaka.model.UtilisateurEntity;
import com.ydev.depenseNaka.repository.UtilisateurRepository;
import com.ydev.depenseNaka.service.ConnexionService;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginControlleur {

    private final ConnexionService connexionService;
    private final UtilisateurRepository utilisateurRepository;
    private final StageManager stageManager;
    private final Utilitaire utilitaire;
    private final UtilisateurService utilisateurService;
    private final ApplicationContext  applicationContext;

    @FXML private TextField txtPseudo;
    @FXML private PasswordField txtMdp;
    @FXML private Label lblErreur;



    public void initialize() {
        Platform.runLater(() -> {
            txtPseudo.requestFocus();
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String pseudo = txtPseudo.getText();
        String password = txtMdp.getText();

        if (pseudo.isBlank() || password.isBlank()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        try {
            // 1. Authentification & enregistrement de la session en base
            ConnexionDTO connexion = connexionService.login(pseudo, password);

            // 2. Recupération de l'utilisateur de l'utilisateur pour extraire son rôle et créer le Token Spring Security
            UtilisateurEntity userEntity = utilisateurRepository.findByPseudoUtilisateur(pseudo)
                    .orElseThrow(()-> new RuntimeException("Utilisateur introuvable: "+pseudo));

            // 3. Initialisation de la session globale (Singleton)
            UserSession.createSession(connexion.idConnexion(),
                    userEntity.getIdUtilisateur(),
                    userEntity.getPseudoUtilisateur(),
                    userEntity.getRoleUtilisateur()
                    );


            utilisateurRepository.findByPseudoUtilisateur(connexion.pseudoUtilisateur()).ifPresent(user -> {
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(user.getRoleUtilisateur().name())
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getPseudoUtilisateur(), null, authorities);

                // Définition de l'authentification globale Spring Security
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });

            // 3. Redirection vers la vue principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/MainView.fxml"));
           // stageManager.switchScene("/vue/MainView.fxml", "DepenseNaka - Tableau de bord");

            //injection de contexte spring
            loader.setControllerFactory(applicationContext::getBean);
            Parent mainLayoutRoot = (Parent) loader.load();

            //recupéraion du stage actuel à partir de l'évènement du clic du bouton
            Stage stage = (Stage) ((Node)(event.getSource())).getScene().getWindow();

            //Changer la scène avec une taille adaptée au menu principal (ex: 1200x750)
            Scene scene = new Scene(mainLayoutRoot);
            stage.setScene(scene);

            stage.setMaximized(false);
            stage.setWidth(1200);
            stage.setHeight(800);

            stage.centerOnScreen();
            stage.show();



        }catch (IncorrectLogin e) {
                // Ne capturer QUE l'erreur de login ici
                afficherErreur("Login ou mot de passe incorrect.");
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement de l'application : " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private void afficherErreur(String message) {
        lblErreur.setText(message);
        lblErreur.setVisible(true);
    }

    @FXML
    private void handleMinimiser(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleFermer(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}