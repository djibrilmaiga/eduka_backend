package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.PaiementRequestDto;
import com.groupe2_ionic.eduka.dto.PaiementResponseDto;
import com.groupe2_ionic.eduka.dto.PaiementEspeceDto;
import com.groupe2_ionic.eduka.dto.PaiementHistoriqueDto;
import com.groupe2_ionic.eduka.services.PaiementService;
import com.groupe2_ionic.eduka.services.payment.PayPalPaymentService;
import com.groupe2_ionic.eduka.services.payment.StripePaymentService;
import com.groupe2_ionic.eduka.services.payment.WavePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "APIs pour la gestion des paiements")
public class PaiementController {

    private final PaiementService paymentService;
    private final StripePaymentService stripePaymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final WavePaymentService wavePaymentService;

    @PostMapping("/initier")
    @Operation(summary = "Initier un paiement",
            description = "Initie un paiement selon la méthode choisie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paiement initié avec succès"),
            @ApiResponse(responseCode = "400", description = "Données de paiement invalides"),
            @ApiResponse(responseCode = "404", description = "Parrainage non trouvé")
    })
    public ResponseEntity<PaiementResponseDto> initierPaiement(@Valid @RequestBody PaiementRequestDto requestDto) {
        try {
            PaiementResponseDto response = paymentService.initierPaiement(requestDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/stripe/webhook")
    @Operation(summary = "Webhook Stripe", description = "Traite les événements webhook de Stripe")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        try {
            stripePaymentService.traiterWebhook(payload, signature);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur webhook");
        }
    }

    @PostMapping("/paypal/capture/{orderId}")
    @Operation(summary = "Capturer paiement PayPal",
            description = "Capture un paiement PayPal après approbation")
    public ResponseEntity<String> capturerPayPal(
            @Parameter(description = "ID de la commande PayPal") @PathVariable String orderId) {

        try {
            payPalPaymentService.capturerPaiement(orderId);
            return ResponseEntity.ok("Paiement capturé");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur capture");
        }
    }

    @PostMapping("/wave/callback")
    @Operation(summary = "Callback Wave", description = "Traite les callbacks de Wave")
    public ResponseEntity<String> waveCallback(
            @RequestParam String transactionId,
            @RequestParam String status,
            @RequestParam(required = false) String confirmationCode) {

        try {
            wavePaymentService.traiterCallback(transactionId, status, confirmationCode);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur callback");
        }
    }

    @PostMapping("/mobile-money/confirm")
    @Operation(summary = "Confirmer paiement Mobile Money",
            description = "Confirme un paiement Mobile Money avec le code de confirmation")
    public ResponseEntity<String> confirmerMobileMoney(
            @RequestParam String transactionId,
            @RequestParam String codeConfirmation) {

        try {
            paymentService.confirmerPaiement(transactionId,
                    com.groupe2_ionic.eduka.models.enums.StatutPaiement.REUSSI,
                    codeConfirmation);
            return ResponseEntity.ok("Paiement confirmé");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur confirmation");
        }
    }

    @GetMapping("/historique/{parrainId}")
    @Operation(summary = "Historique des paiements d'un parrain",
            description = "Récupère l'historique paginé des paiements d'un parrain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Parrain non trouvé")
    })
    public ResponseEntity<Page<PaiementHistoriqueDto>> getHistoriquePaiements(
            @Parameter(description = "ID du parrain") @PathVariable Integer parrainId,
            Pageable pageable) {

        try {
            Page<PaiementHistoriqueDto> historique = paymentService.getHistoriquePaiements(parrainId, pageable);
            return ResponseEntity.ok(historique);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historique/organisation/{organisationId}")
    @Operation(summary = "Historique des paiements d'une organisation",
            description = "Récupère l'historique des paiements enregistrés par une organisation")
    public ResponseEntity<Page<PaiementHistoriqueDto>> getHistoriqueOrganisation(
            @Parameter(description = "ID de l'organisation") @PathVariable Integer organisationId,
            Pageable pageable) {

        try {
            Page<PaiementHistoriqueDto> historique = paymentService.getHistoriqueOrganisation(organisationId, pageable);
            return ResponseEntity.ok(historique);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/espece/enregistrer")
    @Operation(summary = "Enregistrer un paiement en espèces",
            description = "Permet à une organisation d'enregistrer un paiement reçu en espèces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paiement en espèces enregistré avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Seules les organisations peuvent enregistrer des paiements en espèces")
    })
    public ResponseEntity<PaiementResponseDto> enregistrerPaiementEspece(
            @Valid @RequestBody PaiementEspeceDto paiementEspeceDto) {

        try {
            PaiementResponseDto response = paymentService.enregistrerPaiementEspece(paiementEspeceDto);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistiques/{parrainId}")
    @Operation(summary = "Statistiques de paiement d'un parrain",
            description = "Récupère les statistiques de paiement d'un parrain")
    public ResponseEntity<Object> getStatistiquesPaiements(
            @Parameter(description = "ID du parrain") @PathVariable Integer parrainId) {

        try {
            Object statistiques = paymentService.getStatistiquesPaiements(parrainId);
            return ResponseEntity.ok(statistiques);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
