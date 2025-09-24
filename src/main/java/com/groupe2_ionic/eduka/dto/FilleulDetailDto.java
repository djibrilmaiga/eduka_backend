package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.Genre;
import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class FilleulDetailDto {
    // Informations de base de l'enfant
    private int enfantId;
    private String nom;
    private String prenom;
    private Genre genre;
    private LocalDate dateNaissance;
    private int age;
    private String niveauScolaire;
    private String photoProfil;
    private String histoire;

    // Informations du parrainage
    private int parrainageId;
    private StatutParrainage statutParrainage;
    private LocalDate dateDebutParrainage;
    private BigDecimal montantTotalParrainage;
    private BigDecimal montantPaye;
    private BigDecimal montantRestant;
    private BigDecimal soldeEnfant;

    // Informations contextuelles
    private String organisationNom;
    private String ecoleNom;
    private String tuteurNom;
    private String tuteurTelephone;

    // Besoins actuels
    private List<BesoinDto> besoinsActuels;

    // Dernier rapport
    private RapportRecentDto dernierRapport;

    // Statistiques
    private int nombrePaiementsEffectues;
    private LocalDate dateDernierPaiement;
    private int nombreRapportsRecus;

    // Photos d'activités récentes (URLs)
    private List<String> photosActivitesRecentes;

    // Indicateurs de performance
    private boolean besoinUrgent;
    private boolean rapportEnRetard;
    private int joursDepuisDernierRapport;
}
