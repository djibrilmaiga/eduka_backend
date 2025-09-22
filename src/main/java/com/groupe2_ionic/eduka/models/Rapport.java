package com.groupe2_ionic.eduka.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rapport")
    private int id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String typeRapport;

    @Column(nullable = false)
    private String periode;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false)
    private LocalDate date;

    // Un rapport concerne un seul enfant.
    @ManyToOne
    @JoinColumn(name = "id_enfant")
    private Enfant enfant;

    // Un rapport n'est créé que par une seule organisation.
    @ManyToOne
    @JoinColumn(name = "id_organisation")
    private Organisation organisation;

    // Un rapport comprend plusieurs documents.
    @OneToMany(mappedBy = "rapport")
    private Set<Document> documents = new HashSet<>();
}
