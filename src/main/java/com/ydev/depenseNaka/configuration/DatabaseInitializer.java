package com.ydev.depenseNaka.configuration;

import com.ydev.depenseNaka.DepenseNakaApplication;
import com.ydev.depenseNaka.update.BackupScheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.prefs.Preferences;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    // Identifiants par défaut pour la synchronisation
    private static final String DEFAULT_EMAIL = "ralaiseheno25yakini@gmail.com";
    private static final String DEFAULT_GOOGLE_PASS = "otdt fctu wtoz flwv";

    @Override
    public void run(String... args) throws Exception {
        Preferences prefs = Preferences.userNodeForPackage(DepenseNakaApplication.class);

        // 1. Définition du chemin
        String userHome = System.getProperty("user.home");
        String oneDriveFolder = System.getenv("OneDrive");


        String dossierBaseDefaut = userHome + File.separator + "DepenseNaka" + File.separator + "Data";

        String dbPathDefaut = dossierBaseDefaut + File.separator + "depensenaka.db";


        if (prefs.get("DB_PATH", null) == null) {
            prefs.put("DB_PATH", dbPathDefaut);
        }

        String dbPathActuel = prefs.get("DB_PATH", dbPathDefaut);
        File dbFile = new File(dbPathActuel);

        // 🔍 AFFICHAGE DANS LA CONSOLE AU DÉMARRAGE
        System.out.println("--------------------------------------------------");
        System.out.println(" BASE DE DONNÉES ACTUELLEMENT UTILISÉE :");
        System.out.println("   --> " + dbFile.getAbsolutePath());
        System.out.println("--------------------------------------------------");

        // 2. Création des dossiers parent uniquement (PAS du fichier vide)
        if (!dbFile.getParentFile().exists()) {
            boolean dossierCree = dbFile.getParentFile().mkdirs();
            if (dossierCree) {
                System.out.println("📁 Dossier de la BDD créé avec succès : " + dbFile.getParentFile().getAbsolutePath());
            }
        }

        // Si une base de données existait déjà à la racine du projet (ex: depensenaka.db de 208 Ko),
        // on la déplace/copie vers le nouveau dossier au lieu de créer un fichier vide de 0 Ko
        File localDb = new File("depensenaka.db");
        if (!dbFile.exists() && localDb.exists() && localDb.length() > 0) {
            try {
                Files.copy(localDb.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("📦 Migration de l'ancienne BDD vers : " + dbFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 3. Configuration par défaut
        if (prefs.get("SYNC_EMAIL", null) == null) {
            prefs.put("SYNC_EMAIL", DEFAULT_EMAIL);
        }

        if (prefs.get("SYNC_GOOGLE_PASSWORD", null) == null) {
            prefs.put("SYNC_GOOGLE_PASSWORD", DEFAULT_GOOGLE_PASS);
        }
    }

}