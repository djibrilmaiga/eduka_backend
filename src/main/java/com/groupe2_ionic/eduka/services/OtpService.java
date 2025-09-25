package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.security.properties.JwtProperties;
import com.groupe2_ionic.eduka.security.properties.OtpProperties;
import com.groupe2_ionic.eduka.dto.auth.OtpRequestDto;
import com.groupe2_ionic.eduka.dto.auth.OtpVerificationDto;
import com.groupe2_ionic.eduka.dto.auth.TuteurAuthResponseDto;
import com.groupe2_ionic.eduka.models.OtpCode;
import com.groupe2_ionic.eduka.models.Tuteur;
import com.groupe2_ionic.eduka.repository.OtpCodeRepository;
import com.groupe2_ionic.eduka.repository.TuteurRepository;
import com.groupe2_ionic.eduka.security.JwtUtil;
import com.groupe2_ionic.eduka.services.utilitaires.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

/**
 * Service de gestion des codes OTP
 * Gère la génération, l'envoi et la vérification des codes OTP pour les tuteurs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final TuteurRepository tuteurRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final OtpProperties otpProperties;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Génère et envoie un code OTP pour un tuteur
     */
    @Transactional
    public void generateAndSendOtp(OtpRequestDto request) {
        log.info("Génération d'OTP pour le téléphone: {}", request.getTelephone());

        // Vérifier si le tuteur existe
        Tuteur tuteur = tuteurRepository.findByTelephone(request.getTelephone())
                .orElseThrow(() -> new BadCredentialsException("Aucun tuteur trouvé avec ce numéro de téléphone"));

        // Vérifier le nombre de tentatives récentes (protection contre le spam)
        long recentAttempts = otpCodeRepository.countRecentAttemptsByTelephone(
                request.getTelephone(),
                LocalDateTime.now().minusHours(1)
        );

        if (recentAttempts >= 5) {
            throw new BadCredentialsException("Trop de tentatives. Veuillez réessayer dans une heure.");
        }

        // Marquer tous les anciens codes comme utilisés
        otpCodeRepository.markAllAsUsedByTelephone(request.getTelephone());

        // Générer un nouveau code OTP
        String otpCode = generateOtpCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(otpProperties.getExpirationMinutes());

        // Sauvegarder le code OTP
        OtpCode otp = new OtpCode();
        otp.setTelephone(request.getTelephone());
        otp.setCode(otpCode);
        otp.setExpiryDate(expiryDate);
        otp.setUsed(false);
        otp.setAttempts(0);

        otpCodeRepository.save(otp);

        // Envoyer le code par SMS/Email (simulation par email pour le moment)
        try {
            sendOtpNotification(tuteur, otpCode);
            log.info("Code OTP généré et envoyé avec succès pour: {}", request.getTelephone());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'OTP: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi du code OTP");
        }
    }

    /**
     * Vérifie un code OTP et authentifie le tuteur
     */
    @Transactional
    public TuteurAuthResponseDto verifyOtpAndAuthenticate(OtpVerificationDto request) {
        log.info("Vérification d'OTP pour le téléphone: {}", request.getTelephone());

        // Récupérer le tuteur
        Tuteur tuteur = tuteurRepository.findByTelephone(request.getTelephone())
                .orElseThrow(() -> new BadCredentialsException("Tuteur non trouvé"));

        // Récupérer le code OTP le plus récent et valide
        OtpCode otpCode = otpCodeRepository.findLatestValidOtpByTelephone(
                request.getTelephone(),
                LocalDateTime.now()
        ).orElseThrow(() -> new BadCredentialsException("Code OTP expiré ou invalide"));

        // Vérifier le nombre de tentatives
        if (otpCode.getAttempts() >= otpProperties.getMaxAttempts()) {
            otpCode.setUsed(true);
            otpCodeRepository.save(otpCode);
            throw new BadCredentialsException("Nombre maximum de tentatives atteint");
        }

        // Incrémenter le nombre de tentatives
        otpCode.setAttempts(otpCode.getAttempts() + 1);
        otpCodeRepository.save(otpCode);

        // Vérifier le code
        if (!otpCode.getCode().equals(request.getOtpCode())) {
            throw new BadCredentialsException("Code OTP incorrect");
        }

        // Marquer le code comme utilisé
        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);

        // Générer le token JWT pour le tuteur
        UserDetails userDetails = User.builder()
                .username(tuteur.getTelephone())
                .password("") // Pas de mot de passe pour les tuteurs
                .authorities(Collections.singletonList(() -> "ROLE_TUTEUR"))
                .build();

        String accessToken = jwtUtil.generateAccessToken(userDetails, tuteur.getId(), "TUTEUR");

        log.info("Authentification OTP réussie pour le tuteur: {}", tuteur.getTelephone());

        return TuteurAuthResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessToken().getExpiration() / 1000)
                .tuteur(TuteurAuthResponseDto.TuteurInfoDto.builder()
                        .id(tuteur.getId())
                        .nom(tuteur.getNom())
                        .prenom(tuteur.getPrenom())
                        .telephone(tuteur.getTelephone())
                        .build())
                .build();
    }

    /**
     * Génère un code OTP aléatoire
     */
    private String generateOtpCode() {
        int code = secureRandom.nextInt(900000) + 100000; // Génère un nombre entre 100000 et 999999
        return String.valueOf(code);
    }

    /**
     * Envoie le code OTP par notification (email pour simulation)
     */
    private void sendOtpNotification(Tuteur tuteur, String otpCode) {
        String subject = "Code de vérification Eduka";
        String message = String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre code de vérification Eduka est : %s\n\n" +
                        "Ce code expire dans %d minutes.\n\n" +
                        "Si vous n'avez pas demandé ce code, ignorez ce message.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Eduka",
                tuteur.getPrenom(),
                tuteur.getNom(),
                otpCode,
                otpProperties.getExpirationMinutes()
        );

        // Pour le moment, on simule l'envoi par email
        // Dans un vrai projet, on utiliserait un service SMS
        emailService.envoyerEmail(
                tuteur.getTelephone() + "@sms-simulation.com", // Email de simulation
                subject,
                message
        );
    }

    /**
     * Nettoyage automatique des codes OTP expirés (exécuté toutes les heures)
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("Démarrage du nettoyage automatique des codes OTP expirés");

        try {
            otpCodeRepository.deleteExpiredOtps(LocalDateTime.now());
            log.info("Nettoyage des codes OTP expirés terminé");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des codes OTP expirés: {}", e.getMessage());
        }
    }

    /**
     * Statistiques des codes OTP
     */
    public OtpStats getOtpStats() {
        long totalOtps = otpCodeRepository.count();
        long activeOtps = otpCodeRepository.findAll().stream()
                .filter(otp -> !otp.getUsed() &&
                        otp.getExpiryDate().isAfter(LocalDateTime.now()))
                .count();

        return new OtpStats(totalOtps, activeOtps, totalOtps - activeOtps);
    }

    /**
     * Classe pour les statistiques des codes OTP
     */
    public record OtpStats(long total, long active, long expired) {}
}
