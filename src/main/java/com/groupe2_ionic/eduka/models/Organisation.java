package com.groupe2_ionic.eduka.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.groupe2_ionic.eduka.models.enums.StatutValidation;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id_organisation")
public class Organisation extends Utilisateur{

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String nomRepresentant;

    @Column(nullable = false)
    private String prenomRepresentant;

    @Column(nullable = false)
    private String fonctionRepresentant;

    private String ville;

    private String pays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    private String commentaireValidation;

    private LocalDateTime dateValidation;

    // Une organisation est validé par un admin.
    @ManyToOne
    @JoinColumn(name = "id_admin")
    private Admin validateur;

    // Une organisation peut enregistrer plusieurs enfants.
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL)
    private Set<Enfant> enfants = new HashSet<>();

    // Une organisation peut enregistrer plusieurs paiements (paiement.type = ESPECE).
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL)
    private Set<Paiement> paiements = new HashSet<>();

    // Une organisation peut fournir plusieurs documents lors de son inscription.
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();

    // Une organisation peut créer plusieurs mini-rapports pour les parrains.
    @OneToMany(mappedBy = "organisation")
    private Set<Rapport> rapports = new HashSet<>();
}
