package com.ydev.depenseNaka.configuration;

import com.ydev.depenseNaka.main.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StageManager {
    private final ConfigurableApplicationContext springContext;
    private Stage primaryStage;

  /*  public StageManager(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }*/

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // Récupère automatiquement le Stage au démarrage de l'application
    @EventListener
    public void handleStageReady(StageReadyEvent event) {
        this.primaryStage = event.getStage();
        switchScene("/vue/LoginView.fxml", "DepenseNaka - Connexion");
    }

    public void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
