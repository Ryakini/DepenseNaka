package com.ydev.depenseNaka.mapper;

import com.ydev.depenseNaka.dto.ClientDTO;
import com.ydev.depenseNaka.model.ClientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientMapper {
    public ClientDTO toDTO(ClientEntity entity) {
        if (entity == null) return null;
        return new ClientDTO(
                entity.getIdClient(),
                entity.getNomClient(),
                entity.getPrenomClient(),
                entity.getTelephoneClient(),
                entity.getAdresseClient()
        );
    }

    public ClientEntity toEntity(ClientDTO dto) {
        if (dto == null) return null;
        return ClientEntity.builder()
                .idClient(dto.idClient())
                .nomClient(dto.nomClient())
                .prenomClient(dto.prenomClient())
                .telephoneClient(dto.telephoneClient())
                .adresseClient(dto.adresseClient())
                .build();
    }
}
