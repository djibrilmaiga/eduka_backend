package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour l'historique des paiements du parrain
 * API temps réel (pas de cache)
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaiementHistoriqueDto {

    private int id;
    private MethodePaiement methodePaiement;
    private BigDecimal montant;
    private StatutPaiement statut;
    private LocalDate datePaiement;
    private String transactionId;

    // Informations sur l'enfant
    private String enfantNom;
    private String enfantPrenom;
    private String enfantPhoto;

    // Type de paiement
    private String typeParrainage; // NOUVEAU_PARRAINAGE, DON_COMPLEMENTAIRE
    private String descriptionPaiement;

    // Informations contextuelles
    private String enfantNomComplet;
    private String enfantPhotoProfil;
    private int parrainageId;

    // Informations organisation (pour paiements en espèces)
    private String organisationNom;
    private String organisationContact;

    // Détails du paiement
    private String numeroTelephone; // Pour Mobile Money
    private String codeConfirmation;
    private String messageErreur;
    private String recuUrl; // URL du reçu numérique

    // Informations calculées
    private String tempsEcoule; // "il y a 3 jours"
    private boolean peutEtreRembourse;
    private boolean recuDisponible;
}
