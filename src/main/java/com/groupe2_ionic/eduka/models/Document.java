package com.groupe2_ionic.eduka.models;

import com.groupe2_ionic.eduka.models.enums.TypeDocument;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private int id;

    @Deprecated
    private String type;

    private String url;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocument typeDocument;

    // Un document peut être lié à un rapport.
    @ManyToOne
    @JoinColumn(name = "id_rapport")
    private Rapport rapport;

    // Un document peut être lié à une organisation.
    @ManyToOne
    @JoinColumn(name = "id_organisation")
    private Organisation organisation;
}
