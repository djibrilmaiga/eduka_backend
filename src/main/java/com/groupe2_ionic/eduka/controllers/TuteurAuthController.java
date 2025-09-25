package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.auth.OtpRequestDto;
import com.groupe2_ionic.eduka.dto.auth.OtpVerificationDto;
import com.groupe2_ionic.eduka.dto.auth.TuteurAuthResponseDto;
import com.groupe2_ionic.eduka.services.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur d'authentification pour les tuteurs
 * Gère l'authentification par OTP (One-Time Password)
 */
@Slf4j
@RestController
@RequestMapping("/api/tuteur-auth")
@RequiredArgsConstructor
@Tag(name = "Tuteur Authentication", description = "API d'authentification OTP pour les tuteurs")
public class TuteurAuthController {

    private final OtpService otpService;

    /**
     * Génération et envoi d'un code OTP
     */
    @PostMapping("/request-otp")
    @Operation(summary = "Demander un code OTP", description = "Génère et envoie un code OTP au tuteur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code OTP envoyé avec succès"),
            @ApiResponse(responseCode = "404", description = "Tuteur non trouvé"),
            @ApiResponse(responseCode = "429", description = "Trop de tentatives"),
            @ApiResponse(responseCode = "400", description = "Données de requête invalides")
    })
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        log.info("Demande d'OTP pour le tuteur: {}", request.getTelephone());

        try {
            otpService.generateAndSendOtp(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Code OTP envoyé avec succès",
                    "telephone", request.getTelephone()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la génération d'OTP: {}", e.getMessage());

            if (e.getMessage().contains("Trop de tentatives")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("non trouvé")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Tuteur non trouvé avec ce numéro de téléphone"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur lors de l'envoi du code OTP"));
            }
        }
    }

    /**
     * Vérification du code OTP et authentification
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Vérifier le code OTP", description = "Vérifie le code OTP et authentifie le tuteur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie"),
            @ApiResponse(responseCode = "401", description = "Code OTP invalide"),
            @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
            @ApiResponse(responseCode = "404", description = "Tuteur non trouvé")
    })
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationDto request) {
        log.info("Vérification d'OTP pour le tuteur: {}", request.getTelephone());

        try {
            TuteurAuthResponseDto response = otpService.verifyOtpAndAuthenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification d'OTP: {}", e.getMessage());

            if (e.getMessage().contains("non trouvé")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Tuteur non trouvé"));
            } else if (e.getMessage().contains("incorrect") ||
                    e.getMessage().contains("invalide") ||
                    e.getMessage().contains("expiré") ||
                    e.getMessage().contains("tentatives")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur lors de la vérification du code OTP"));
            }
        }
    }

    /**
     * Endpoint de test pour vérifier le statut du service OTP
     */
    @GetMapping("/status")
    @Operation(summary = "Statut du service OTP", description = "Vérifie le statut du service d'authentification OTP")
    @ApiResponse(responseCode = "200", description = "Service opérationnel")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            var stats = otpService.getOtpStats();
            return ResponseEntity.ok(Map.of(
                    "status", "operational",
                    "service", "tuteur-otp-auth",
                    "stats", Map.of(
                            "totalOtps", stats.total(),
                            "activeOtps", stats.active(),
                            "expiredOtps", stats.expired()
                    )
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Erreur interne du service"
                    ));
        }
    }
}
