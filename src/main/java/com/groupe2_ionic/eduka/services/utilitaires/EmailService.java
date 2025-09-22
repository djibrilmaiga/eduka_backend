package com.groupe2_ionic.eduka.services.utilitaires;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String expediteur;

    /**
     * Envoie un email simple.
     *
     * @param to      L'adresse email du destinataire.
     * @param sujet   Le sujet de l'email.
     * @param contenu Le contenu de l'email.
     * @return Un message indiquant le succès ou l'échec de l'envoi.
     */
    public String envoyerEmail(String to, String sujet, String contenu){
        try {
            // Création du message email
            SimpleMailMessage message = new SimpleMailMessage();
            // Configuration des champs de l'email
            message.setFrom(expediteur); // L'expéditeur est défini à partir de la propriété spring.mail.username
            message.setTo(to); // Le destinataire est passé en paramètre
            message.setSubject(sujet); // Le sujet de l'email est passé en paramètre
            message.setText(contenu); // Le contenu de l'email est passé en paramètre
            // Envoi de l'email
            javaMailSender.send(message);
            // Retourne un message de succès
            return "Message envoyé avec succès !";
        } catch (Exception e) {
            // En cas d'erreur lors de l'envoi, retourne un message d'erreur
            return "Erreur lors de l'envoie de l'email !";
        }
    }
}
