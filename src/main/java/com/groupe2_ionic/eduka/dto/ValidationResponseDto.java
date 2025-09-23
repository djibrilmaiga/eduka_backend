package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.StatutValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResponseDto {
    private Integer organisationId;
    private String nomOrganisation;
    private StatutValidation statut;
    private String commentaire;
    private String validateurNom;
    private LocalDateTime dateValidation;
}
