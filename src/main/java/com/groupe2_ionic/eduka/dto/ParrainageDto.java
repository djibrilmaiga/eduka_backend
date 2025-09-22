package com.groupe2_ionic.eduka.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class ParrainageDto {
    
    @NotNull(message = "L'ID du parrain est obligatoire")
    private Integer parrainId;
    
    @NotNull(message = "L'ID de l'enfant est obligatoire")
    private Integer enfantId;
    
    @NotNull(message = "Le montant total est obligatoire")
    @DecimalMin(value = "1.0", message = "Le montant doit être supérieur à 0")
    private BigDecimal montantTotal;
    
    private String motifFin;
}
