package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.security.properties.JwtProperties;
import com.groupe2_ionic.eduka.dto.auth.*;
import com.groupe2_ionic.eduka.models.RefreshToken;
import com.groupe2_ionic.eduka.models.Utilisateur;
import com.groupe2_ionic.eduka.repository.RefreshTokenRepository;
import com.groupe2_ionic.eduka.repository.UtilisateurRepository;
import com.groupe2_ionic.eduka.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service d'authentification
 * Gère la connexion, l'inscription et le rafraîchissement des tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    /**
     * Authentifie un utilisateur et génère les tokens
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        log.info("Tentative de connexion pour: {}", request.getIdentifier());

        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentifier(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Récupérer l'utilisateur depuis la base de données
            Utilisateur utilisateur = utilisateurRepository.findByEmailOrTelephone(
                    request.getIdentifier(), request.getIdentifier()
            ).orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

            // Révoquer les anciens refresh tokens
            refreshTokenRepository.revokeAllTokensByUserId(utilisateur.getId());

            // Générer les nouveaux tokens
            String accessToken = jwtUtil.generateAccessToken(
                    userDetails, utilisateur.getId(), utilisateur.getRole().name()
            );
            String refreshToken = jwtUtil.generateRefreshToken(userDetails, utilisateur.getId());

            // Sauvegarder le refresh token
            saveRefreshToken(refreshToken, utilisateur.getId());

            log.info("Connexion réussie pour l'utilisateur: {}", utilisateur.getEmail());

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtProperties.getAccessToken().getExpiration() / 1000)
                    .user(AuthResponseDto.UserInfoDto.builder()
                            .id(utilisateur.getId())
                            .email(utilisateur.getEmail())
                            .telephone(utilisateur.getTelephone())
                            .role(utilisateur.getRole())
                            .actif(utilisateur.getActif())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la connexion pour {}: {}", request.getIdentifier(), e.getMessage());
            throw new BadCredentialsException("Identifiants invalides");
        }
    }

    /**
     * Rafraîchit le token d'accès
     */
    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        log.info("Tentative de rafraîchissement de token");

        try {
            // Valider le refresh token
            if (!jwtUtil.validateRefreshToken(request.getRefreshToken())) {
                throw new BadCredentialsException("Token de rafraîchissement invalide");
            }

            // Vérifier que le token existe en base et n'est pas révoqué
            RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new BadCredentialsException("Token de rafraîchissement non trouvé"));

            if (storedToken.getRevoked() || storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new BadCredentialsException("Token de rafraîchissement expiré ou révoqué");
            }

            // Récupérer l'utilisateur
            Integer userId = jwtUtil.extractUserId(request.getRefreshToken());
            Utilisateur utilisateur = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

            // Créer UserDetails
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(utilisateur.getEmail())
                    .password(utilisateur.getPassword())
                    .authorities("ROLE_" + utilisateur.getRole().name())
                    .build();

            // Générer un nouveau token d'accès
            String newAccessToken = jwtUtil.generateAccessToken(
                    userDetails, utilisateur.getId(), utilisateur.getRole().name()
            );

            log.info("Token rafraîchi avec succès pour l'utilisateur: {}", utilisateur.getEmail());

            return AuthResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken()) // Garder le même refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtProperties.getAccessToken().getExpiration() / 1000)
                    .user(AuthResponseDto.UserInfoDto.builder()
                            .id(utilisateur.getId())
                            .email(utilisateur.getEmail())
                            .telephone(utilisateur.getTelephone())
                            .role(utilisateur.getRole())
                            .actif(utilisateur.getActif())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            throw new BadCredentialsException("Impossible de rafraîchir le token");
        }
    }

    /**
     * Déconnecte un utilisateur en révoquant ses tokens
     */
    @Transactional
    public void logout(String refreshToken) {
        log.info("Tentative de déconnexion");

        try {
            if (refreshToken != null) {
                Integer userId = jwtUtil.extractUserId(refreshToken);
                if (userId != null) {
                    refreshTokenRepository.revokeAllTokensByUserId(userId);
                    log.info("Déconnexion réussie pour l'utilisateur ID: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
        }
    }

    /**
     * Sauvegarde un refresh token en base de données
     */
    private void saveRefreshToken(String token, Integer userId) {
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshToken().getExpiration() / 1000);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Nettoie les tokens expirés (à appeler périodiquement)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Nettoyage des tokens expirés");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
