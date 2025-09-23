package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaiementRequestDto {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methodePaiement;

    @NotNull(message = "L'ID du parrainage est obligatoire")
    private Integer parrainageId;

    // Champs spécifiques selon la méthode de paiement
    private String numeroTelephone; // Pour Mobile Money
    private String email; // Pour PayPal
    private String tokenCarte; // Pour Stripe
    private String returnUrl; // URL de retour après paiement
    private String cancelUrl; // URL d'annulation
}
