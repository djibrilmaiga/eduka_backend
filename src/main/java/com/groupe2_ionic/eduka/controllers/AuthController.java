package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.DTO.OrganisationConnecterDTO;
import com.groupe2_ionic.eduka.DTO.OrganisationInscrireDTO;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.services.OrganisationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private OrganisationService organisationService;
    @PostMapping("/register")
    public ResponseEntity<Organisation> register(@Valid @RequestBody OrganisationInscrireDTO dto) {
        Organisation organisation = organisationService.registerOrganisation(dto);
        return ResponseEntity.ok(organisation);

    }

    @PostMapping("/login")
    public ResponseEntity<Organisation> login(@Valid @RequestBody OrganisationConnecterDTO dto) {
        Organisation organisation = organisationService.loginOrganisation(dto);
        return ResponseEntity.ok(organisation);
    }
}
