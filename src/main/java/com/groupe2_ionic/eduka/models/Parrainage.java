package com.groupe2_ionic.eduka.models;

import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Parrainage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parrainage")
    private int id;

    @Enumerated(EnumType.STRING)
    private StatutParrainage statut;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private BigDecimal montantTotal;

    private LocalDate dateFin;

    @Column(columnDefinition = "TEXT")
    private String motifFin;

    // Le parrainage ne concerne qu'un seul parrain.
    @ManyToOne
    @JoinColumn(name = "id_parrain")
    private Parrain parrain;

    // Le parrainage ne concerne qu'un seul enfant.
    @ManyToOne
    @JoinColumn(name = "id_enfant")
    private Enfant enfant;

    // La liste de tous les paiements effectués lors de ce parrainage
    @OneToMany(mappedBy = "parrainage")
    private Set<Paiement> paiements = new HashSet<>();
}
