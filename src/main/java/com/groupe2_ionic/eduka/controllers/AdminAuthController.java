package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur d'administration pour l'authentification
 * Fournit des endpoints pour la gestion des tokens et statistiques (Admin uniquement)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Authentication", description = "API d'administration pour l'authentification (Admin uniquement)")
public class AdminAuthController {

    private final RefreshTokenService refreshTokenService;

    /**
     * Statistiques des tokens de rafraîchissement
     */
    @GetMapping("/token-stats")
    @Operation(summary = "Statistiques des tokens", description = "Récupère les statistiques des refresh tokens")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès")
    public ResponseEntity<Map<String, Object>> getTokenStats() {
        log.info("Récupération des statistiques des tokens par un admin");

        try {
            var stats = refreshTokenService.getTokenStats();
            return ResponseEntity.ok(Map.of(
                    "refreshTokens", Map.of(
                            "total", stats.total(),
                            "active", stats.active(),
                            "expired", stats.expired()
                    )
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération des statistiques"));
        }
    }

    /**
     * Révocation de tous les tokens d'un utilisateur
     */
    @PostMapping("/revoke-user-tokens/{userId}")
    @Operation(summary = "Révoquer les tokens d'un utilisateur", description = "Révoque tous les refresh tokens d'un utilisateur spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens révoqués avec succès"),
            @ApiResponse(responseCode = "400", description = "ID utilisateur invalide")
    })
    public ResponseEntity<Map<String, String>> revokeUserTokens(@PathVariable Integer userId) {
        log.info("Révocation des tokens pour l'utilisateur {} par un admin", userId);

        try {
            refreshTokenService.revokeAllUserTokens(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Tous les tokens de l'utilisateur ont été révoqués",
                    "userId", userId.toString()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la révocation des tokens: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur lors de la révocation des tokens"));
        }
    }

    /**
     * Nettoyage manuel des tokens expirés
     */
    @PostMapping("/cleanup-expired-tokens")
    @Operation(summary = "Nettoyer les tokens expirés", description = "Lance le nettoyage manuel des tokens expirés")
    @ApiResponse(responseCode = "200", description = "Nettoyage effectué avec succès")
    public ResponseEntity<Map<String, String>> cleanupExpiredTokens() {
        log.info("Nettoyage manuel des tokens expirés par un admin");

        try {
            refreshTokenService.cleanupExpiredTokens();
            return ResponseEntity.ok(Map.of("message", "Nettoyage des tokens expirés effectué avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors du nettoyage des tokens expirés"));
        }
    }

    /**
     * Informations sur un token spécifique
     */
    @GetMapping("/token-info")
    @Operation(summary = "Informations sur un token", description = "Récupère les informations d'un refresh token spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informations récupérées"),
            @ApiResponse(responseCode = "404", description = "Token non trouvé")
    })
    public ResponseEntity<Map<String, Object>> getTokenInfo(@RequestParam String token) {
        log.info("Récupération d'informations sur un token par un admin");

        try {
            var refreshToken = refreshTokenService.findByToken(token);
            if (refreshToken.isPresent()) {
                var tokenData = refreshToken.get();
                return ResponseEntity.ok(Map.of(
                        "found", true,
                        "userId", tokenData.getUserId(),
                        "createdAt", tokenData.getCreatedAt(),
                        "expiryDate", tokenData.getExpiryDate(),
                        "revoked", tokenData.getRevoked()
                ));
            } else {
                return ResponseEntity.ok(Map.of("found", false));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des informations du token: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération des informations"));
        }
    }
}
