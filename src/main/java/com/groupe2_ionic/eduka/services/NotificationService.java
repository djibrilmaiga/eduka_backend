package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.NotificationResponseDto;
import com.groupe2_ionic.eduka.models.Notification;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.Parrain;
import com.groupe2_ionic.eduka.models.Utilisateur;
import com.groupe2_ionic.eduka.repository.NotificationRepository;
import com.groupe2_ionic.eduka.repository.UtilisateurRepository;
import com.groupe2_ionic.eduka.services.utilitaires.EmailService;
import com.groupe2_ionic.eduka.services.utilitaires.SmsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    /**
     * Crée une notification pour un utilisateur et envoie un email.
     */
    public NotificationResponseDto createNotification(Utilisateur utilisateur, String sujet, String message) {
        // Vérification de l'existence de l'utilisateur
        Notification notification = new Notification();
        // Remplissage des champs de la notification
        notification.setDestinataire(utilisateur);
        notification.setSujet(sujet);
        notification.setMessage(message);
        notification.setDate(LocalDate.now());
        notification.setLu(false);

        // Enregistrement de la notification dans la base de données
        Notification savedNotification = notificationRepository.save(notification);

        // Envoie de la notification par email :
        System.out.println(
                emailService.envoyerEmail(utilisateur.getEmail(), sujet, message)
        );

        return mapToResponseDto(savedNotification);
    }

    /**
     * Envoie une notification complète (email + SMS + base de données)
     */
    public NotificationResponseDto envoyerNotification(Utilisateur utilisateur, String sujet, String message) {
        return envoyerNotification(utilisateur, sujet, message, true, true);
    }

    /**
     * Envoie une notification avec options personnalisées
     */
    public NotificationResponseDto envoyerNotification(Utilisateur utilisateur, String sujet, String message,
                                                       boolean envoyerEmail, boolean envoyerSms) {
        // Créer la notification en base
        Notification notification = new Notification();
        notification.setDestinataire(utilisateur);
        notification.setSujet(sujet);
        notification.setMessage(message);
        notification.setDate(LocalDate.now());
        notification.setLu(false);

        Notification savedNotification = notificationRepository.save(notification);

        // Envoyer par email si demandé
        if (envoyerEmail) {
            if (utilisateur instanceof Parrain) {
                Parrain parrain = (Parrain) utilisateur;
                emailService.envoyerEmailHtml(utilisateur.getEmail(), sujet, message, null);
            } else if (utilisateur instanceof Organisation) {
                Organisation org = (Organisation) utilisateur;
                emailService.envoyerEmailHtml(utilisateur.getEmail(), sujet, message, null);
            } else {
                emailService.envoyerEmail(utilisateur.getEmail(), sujet, message);
            }
        }

        // Envoyer par SMS si demandé et numéro disponible
        if (envoyerSms && utilisateur.getTelephone() != null) {
            smsService.envoyerSms(utilisateur.getTelephone(), message);
        }

        return mapToResponseDto(savedNotification);
    }

    /**
     * Envoie une notification de bienvenue
     */
    public void envoyerNotificationBienvenue(Utilisateur utilisateur) {
        String nom = "";
        String typeUtilisateur = "";

        if (utilisateur instanceof Parrain) {
            Parrain parrain = (Parrain) utilisateur;
            nom = parrain.getPrenom() + " " + parrain.getNom();
            typeUtilisateur = "parrain";
        } else if (utilisateur instanceof Organisation) {
            Organisation org = (Organisation) utilisateur;
            nom = org.getNom();
            typeUtilisateur = "organisation";
        }

        String sujet = "Bienvenue sur EduKa !";
        String message = String.format("Bienvenue %s ! Votre inscription en tant que %s a été confirmée.", nom, typeUtilisateur);

        // Envoyer email de bienvenue avec template HTML
        emailService.envoyerEmailBienvenue(utilisateur.getEmail(), nom, typeUtilisateur);

        // Créer notification en base
        envoyerNotification(utilisateur, sujet, message, false, true);
    }

    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<NotificationResponseDto> getNotificationsByUtilisateur(int utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        List<Notification> notifications = notificationRepository.findByDestinataireOrderByDateDesc(utilisateur);

        return notifications.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les notifications d'un utilisateur avec pagination
     */
    public Page<NotificationResponseDto> getNotificationsByUtilisateurWithPagination(int utilisateurId, int page, int size) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByDestinataireOrderByDateDesc(utilisateur, pageable);

        return notifications.map(this::mapToResponseDto);
    }

    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    public List<NotificationResponseDto> getUnreadNotifications(int utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        List<Notification> notifications = notificationRepository.findByDestinataireAndLuFalseOrderByDateDesc(utilisateur);

        return notifications.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Compte le nombre de notifications non lues d'un utilisateur
     */
    public long countUnreadNotifications(int utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        return notificationRepository.countByDestinataireAndLuFalse(utilisateur);
    }

    /**
     * Marque une notification comme lue
     */
    public NotificationResponseDto markAsRead(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));

        notification.setLu(true);
        Notification savedNotification = notificationRepository.save(notification);

        return mapToResponseDto(savedNotification);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Transactional
    public void markAllAsRead(int utilisateurId) {
        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId);
        }

        notificationRepository.markAllAsReadByDestinataireId(utilisateurId);
    }

    /**
     * Supprime une notification spécifique
     */
    public void deleteNotification(int notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Notification non trouvée avec l'ID: " + notificationId);
        }

        notificationRepository.deleteById(notificationId);
    }

    /**
     * Supprime toutes les notifications d'un utilisateur
     */
    @Transactional
    public void deleteAllNotifications(int utilisateurId) {
        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId);
        }

        notificationRepository.deleteAllByDestinataireId(utilisateurId);
    }

    /**
     * Mappe une entité Notification vers un DTO de réponse
     */
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        String nom, prenom;
        if (notification.getDestinataire() instanceof Parrain) {
            nom = ((Parrain) notification.getDestinataire()).getNom();
            prenom = ((Parrain) notification.getDestinataire()).getPrenom();
        } else if (notification.getDestinataire() instanceof Organisation) {
            nom = ((Organisation) notification.getDestinataire()).getNom();
            prenom = null;
        } else {
            nom = null;
            prenom = null;
        }
        return new NotificationResponseDto(
                notification.getId(),
                notification.getSujet(),
                notification.getMessage(),
                notification.getDate(),
                notification.getLu(),
                nom,
                prenom,
                notification.getDestinataire().getEmail()
        );
    }
}
