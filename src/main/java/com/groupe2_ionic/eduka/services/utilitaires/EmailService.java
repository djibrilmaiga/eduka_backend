package com.groupe2_ionic.eduka.services.utilitaires;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String expediteur;

    @Value("${app.name:EduKa}")
    private String appName;

    /**
     * Envoie un email simple.
     */
    public String envoyerEmail(String to, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(expediteur);
            message.setTo(to);
            message.setSubject(sujet);
            message.setText(contenu);

            javaMailSender.send(message);
            log.info("Email simple envoyé avec succès à {}", to);
            return "Message envoyé avec succès !";
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email simple: {}", e.getMessage());
            return "Erreur lors de l'envoie de l'email !";
        }
    }

    /**
     * Envoie un email HTML avec pièces jointes
     */
    public boolean envoyerEmailHtml(String to, String sujet, String contenuHtml, List<PieceJointe> piecesJointes) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur);
            helper.setTo(to);
            helper.setSubject(sujet);
            helper.setText(contenuHtml, true);

            // Ajouter les pièces jointes
            if (piecesJointes != null) {
                for (PieceJointe pj : piecesJointes) {
                    helper.addAttachment(pj.nom(), new ByteArrayResource(pj.contenu()));
                }
            }

            javaMailSender.send(message);
            log.info("Email HTML envoyé avec succès à {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envoie un email de bienvenue
     */
    public boolean envoyerEmailBienvenue(String to, String nom, String typeUtilisateur) {
        String sujet = "Bienvenue sur " + appName + " !";
        String contenuHtml = genererTemplateHtml(
                "Bienvenue " + nom + " !",
                "Votre inscription en tant que " + typeUtilisateur + " a été confirmée avec succès.",
                "Vous pouvez maintenant accéder à toutes les fonctionnalités de la plateforme.",
                "Se connecter",
                "#"
        );

        return envoyerEmailHtml(to, sujet, contenuHtml, null);
    }

    /**
     * Envoie un email de confirmation de paiement
     */
    public boolean envoyerConfirmationPaiement(String to, String nomParrain, double montant, String nomEnfant, String transactionId) {
        String sujet = "Confirmation de paiement - " + appName;
        String contenuHtml = genererTemplateHtml(
                "Paiement confirmé !",
                "Cher(e) " + nomParrain + ",",
                String.format("Votre paiement de %.0f FCFA pour %s a été confirmé avec succès.<br><br>Référence: %s",
                        montant, nomEnfant, transactionId),
                "Voir mes paiements",
                "#"
        );

        return envoyerEmailHtml(to, sujet, contenuHtml, null);
    }

    /**
     * Envoie un email de rappel de paiement
     */
    public boolean envoyerRappelPaiement(String to, String nomParrain, String nomEnfant, double montant) {
        String sujet = "Rappel de paiement - " + appName;
        String contenuHtml = genererTemplateHtml(
                "Rappel de paiement",
                "Cher(e) " + nomParrain + ",",
                String.format("Votre contribution mensuelle de %.0f FCFA pour %s est due.<br><br>Merci de procéder au paiement dans les plus brefs délais.",
                        montant, nomEnfant),
                "Effectuer le paiement",
                "#"
        );

        return envoyerEmailHtml(to, sujet, contenuHtml, null);
    }

    /**
     * Envoie un email de validation d'organisation
     */
    public boolean envoyerValidationOrganisation(String to, String nomOrganisation, boolean valide, String commentaire) {
        String sujet = valide ? "Inscription validée - " + appName : "Inscription en attente - " + appName;
        String titre = valide ? "Félicitations !" : "Inscription en attente";
        String message = valide
                ? "Votre inscription a été validée avec succès. Vous pouvez maintenant accéder à toutes les fonctionnalités."
                : "Votre inscription nécessite des corrections: " + commentaire;

        String contenuHtml = genererTemplateHtml(titre, nomOrganisation, message, "Accéder au tableau de bord", "#");

        return envoyerEmailHtml(to, sujet, contenuHtml, null);
    }

    /**
     * Génère un template HTML pour les emails
     */
    private String genererTemplateHtml(String titre, String salutation, String message, String texteBouton, String lienBouton) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9fafb; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>%s</h2>
                        <p>%s</p>
                        <a href="%s" class="button">%s</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. Tous droits réservés.</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, titre, appName, salutation, message, lienBouton, texteBouton, appName);
    }

    /**
     * Record pour les pièces jointes
     */
    public record PieceJointe(String nom, byte[] contenu, String typeContenu) {}

    /**
     * Envoie un email simple
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@eduka.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            javaMailSender.send(message);
            log.info("Email envoyé avec succès à: {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email de bienvenue
     */
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Bienvenue sur Eduka !";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Bienvenue sur la plateforme Eduka !\n\n" +
                        "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter " +
                        "et commencer à utiliser nos services.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Eduka",
                name
        );

        sendSimpleMessage(to, subject, message);
    }

    /**
     * Envoie un email de notification de changement de mot de passe
     */
    public void sendPasswordChangeNotification(String to) {
        String subject = "Mot de passe modifié - Eduka";
        String message = "Bonjour,\n\n" +
                "Votre mot de passe Eduka a été modifié avec succès.\n\n" +
                "Si vous n'êtes pas à l'origine de cette modification, " +
                "contactez immédiatement notre support.\n\n" +
                "Cordialement,\n" +
                "L'équipe Eduka";

        sendSimpleMessage(to, subject, message);
    }
}
