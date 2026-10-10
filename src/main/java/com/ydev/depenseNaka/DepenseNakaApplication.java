package com.ydev.depenseNaka;

import com.ydev.depenseNaka.configuration.StageManager;
import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.main.StageReadyEvent;
import com.ydev.depenseNaka.mapper.UtilisateurMapper;
import com.ydev.depenseNaka.model.RoleUtilisateur;
import com.ydev.depenseNaka.repository.UtilisateurRepository;
import com.ydev.depenseNaka.service.UtilisateurService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.prefs.Preferences;

@SpringBootApplication
public class DepenseNakaApplication extends Application implements CommandLineRunner {
	private ConfigurableApplicationContext context;
	@Autowired
	private UtilisateurRepository utilisateurRepository;

	@Autowired
	private UtilisateurService utilisateurService;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("DepenseNakaApplication a bien demarrer");
/*
		String adminPseudo = "admin";

		// Vérifier si l'administrateur existe déjà pour éviter d'essayer de le créer à chaque redémarrage
		if (!utilisateurRepository.existsByPseudoUtilisateur(adminPseudo)) {

			// Création du DTO de l'utilisateur par défaut
			UtilisateurDTO admin = UtilisateurDTO.builder()
					.nomUtilisateur("admin")
					.prenomUtilisateur("admin")
					.adresseUtilisateur("admin")
					.pseudoUtilisateur("admin")
					.mdpUtilisateur("admin")
					.roleUtilisateur(RoleUtilisateur.ADMINISTRATEUR)
					.build();

			// Appel de votre méthode de création existante
			utilisateurService.createUtilisateur(admin);

			System.out.println("--> Administrateur par défaut crée avec succès (Pseudo : " + admin.pseudoUtilisateur() + ")/mdp: "+ admin.mdpUtilisateur());
		}*/
		System.out.println("********* CA PASSE*****************");
	}

	public static void main(String[] args) {
		//SpringApplication.run(DepenseNakaApplication.class, args);
		Application.launch(DepenseNakaApplication.class, args);
	}

	//lancement de spring boot au demarage
	@Override
	public void init(){
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

		String dbPath = obtenirEtVerifierCheminBdd();
		String jdbcUrl = "jdbc:sqlite:" + dbPath + "?journal_mode=WAL&busy_timeout=5000";
		this.context = new SpringApplicationBuilder()
				.sources(DepenseNakaApplication.class)
				.properties("spring.datasource.url=" + jdbcUrl)
				.run();
	}

	//publie un évènement pour dire qu'un évènement est prêt
	@Override
	public void start(Stage primaryStage) throws Exception {
		Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
		primaryStage.getIcons().add(appIcon);
		this.context.publishEvent(new StageReadyEvent(primaryStage));
	}

	//ferme l'application
	@Override
	public void stop() {
		context.close();
		Platform.exit();
	}

	/**
	 * Récupère le chemin configuré de la BDD ou initialise le chemin par défaut.
	 * Crée le dossier parent et copie la BDD modèle si nécessaire.
	 */
	private String obtenirEtVerifierCheminBdd() {
		Preferences prefs = Preferences.userNodeForPackage(DepenseNakaApplication.class);

		// Emplacement par défaut (OneDrive ou dossier Utilisateur)
		String userHome = System.getProperty("user.home");
		String oneDriveFolder = System.getenv("OneDrive");

		String dossierBaseDefaut = userHome + File.separator + "DepenseNaka" + File.separator + "Data";

		String cheminDefaut = dossierBaseDefaut + File.separator + "depensenaka.db";

		// Récupérer le chemin personnalisé enregistré dans Java Preferences
		String dbPath = prefs.get("DB_PATH", cheminDefaut);
		File dbFile = new File(dbPath);

		// Créer les dossiers parents s'ils n'existent pas
		File parentDir = dbFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			boolean created = parentDir.mkdirs();
			if (created) {
				System.out.println("📁 Dossier de base de données créé : " + parentDir.getAbsolutePath());
			}
		}

		// Si le fichier .db n'existe pas encore ou est vide chez le client, on copie la BDD modèle
		if (!dbFile.exists() || dbFile.length() == 0) {
			copierBaseDeDonneesInitiale(dbFile);
		}

		System.out.println(">>> Connexion SQLite vers : " + dbFile.getAbsolutePath());
		return dbFile.getAbsolutePath();
	}

	/**
	 * Copie la base de données initiale pré-remplie depuis les resources
	 * vers le dossier de destination utilisateur.
	 */
	private void copierBaseDeDonneesInitiale(File destination) {
		try (InputStream is = getClass().getResourceAsStream("/depensenaka.db")) {
			if (is != null) {
				Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("📦 Base de données initiale pré-remplie copiée vers : " + destination.getAbsolutePath());
			} else {
				System.err.println("⚠️ ATTENTION : Le fichier modèle /depensenaka.db n'a pas été trouvé dans src/main/resources !");
			}
		} catch (Exception e) {
			System.err.println("❌ Erreur lors de la copie de la base de données initiale : " + e.getMessage());
			e.printStackTrace();
		}
	}


}
