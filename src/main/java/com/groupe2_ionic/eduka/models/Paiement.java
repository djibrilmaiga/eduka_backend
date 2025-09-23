package com.groupe2_ionic.eduka.models;

import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private int id;

    @Enumerated(EnumType.STRING)
    private MethodePaiement methode;

    @Column(nullable = false)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut;

    @Column(nullable = false)
    private LocalDate datePaiement;

    private String transactionId; // ID de transaction externe (Stripe, PayPal, etc.)
    private String paymentIntentId; // ID d'intention de paiement (Stripe)
    private String numeroTelephone; // Numéro de téléphone pour Mobile Money
    private String codeConfirmation; // Code de confirmation pour Mobile Money
    private String messageErreur; // Message d'erreur en cas d'échec
    private String metadonnees; // Métadonnées JSON pour informations supplémentaires

    // Un paiement n'est effectué que par un seul parrain.
    @ManyToOne
    @JoinColumn(name = "id_parrain")
    private Parrain parrain;

    // Le paiement ne concerne qu'un seul parrainage.
    @ManyToOne
    @JoinColumn(name = "id_parrainage")
    private Parrainage parrainage;

    // Un paiement peut être enregistré par une organisation (paiement.type = ESPECE).
    @ManyToOne
    @JoinColumn(name = "id_organisation")
    private Organisation organisation;
}
