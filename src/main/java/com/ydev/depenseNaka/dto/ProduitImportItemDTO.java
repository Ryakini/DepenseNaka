package com.ydev.depenseNaka.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProduitImportItemDTO {
    private final ProduitDTO dto;
    private final StringProperty statut;

    public ProduitImportItemDTO(ProduitDTO dto, String statutInitial) {
        this.dto = dto;
        this.statut = new SimpleStringProperty(statutInitial);
    }

    public ProduitDTO getDto() {
        return dto;
    }

    public String getStatut() {
        return statut.get();
    }

    public void setStatut(String value) {
        this.statut.set(value);
    }

    public StringProperty statutProperty() {
        return statut;
    }
}
