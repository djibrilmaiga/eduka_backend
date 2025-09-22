package com.groupe2_ionic.eduka.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id_parrain")
public class Parrain extends Utilisateur{

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String ville;

    private String pays;

    private String photoProfil;

    private Boolean anonyme = false;

    // Un parrain peut contribuer à plusieurs parrainages.
    @OneToMany(mappedBy = "parrain")
    private Set<Parrainage> parrainages = new HashSet<>();

    // Un parrain peut effectuer plusieurs paiements.
    @OneToMany(mappedBy = "parrain")
    private Set<Paiement> paiements = new HashSet<>();

    // Un parrain peut valider plusieurs transferts de fonds.
    @OneToMany(mappedBy = "parrain")
    private Set<TransfertFond> transfertFonds = new HashSet<>();
}
