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
public class WavePaymentService {
    
    private final PaiementReposiroty paiementRepository;
    
    @Value("${wave.api.url:https://api.wave.com/v1}")
    private String waveApiUrl;
    
    @Value("${wave.api.key:}")
    private String apiKey;
    
    @Value("${wave.merchant.id:}")
    private String merchantId;
    
    /**
     * Initier un paiement Wave
     */
    public PaiementResponseDto initierPaiement(Paiement paiement, PaiementRequestDto requestDto) {
        try {
            // Validation du numéro de téléphone Wave
            if (requestDto.getNumeroTelephone() == null || !requestDto.getNumeroTelephone().matches("^\\+221[7]\\d{8}$")) {
                throw new RuntimeException("Numéro de téléphone Wave invalide (format Sénégal requis)");
            }
            
            String transactionId = "WAVE_" + System.currentTimeMillis();
            
            // Simulation de l'appel API Wave
            // Dans un vrai projet, faire l'appel HTTP à l'API Wave
            
            paiement.setTransactionId(transactionId);
            paiement.setNumeroTelephone(requestDto.getNumeroTelephone());
            paiement.setStatut(StatutPaiement.INITE);
            
            paiement = paiementRepository.save(paiement);
            
            log.info("Demande de paiement Wave envoyée au {}", requestDto.getNumeroTelephone());
            
            PaiementResponseDto response = new PaiementResponseDto();
            response.setId(paiement.getId());
            response.setMontant(paiement.getMontant());
            response.setMethodePaiement(paiement.getMethode());
            response.setStatut(paiement.getStatut());
            response.setDatePaiement(paiement.getDatePaiement());
            response.setTransactionId(paiement.getTransactionId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erreur Wave: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du paiement Wave: " + e.getMessage());
        }
    }
    
    /**
     * Traiter le callback Wave
     */
    public void traiterCallback(String transactionId, String statut, String codeConfirmation) {
        try {
            Paiement paiement = paiementRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
            
            StatutPaiement nouveauStatut = switch (statut.toUpperCase()) {
                case "SUCCESS", "COMPLETED" -> StatutPaiement.REUSSI;
                case "FAILED", "CANCELLED" -> StatutPaiement.ECHEC;
                default -> StatutPaiement.INITE;
            };
            
            paiement.setStatut(nouveauStatut);
            paiement.setCodeConfirmation(codeConfirmation);
            
            paiementRepository.save(paiement);
            
            log.info("Callback Wave traité: {} - {}", transactionId, statut);
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement du callback Wave: {}", e.getMessage());
        }
    }
}
