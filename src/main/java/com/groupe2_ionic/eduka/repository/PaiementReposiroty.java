package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementReposiroty extends JpaRepository<Paiement, Integer> {
}
