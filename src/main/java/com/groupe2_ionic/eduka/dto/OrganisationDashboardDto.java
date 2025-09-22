package com.groupe2_ionic.eduka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrganisationDashboardDto {
    private long nombreEnfants;
    private long nombreEnfantsParraines;
    private long nombreRapports;
    private long nombreDepenses;
    private BigDecimal montantTotalDepenses;
}
