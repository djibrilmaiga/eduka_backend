package com.groupe2_ionic.eduka.models;

import com.groupe2_ionic.eduka.models.enums.Genre;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Enfant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enfant")
    private int id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private String niveauScolaire;

    @Column(columnDefinition = "TEXT")
    private String histoire;

    private String photoProfil;

    @Column(nullable = false)
    private Boolean statutParrainage;

    private BigDecimal solde;

    @Column(nullable = false)
    private Boolean consentementPedagogique = false;

    // Un enfant n'a qu'un seul tuteur.
    @OneToOne
    @JoinColumn(name = "id_tuteur")
    private Tuteur tuteur;

    // Un enfant se trouve dans une école.
    @OneToOne
    @JoinColumn(name = "id_ecole")
    private Ecole ecole;

    // Un enfant peut avoir plusieurs besoins.
    @OneToMany(mappedBy = "enfant")
    private Set<Besoin> besoins = new HashSet<>();

    // Un enfant peut avoir plusieurs rapports.
    @OneToMany(mappedBy = "enfant")
    private Set<Rapport> rapports = new HashSet<>();

    // Un enfant peut être lié à plusieurs parrainages.
    @OneToMany(mappedBy = "enfant")
    private Set<Parrainage> parrainages;

    // Un enfant est enregistré par une organisation.
    @ManyToOne
    @JoinColumn(name = "id_organisation")
    private Organisation organisation;

    // Méthode utilitaire pour calculer l’âge
    public int getAge() {
        if (dateNaissance == null) {
            return 0; // ou lever une exception
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }
}
