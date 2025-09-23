package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaiementResponseDto {
    private int id;
    private BigDecimal montant;
    private MethodePaiement methodePaiement;
    private StatutPaiement statut;
    private LocalDate datePaiement;
    private Integer parrainId;
    private Integer enfantId;
    private Integer organisationId;

    private String transactionId;
    private String paymentUrl;
    private String codeConfirmation;
    private String messageErreur;

    // Legacy fields for backward compatibility
    private String parrainNom;
    private String enfantNom;
    private String organisationNom;
    private String referenceTransaction;

    // Constructor for backward compatibility
    public PaiementResponseDto(int id, MethodePaiement methode, BigDecimal montant, StatutPaiement statut,
                               LocalDate datePaiement, String parrainNom, String enfantNom,
                               String organisationNom, String referenceTransaction) {
        this.id = id;
        this.methodePaiement = methode;
        this.montant = montant;
        this.statut = statut;
        this.datePaiement = datePaiement;
        this.parrainNom = parrainNom;
        this.enfantNom = enfantNom;
        this.organisationNom = organisationNom;
        this.referenceTransaction = referenceTransaction;
    }
}
