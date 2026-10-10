package com.ydev.depenseNaka.update;

import com.ydev.depenseNaka.DepenseNakaApplication;
import com.ydev.depenseNaka.utilitaire.Utilitaire;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.prefs.Preferences;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class BackupScheduler {

    private final EmailBackupService emailBackupService;
    private final Utilitaire utilitaire;
    //passer à 'false' en production
    private static final boolean IS_TEST_MODE = false;

    private Preferences getPrefs() {
        return Preferences.userNodeForPackage(DepenseNakaApplication.class);
    }

    /*
     * otdt fctu wtoz flwv
     * */


    public boolean effectuerSauvegardeManuelOuAuto() {
        Preferences prefs = getPrefs();
        String email = prefs.get("SYNC_EMAIL", "ralaiseheno25yakini@gmail.com");
        String appPassword = prefs.get("SYNC_GOOGLE_PASSWORD", "otdt fctu wtoz flwv");

        // 1. Récupération du chemin de la BDD
        String dbPath = prefs.get("DB_PATH", obtenirCheminBddDefaut());
        File currentDb = new File(dbPath);

        if (!currentDb.exists()) {
            currentDb = new File("data" + File.separator + "depensenaka.db");
        }

        if (!currentDb.exists()) {
            currentDb = new File("depensenaka.db");
        }

        if (!currentDb.exists()) {
            System.err.println("❌ Fichier BDD introuvable à : " + dbPath);
            return false;
        }

        // 2. Copie locale vers OneDrive
        String fileNameBackup = "depensenaka_backup_" + LocalDate.now() + ".db";
        File backupFile = copierVersOneDrive(currentDb, fileNameBackup);

        if (backupFile != null && backupFile.exists()) {
            boolean succesEmail = false;

            // 3. Tentative d'envoi d'e-mail si la configuration existe
            if (!email.isBlank() && !appPassword.isBlank()) {
                succesEmail = emailBackupService.envoyerSauvegardeParEmail(backupFile, email, appPassword);
            }

            // 4. On met à jour la date si la copie OneDrive ou l'email a réussi
            prefs.put("LAST_BACKUP_DATE", LocalDate.now().toString());
            System.out.println("✅ Synchronisation réussie. Prochaine échéance dans 30 jours.");
            return true;
        }

        return false;
    }

    public boolean sauvegardeEnRetard() {
        if (IS_TEST_MODE){
            return true;
        }
        Preferences prefs = getPrefs();
        String lastBackupStr = prefs.get("LAST_BACKUP_DATE", null);

        // Si aucune sauvegarde n'a encore été effectuée, la synchronisation est requise
        if (lastBackupStr == null || lastBackupStr.isBlank()) {
            return true;
        }

        try {
            LocalDate lastBackup = LocalDate.parse(lastBackupStr);
            // Vérifie si 30 jours ou plus se sont écoulés
            return ChronoUnit.DAYS.between(lastBackup, LocalDate.now()) >= 30;
        } catch (Exception e) {
            // En cas de format de date invalide, forcer la sauvegarde
            return true;
        }
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT15M")
    //@Scheduled(initialDelayString = "PT1M", fixedDelayString = IS_TEST_MODE ? "PT1M" : "PT15M")
    public void verifierEtLancerSyncMensuelle() {
        if (sauvegardeEnRetard()) {
            if (estConnecteInternet()) {
                System.out.println("⏰ 30 jours écoulés : Lancement de la sauvegarde mensuelle automatique...");
                effectuerSauvegardeManuelOuAuto();
            } else {
                System.out.println("⚠️ Sauvegarde mensuelle due, mais pas de connexion Internet. Nouvelle tentative ultérieure.");
            }
        }
    }

    /**
     * Détermine dynamiquement le chemin par défaut de la BDD
     * en s'adaptant au disque utilisateur (C:, D:, etc.) et à OneDrive.
     */
    public String obtenirCheminBddDefaut() {
        String userHome = System.getProperty("user.home");
        String oneDriveFolder = System.getenv("OneDrive");

        String dossierData = userHome + File.separator + "DepenseNaka" + File.separator + "Data";

        File dir = new File(dossierData);
        if (!dir.exists()) {
            dir.mkdirs(); // Crée automatiquement le dossier s'il n'existe pas
        }

        return dossierData + File.separator + "depensenaka.db";
    }

    public File copierVersOneDrive(File currentDb, String backupFileName) {
        String userHome = System.getProperty("user.home");
        String oneDriveFolder = System.getenv("OneDrive");

        File backupDir = (oneDriveFolder != null && !oneDriveFolder.isBlank())
                ? new File(oneDriveFolder + File.separator + "DepenseNaka_Backups")
                : new File(userHome + File.separator + "DepenseNaka" + File.separator + "Backups");

        if (!backupDir.exists()) backupDir.mkdirs();

        File backupFile = new File(backupDir, backupFileName);

        try {
            Files.copy(currentDb.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("📁 Copie BDD réussie dans : " + backupFile.getAbsolutePath());
            return backupFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean estConnecteInternet() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @Scheduled(fixedDelay = 900000) // Vérification toutes les 15 minutes
    public void verifierEtSynchroniserAutomatiquement() {
        if (sauvegardeEnRetard() && estConnecteInternet()) {
            effectuerSauvegardeManuelOuAuto();
        }
    }
}