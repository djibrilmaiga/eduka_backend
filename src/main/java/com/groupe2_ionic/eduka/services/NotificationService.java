package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.NotificationResponseDto;
import com.groupe2_ionic.eduka.models.*;
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
     * Notifie les parties concernées lors d'un nouveau parrainage
     */
    public void notifierNouveauParrainage(Parrainage parrainage) {
        if (parrainage == null || parrainage.getParrain() == null || parrainage.getEnfant() == null) {
            throw new IllegalArgumentException("Le parrainage, le parrain et l'enfant ne peuvent pas être null");
        }

        Parrain parrain = parrainage.getParrain();
        String nomEnfant = parrainage.getEnfant().getPrenom() + " " + parrainage.getEnfant().getNom();
        String nomParrain = parrain.getPrenom() + " " + parrain.getNom();

        // Notification au parrain
        String sujetParrain = "Nouveau parrainage confirmé";
        String messageParrain = String.format(
                "Félicitations %s ! Votre parrainage avec %s a été confirmé. " +
                        "Vous pouvez maintenant suivre l'évolution de votre filleul(e) depuis votre tableau de bord.",
                nomParrain, nomEnfant
        );

        envoyerNotification(parrain, sujetParrain, messageParrain, true, true);

        // Notification à l'organisation si elle existe
        if (parrainage.getEnfant().getOrganisation() != null) {
            Organisation organisation = parrainage.getEnfant().getOrganisation();
            String sujetOrg = "Nouveau parrainage établi";
            String messageOrg = String.format(
                    "Un nouveau parrainage a été établi entre %s et %s (%s). " +
                            "Montant total: %s. Vous pouvez maintenant commencer le suivi.",
                    nomParrain, nomEnfant, parrainage.getEnfant().getEcole(),
                    parrainage.getMontantTotal()
            );

            envoyerNotification(organisation, sujetOrg, messageOrg, true, false);
        }

        // Notification au tuteur si consentement accordé
       /* if (parrainage.getEnfant().getTuteur() != null
                // && parrainage.getEnfant().getTuteur().getConsentementPedagogique()
        ) {

            String sujetTuteur = "Nouveau parrainage pour votre enfant";
            String messageTuteur = String.format(
                    "Bonne nouvelle ! %s a trouvé un parrain/marraine : %s. " +
                            "Ce parrainage permettra de soutenir l'éducation de votre enfant.",
                    nomEnfant, nomParrain
            );

            envoyerNotification(parrainage.getEnfant().getTuteur(), sujetTuteur, messageTuteur, true, true);
        }*/
    }

    /**
     * Notifie un parrain lorsqu'une demande de transfert de fonds est initiée
     */
    public void notifierDemandeTransfertParent(TransfertFond transfert) {
        if (transfert.getParrain() != null) {
            String sujet = "Demande de transfert de fonds";
            String message = String.format("Une demande de transfert de %s FCFA a été initiée. Motif: %s",
                    transfert.getMontant(), transfert.getMotif());
            envoyerNotification(transfert.getParrain(), sujet, message, true, true);
        }
    }

    /**
     * Notifie les parrains lorsqu'un nouveau rapport est disponible pour leur filleul(e)
     */
    public void notifierNouveauRapport(Rapport rapport) {
        if (rapport.getEnfant() != null && rapport.getEnfant().getParrainages() != null) {
            rapport.getEnfant().getParrainages().forEach(parrainage -> {
                if (parrainage.getStatut() == com.groupe2_ionic.eduka.models.enums.StatutParrainage.ACTIF) {
                    String sujet = "Nouveau rapport disponible";
                    String message = String.format("Un nouveau rapport est disponible pour votre filleul(e) %s : %s",
                            rapport.getEnfant().getPrenom() + " " + rapport.getEnfant().getNom(),
                            rapport.getTitre());
                    envoyerNotification(parrainage.getParrain(), sujet, message, true, false);
                }
            });
        }
    }

    /**
     * Notifie les parrains lorsqu'une nouvelle dépense est enregistrée pour leur filleul(e)
     */
    public void notifierNouvelleDepense(Depense depense) {
        if (depense.getEnfant() != null && depense.getEnfant().getParrainages() != null) {
            depense.getEnfant().getParrainages().forEach(parrainage -> {
                if (parrainage.getStatut() == com.groupe2_ionic.eduka.models.enums.StatutParrainage.ACTIF) {
                    String sujet = "Nouvelle dépense enregistrée";
                    String message = String.format("Une nouvelle dépense a été enregistrée pour votre filleul(e) %s : %s (Montant: %s FCFA)",
                            depense.getEnfant().getPrenom() + " " + depense.getEnfant().getNom(),
                            depense.getTypeDepense(),
                            depense.getMontant());
                    envoyerNotification(parrainage.getParrain(), sujet, message, true, false);
                }
            });
        }
    }

    /**
     * Notifie les parrains lorsqu'un nouveau besoin est identifié pour leur filleul(e)
     */
    public void notifierNouveauBesoin(Besoin besoin) {
        if (besoin.getEnfant() != null && besoin.getEnfant().getParrainages() != null) {
            besoin.getEnfant().getParrainages().forEach(parrainage -> {
                if (parrainage.getStatut() == com.groupe2_ionic.eduka.models.enums.StatutParrainage.ACTIF) {
                    String sujet = "Nouveau besoin identifié";
                    String message = String.format("Un nouveau besoin a été identifié pour votre filleul(e) %s : %s (Montant: %s FCFA)",
                            besoin.getEnfant().getPrenom() + " " + besoin.getEnfant().getNom(),
                            besoin.getType(),
                            besoin.getMontant());
                    envoyerNotification(parrainage.getParrain(), sujet, message, true, true);
                }
            });
        }
    }

    /**
     * Notifie les parrains potentiels lorsqu'un nouvel enfant est disponible
     */
    public void notifierNouvelEnfantDisponible(Enfant enfant) {
        // Cette méthode pourrait notifier les parrains potentiels
        // Pour l'instant, on la laisse vide ou on peut implémenter une logique spécifique
    }

    /**
     * Notifie une organisation lors d'une nouvelle demande d'inscription
     */
    public void notifierNouvelleDemandeOrganisation(Organisation organisation) {
        String sujet = "Demande d'inscription reçue";
        String message = String.format("Votre demande d'inscription pour l'organisation %s a été reçue et est en cours de traitement.", organisation.getNom());
        envoyerNotification(organisation, sujet, message);
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
