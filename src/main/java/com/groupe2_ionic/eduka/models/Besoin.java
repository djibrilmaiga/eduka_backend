package com.groupe2_ionic.eduka.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_besoin")
    private int id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal montant;

    // Un besoin concerne un seul enfant.
    @ManyToOne
    @JoinColumn(name = "id_enfant")
    private Enfant enfant;
}
