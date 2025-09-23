package com.groupe2_ionic.eduka.services.payment;

import com.groupe2_ionic.eduka.dto.PaiementRequestDto;
import com.groupe2_ionic.eduka.dto.PaiementResponseDto;
import com.groupe2_ionic.eduka.models.Paiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import com.groupe2_ionic.eduka.repository.PaiementReposiroty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoovMoneyService {
    
    private final PaiementReposiroty paiementRepository;
    
    @Value("${moov.money.api.url:https://api.moov-africa.ml/v1}")
    private String moovApiUrl;
    
    @Value("${moov.money.api.key:}")
    private String apiKey;
    
    @Value("${moov.money.merchant.id:}")
    private String merchantId;
    
    /**
     * Initier un paiement Moov Money
     */
    public PaiementResponseDto initierPaiement(Paiement paiement, PaiementRequestDto requestDto) {
        try {
            // Validation du numéro de téléphone Moov
            if (requestDto.getNumeroTelephone() == null || !requestDto.getNumeroTelephone().matches("^\\+223[67]\\d{7}$")) {
                throw new RuntimeException("Numéro de téléphone Moov Money invalide");
            }
            
            String transactionId = "MOOV_" + System.currentTimeMillis();
            
            // Simulation de l'appel API Moov Money
            // Dans un vrai projet, faire l'appel HTTP à l'API Moov Money
            
            paiement.setTransactionId(transactionId);
            paiement.setNumeroTelephone(requestDto.getNumeroTelephone());
            paiement.setStatut(StatutPaiement.INITE);
            
            paiement = paiementRepository.save(paiement);
            
            log.info("Demande de paiement Moov Money envoyée au {}", requestDto.getNumeroTelephone());
            
            PaiementResponseDto response = new PaiementResponseDto();
            response.setId(paiement.getId());
            response.setMontant(paiement.getMontant());
            response.setMethodePaiement(paiement.getMethode());
            response.setStatut(paiement.getStatut());
            response.setDatePaiement(paiement.getDatePaiement());
            response.setTransactionId(paiement.getTransactionId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erreur Moov Money: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du paiement Moov Money: " + e.getMessage());
        }
    }
    
    /**
     * Confirmer un paiement Moov Money
     */
    public void confirmerPaiement(String transactionId, String codeConfirmation) {
        try {
            Paiement paiement = paiementRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
            
            // Vérifier le code de confirmation avec l'API Moov
            // Simulation
            
            paiement.setStatut(StatutPaiement.REUSSI);
            paiement.setCodeConfirmation(codeConfirmation);
            
            paiementRepository.save(paiement);
            
            log.info("Paiement Moov Money confirmé: {}", transactionId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la confirmation Moov Money: {}", e.getMessage());
            throw new RuntimeException("Erreur confirmation Moov Money");
        }
    }
}
