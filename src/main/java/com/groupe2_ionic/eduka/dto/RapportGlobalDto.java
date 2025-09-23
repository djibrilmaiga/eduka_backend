package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RapportGlobalDto {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private long nombreOrganisations;
    private long nombreEnfants;
    private long nombreParrains;
    private long nombrePaiements;
    private BigDecimal montantTotalPaiements;
    private List<StatistiqueParPaysDto> statistiquesParPays;
    private List<StatistiqueParOrganisationDto> topOrganisations;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatistiqueParPaysDto {
        private String pays;
        private long nombreOrganisations;
        private long nombreEnfants;
        private BigDecimal montantTotal;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatistiqueParOrganisationDto {
        private String nomOrganisation;
        private long nombreEnfants;
        private BigDecimal montantTotal;
        private String ville;
        private String pays;
    }
}
