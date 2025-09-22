package com.groupe2_ionic.eduka.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Depense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_depense")
    private int id;

    @Column(nullable = false)
    private String typeDepense;

    @Column(columnDefinition = "TEXT")
    private String justificatif;

    @Column(nullable = false)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDate dateEnregistrement;

    // Une dépense est enregistré par une organisation.
    @ManyToOne
    @JoinColumn(name = "id_organisation")
    private Organisation organisation;

    // Une dépense concerne un seul enfant.
    @ManyToOne
    @JoinColumn(name = "id_enfant")
    private Enfant enfant;
}
