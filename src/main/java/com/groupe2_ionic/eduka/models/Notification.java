package com.groupe2_ionic.eduka.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private int id;

    @Column(nullable = false)
    private String sujet;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDate date;

    private Boolean lu = false;

    // Destinataire de la notification (ex: Parrain, Organisation, Admin…)
    @ManyToOne
    @JoinColumn(name = "id_destinataire")
    private Utilisateur destinataire;
}
