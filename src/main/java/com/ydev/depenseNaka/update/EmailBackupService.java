package com.ydev.depenseNaka.update;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Properties;

@Service
public class EmailBackupService {

    /**
     * Envoie la sauvegarde BDD via SMTP Gmail configuré dynamiquement.
     * otdt fctu wtoz flwv
     */
    public boolean envoyerSauvegardeParEmail(File backupFile, String email, String appPassword) {
        if (email == null || email.isBlank() || appPassword == null || appPassword.isBlank()) {
            System.err.println("❌ Adresse e-mail ou mot de passe d'application Google non renseigné.");
            return false;
        }

        try {
            // Configuration dynamique du JavaMailSender pour Gmail SMTP
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername(email);
            mailSender.setPassword(appPassword);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "false");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(email);
            helper.setTo(email); // Envoi à soi-même
            helper.setSubject("DepenseNaka - Sauvegarde de la Base de Données (" + backupFile.getName() + ")");
            helper.setText("Bonjour,\n\nVeuillez trouver en pièce jointe la sauvegarde de votre base de données DepenseNaka.\n\nCordialement,\nDepenseNaka Application");

            FileSystemResource fileSystemResource = new FileSystemResource(backupFile);
            helper.addAttachment(backupFile.getName(), fileSystemResource);

            mailSender.send(message);
            System.out.println("✅ Sauvegarde envoyée avec succès par e-mail à : " + email);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du mail de sauvegarde : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}