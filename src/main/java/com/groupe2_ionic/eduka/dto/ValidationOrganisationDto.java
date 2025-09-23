package com.groupe2_ionic.eduka.dto;

import com.groupe2_ionic.eduka.models.enums.StatutValidation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationOrganisationDto {

    @NotNull(message = "Le statut de validation est obligatoire")
    private StatutValidation statut;

    private String commentaire;
}
