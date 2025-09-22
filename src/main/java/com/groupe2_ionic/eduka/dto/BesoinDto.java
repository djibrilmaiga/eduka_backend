package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class BesoinDto {
    
    @NotBlank(message = "Le type de besoin est obligatoire")
    private String type;
    
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;
    
    @NotNull(message = "L'ID de l'enfant est obligatoire")
    private Integer enfantId;
}
