package com.ydev.depenseNaka.controlleur;

import atlantafx.base.theme.NordDark;
import com.ydev.depenseNaka.main.StageReadyEvent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageListener implements ApplicationListener<StageReadyEvent> {
    //injection par cnstructeur par constructeur
    private final ApplicationContext applicationContext;

    public StageListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        System.out.println("Tentative d'ouverture de la fenetre!!!");
        // 1. Appliquer le thème AtlantaFX globalement à l'application
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        try {
            Stage stage = event.getStage();
           // FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/MainView.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/LoginView.fxml"));

            //permet à spring de gérer les controlleurs
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(true);
            stage.setScene(new Scene(root,1200,800));
            stage.setTitle("DepenseNaka");

            stage.show();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
