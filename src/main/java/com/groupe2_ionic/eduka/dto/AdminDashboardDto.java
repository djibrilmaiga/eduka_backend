package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDto {
    private long totalOrganisations;
    private long organisationsEnAttente;
    private long organisationsValidees;
    private long organisationsRejetees;
    private long totalEnfants;
    private long totalParrains;
    private long totalPaiements;
    private BigDecimal montantTotalPaiements;
    private long transfertsEnAttente;
    private long litiges;
}
