package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Parrain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParrainRepository extends JpaRepository<Parrain, Integer> {
    List<Parrain> findByPays(String pays);
}
