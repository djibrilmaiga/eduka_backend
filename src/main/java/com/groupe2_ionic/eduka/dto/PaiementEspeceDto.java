package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementEspeceDto {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @NotNull(message = "L'ID du parrainage est obligatoire")
    private Integer parrainageId;

    @NotNull(message = "L'ID de l'organisation est obligatoire")
    private Integer organisationId;

    @NotNull(message = "La date de réception est obligatoire")
    private LocalDate dateReception;

    @NotBlank(message = "La référence de réception est obligatoire")
    private String referenceReception;

    private String commentaire;
    private String nomDonateur; // Nom du parrain qui a donné en espèces
    private String contactDonateur; // Contact du donateur
}
