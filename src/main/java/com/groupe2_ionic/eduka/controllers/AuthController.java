package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.auth.*;
import com.groupe2_ionic.eduka.services.AuthenticationService;
import com.groupe2_ionic.eduka.services.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur d'authentification pour les utilisateurs (Parrain, Organisation, Admin)
 * Gère la connexion, l'inscription et la gestion des tokens
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API d'authentification pour les utilisateurs")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur avec email/téléphone et mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
            @ApiResponse(responseCode = "400", description = "Données de requête invalides")
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Tentative de connexion pour: {}", request.getIdentifier());

        try {
            AuthResponseDto response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la connexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    /**
     * Rafraîchissement du token d'accès
     */
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", description = "Génère un nouveau token d'accès à partir du refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token rafraîchi avec succès"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalide"),
            @ApiResponse(responseCode = "400", description = "Données de requête invalides")
    })
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Tentative de rafraîchissement de token");

        try {
            AuthResponseDto response = authenticationService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    /**
     * Déconnexion d'un utilisateur
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Révoque les tokens de l'utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie"),
            @ApiResponse(responseCode = "400", description = "Données de requête invalides")
    })
    public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshTokenRequestDto request) {
        log.info("Tentative de déconnexion");

        try {
            authenticationService.logout(request.getRefreshToken());
            return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("message", "Déconnexion effectuée"));
        }
    }

    /**
     * Demande de réinitialisation de mot de passe
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Envoie un email de réinitialisation de mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email de réinitialisation envoyé"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé"),
            @ApiResponse(responseCode = "429", description = "Trop de tentatives")
    })
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        log.info("Demande de réinitialisation de mot de passe pour: {}", request.getEmail());

        try {
            passwordService.initiateForgotPassword(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Si cette adresse email existe, un lien de réinitialisation a été envoyé"
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage());
            // Toujours retourner le même message pour éviter l'énumération d'emails
            return ResponseEntity.ok(Map.of(
                    "message", "Si cette adresse email existe, un lien de réinitialisation a été envoyé"
            ));
        }
    }

    /**
     * Réinitialisation du mot de passe avec token
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Réinitialise le mot de passe avec un token valide")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
            @ApiResponse(responseCode = "400", description = "Token invalide ou mots de passe non conformes"),
            @ApiResponse(responseCode = "401", description = "Token expiré")
    })
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        log.info("Tentative de réinitialisation de mot de passe");

        try {
            passwordService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Changement de mot de passe pour utilisateur connecté
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('PARRAIN', 'ORGANISATION', 'ADMIN')")
    @Operation(summary = "Changer le mot de passe", description = "Change le mot de passe de l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe changé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Mot de passe actuel incorrect")
    })
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        log.info("Tentative de changement de mot de passe");

        try {
            passwordService.changePassword(request);
            return ResponseEntity.ok(Map.of("message", "Mot de passe changé avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validation d'un token de réinitialisation
     */
    @GetMapping("/validate-reset-token")
    @Operation(summary = "Valider un token de réinitialisation", description = "Vérifie si un token de réinitialisation est valide")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token valide"),
            @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    })
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        log.info("Validation d'un token de réinitialisation");

        boolean isValid = passwordService.isValidResetToken(token);

        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "Token valide" : "Token invalide ou expiré"
        ));
    }
}
