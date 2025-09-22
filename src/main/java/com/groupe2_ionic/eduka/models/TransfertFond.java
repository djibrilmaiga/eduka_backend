package com.groupe2_ionic.eduka.models;

import com.groupe2_ionic.eduka.models.enums.MotifTransfert;
import com.groupe2_ionic.eduka.models.enums.StatutTransfert;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TransfertFond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transfert")
    private int id;

    @Enumerated(EnumType.STRING)
    private MotifTransfert motif;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private StatutTransfert statut;

    @Column(nullable = false)
    private LocalDate dateDemande;

    private LocalDate dateTraitement;

    private String commentaireValidation; // commentaire du parrain (optionnel)

    // Un transfert est débité chez un enfant.
    @ManyToOne
    @JoinColumn(name = "id_enfant_source")
    private Enfant enfantSource;

    // Un transfert est crédité chez un enfant.
    // Enfant cible (nullable si retrait ou transfert sortant)
    @ManyToOne
    @JoinColumn(name = "id_enfant_cible")
    private Enfant enfantCible;

    // Une demande de transfert est effectué par une organisation.
    @ManyToOne
    @JoinColumn(name = "id_organisation")
    private Organisation organisation;

    // Une demande de transfert est validé par un parrain.
    @ManyToOne
    @JoinColumn(name = "id_parrain")
    private Parrain parrain;
}
