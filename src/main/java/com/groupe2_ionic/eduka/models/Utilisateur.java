package com.groupe2_ionic.eduka.models;

import com.groupe2_ionic.eduka.models.enums.RoleUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity @AllArgsConstructor @NoArgsConstructor @Data
// Les sous-classes auront une clé étrangère qui référence la clé primaire de la classe de base.
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private int id;

    @Column(unique = true)
    private String telephone;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleUser role;

    @Column(nullable = false)
    private LocalDate dateInscription;

    @Column(nullable = false)
    private Boolean actif;

    // Un utilisateur peut avoir plusieurs notifications
    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();
}
