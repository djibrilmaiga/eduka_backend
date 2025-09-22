package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaiementDto {
    
    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methode;
    
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1.0", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;
    
    @NotNull(message = "L'ID du parrainage est obligatoire")
    private Integer parrainageId;
    
    // Pour les paiements en espèces enregistrés par l'organisation
    private Integer organisationId;
    
    // Données spécifiques au paiement mobile money
    private String numeroTelephone;
    private String codeTransaction;
    
    // Données spécifiques au paiement par carte
    private String stripeTokenId;
}
