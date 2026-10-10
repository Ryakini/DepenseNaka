package com.ydev.depenseNaka.dto;

import lombok.Builder;

@Builder
public record ClientDTO(
        String idClient,
        String nomClient,
        String prenomClient,
        String telephoneClient,
        String adresseClient
) {}
