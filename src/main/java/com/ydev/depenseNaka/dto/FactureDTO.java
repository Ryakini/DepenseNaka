package com.ydev.depenseNaka.dto;

import com.ydev.depenseNaka.model.ModePaiement;
import com.ydev.depenseNaka.model.StatusPaiement;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record FactureDTO(
        String idFacture,
        LocalDate dateFacture,
        LocalDate dateModifFacture,
        double montantFacture,
        double avanceFacture,
        double resteFacture,
        double margeFacture,
        ModePaiement modePaiement,
        StatusPaiement status,
        ClientDTO client,
        List<FactureProduitDTO> factureProduitEntityList
) {}
