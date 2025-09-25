package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.auth.ChangePasswordRequestDto;
import com.groupe2_ionic.eduka.dto.auth.ForgotPasswordRequestDto;
import com.groupe2_ionic.eduka.dto.auth.ResetPasswordRequestDto;
import com.groupe2_ionic.eduka.models.PasswordResetToken;
import com.groupe2_ionic.eduka.models.Utilisateur;
import com.groupe2_ionic.eduka.repository.PasswordResetTokenRepository;
import com.groupe2_ionic.eduka.repository.RefreshTokenRepository;
import com.groupe2_ionic.eduka.repository.UtilisateurRepository;
import com.groupe2_ionic.eduka.services.utilitaires.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service de gestion des mots de passe
 * Gère la réinitialisation et le changement des mots de passe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Initie le processus de réinitialisation de mot de passe
     */
    @Transactional
    public void initiateForgotPassword(ForgotPasswordRequestDto request) {
        log.info("Demande de réinitialisation de mot de passe pour: {}", request.getEmail());

        // Vérifier si l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Aucun compte trouvé avec cette adresse email"));

        // Vérifier le nombre de tentatives récentes (protection contre le spam)
        long recentAttempts = passwordResetTokenRepository.countRecentAttemptsByEmail(
                request.getEmail(),
                LocalDateTime.now().minusHours(1)
        );

        if (recentAttempts >= 3) {
            throw new BadCredentialsException("Trop de tentatives. Veuillez réessayer dans une heure.");
        }

        // Marquer tous les anciens tokens comme utilisés
        passwordResetTokenRepository.markAllAsUsedByUserId(utilisateur.getId());

        // Générer un nouveau token
        String resetToken = generateResetToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token valide 1 heure

        // Sauvegarder le token
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setUserId(utilisateur.getId());
        passwordResetToken.setEmail(utilisateur.getEmail());
        passwordResetToken.setExpiryDate(expiryDate);
        passwordResetToken.setUsed(false);

        passwordResetTokenRepository.save(passwordResetToken);

        // Envoyer l'email de réinitialisation
        try {
            sendPasswordResetEmail(utilisateur, resetToken);
            log.info("Email de réinitialisation envoyé avec succès pour: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }

    /**
     * Réinitialise le mot de passe avec un token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        log.info("Tentative de réinitialisation de mot de passe avec token");

        // Vérifier que les mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadCredentialsException("Les mots de passe ne correspondent pas");
        }

        // Valider le token
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidTokenByValue(
                request.getToken(),
                LocalDateTime.now()
        ).orElseThrow(() -> new BadCredentialsException("Token de réinitialisation invalide ou expiré"));

        // Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

        // Changer le mot de passe
        utilisateur.setPassword(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);

        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Révoquer tous les refresh tokens de l'utilisateur pour forcer une nouvelle connexion
        refreshTokenRepository.revokeAllTokensByUserId(utilisateur.getId());

        log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", utilisateur.getEmail());
    }

    /**
     * Change le mot de passe d'un utilisateur connecté
     */
    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {
        log.info("Tentative de changement de mot de passe");

        // Vérifier que les nouveaux mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadCredentialsException("Les nouveaux mots de passe ne correspondent pas");
        }

        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), utilisateur.getPassword())) {
            throw new BadCredentialsException("Mot de passe actuel incorrect");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(request.getNewPassword(), utilisateur.getPassword())) {
            throw new BadCredentialsException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Changer le mot de passe
        utilisateur.setPassword(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);

        // Révoquer tous les refresh tokens sauf celui de la session actuelle
        // (optionnel - on peut garder la session actuelle)
        refreshTokenRepository.revokeAllTokensByUserId(utilisateur.getId());

        log.info("Mot de passe changé avec succès pour l'utilisateur: {}", utilisateur.getEmail());
    }

    /**
     * Génère un token de réinitialisation sécurisé
     */
    private String generateResetToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Envoie l'email de réinitialisation de mot de passe
     */
    private void sendPasswordResetEmail(Utilisateur utilisateur, String resetToken) {
        String subject = "Réinitialisation de votre mot de passe Eduka";

        // Dans un vrai projet, on utiliserait un template HTML
        String resetUrl = "https://your-frontend-url.com/reset-password?token=" + resetToken;

        String message = String.format(
                "Bonjour,\n\n" +
                        "Vous avez demandé la réinitialisation de votre mot de passe pour votre compte Eduka.\n\n" +
                        "Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n" +
                        "%s\n\n" +
                        "Ce lien expire dans 1 heure.\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Eduka",
                resetUrl
        );

        emailService.sendSimpleMessage(utilisateur.getEmail(), subject, message);
    }

    /**
     * Valide un token de réinitialisation
     */
    public boolean isValidResetToken(String token) {
        return passwordResetTokenRepository.existsByTokenAndUsedFalseAndExpiryDateAfter(
                token,
                LocalDateTime.now()
        );
    }

    /**
     * Nettoyage automatique des tokens expirés (exécuté toutes les heures)
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Démarrage du nettoyage automatique des tokens de réinitialisation expirés");

        try {
            passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Nettoyage des tokens de réinitialisation expirés terminé");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens de réinitialisation expirés: {}", e.getMessage());
        }
    }

    /**
     * Statistiques des tokens de réinitialisation
     */
    public PasswordResetStats getPasswordResetStats() {
        long totalTokens = passwordResetTokenRepository.count();
        long activeTokens = passwordResetTokenRepository.findAll().stream()
                .filter(token -> !token.getUsed() &&
                        token.getExpiryDate().isAfter(LocalDateTime.now()))
                .count();

        return new PasswordResetStats(totalTokens, activeTokens, totalTokens - activeTokens);
    }

    /**
     * Classe pour les statistiques des tokens de réinitialisation
     */
    public record PasswordResetStats(long total, long active, long expired) {}
}
