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
public class PayPalPaymentService {
    
    private final PaiementReposiroty paiementRepository;
    
    @Value("${paypal.client.id:}")
    private String paypalClientId;
    
    @Value("${paypal.client.secret:}")
    private String paypalClientSecret;
    
    @Value("${paypal.mode:sandbox}")
    private String paypalMode;
    
    /**
     * Créer un paiement PayPal
     */
    public PaiementResponseDto creerPaiement(Paiement paiement, PaiementRequestDto requestDto) {
        try {
            // Configuration PayPal (simulation)
            // Dans un vrai projet, utiliser la SDK PayPal
            
            String orderId = "PAYPAL_" + System.currentTimeMillis();
            
            // Mettre à jour le paiement
            paiement.setTransactionId(orderId);
            paiement.setStatut(StatutPaiement.INITE);
            
            paiement = paiementRepository.save(paiement);
            
            // URL d'approbation PayPal (simulation)
            String approvalUrl = String.format(
                "https://www.%spaypal.com/checkoutnow?token=%s",
                paypalMode.equals("sandbox") ? "sandbox." : "",
                orderId
            );
            
            PaiementResponseDto response = new PaiementResponseDto();
            response.setId(paiement.getId());
            response.setMontant(paiement.getMontant());
            response.setMethodePaiement(paiement.getMethode());
            response.setStatut(paiement.getStatut());
            response.setDatePaiement(paiement.getDatePaiement());
            response.setTransactionId(paiement.getTransactionId());
            response.setPaymentUrl(approvalUrl);
            
            return response;
            
        } catch (Exception e) {
            log.error("Erreur PayPal: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création du paiement PayPal: " + e.getMessage());
        }
    }
    
    /**
     * Capturer un paiement PayPal après approbation
     */
    public void capturerPaiement(String orderId) {
        try {
            // Capturer le paiement PayPal
            // Dans un vrai projet, utiliser l'API PayPal
            
            Paiement paiement = paiementRepository.findByTransactionId(orderId)
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
            
            paiement.setStatut(StatutPaiement.REUSSI);
            paiement.setCodeConfirmation("PAYPAL_CAPTURED_" + System.currentTimeMillis());
            
            paiementRepository.save(paiement);
            
            log.info("Paiement PayPal capturé: {}", orderId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la capture PayPal: {}", e.getMessage());
            throw new RuntimeException("Erreur capture PayPal");
        }
    }
}
