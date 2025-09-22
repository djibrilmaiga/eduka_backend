package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class TuteurDashboardDto {
    private int nombreEnfants;
    private int nombreEnfantsParraines;
    private int nombreDemandesEnAttente;
    private long nombreRapports;
    private long nombreDepenses;
}
