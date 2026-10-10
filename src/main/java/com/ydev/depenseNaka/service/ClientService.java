package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.ClientDTO;
import com.ydev.depenseNaka.exception.ImpossibleDeleteClient;
import com.ydev.depenseNaka.exception.NotFoundClient;
import com.ydev.depenseNaka.mapper.ClientMapper;
import com.ydev.depenseNaka.model.ClientEntity;
import com.ydev.depenseNaka.model.IdClientSequence;
import com.ydev.depenseNaka.repository.ClientRepository;
import com.ydev.depenseNaka.repository.IdClientSequenceRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final IdClientSequenceRepository idClientSequenceRepository;
    private final ClientMapper clientMapper;

    //creation
    @Transactional
    public ClientDTO createClient(ClientDTO clientDTO) {
        //initialisation de la sequene
        IdClientSequence newIdClientSequence = idClientSequenceRepository.findById("id_sequence")
                .orElse(new IdClientSequence("id_sequence", 0L));

        //si existe on incremente
        newIdClientSequence.setLastValue(newIdClientSequence.getLastValue() + 1);
        idClientSequenceRepository.save(newIdClientSequence);

        //identifiant personnalisé
        String customId = String.format("CLI-%04d", newIdClientSequence.getLastValue());

        ClientEntity save = clientMapper.toEntity(clientDTO);
        save.setIdClient(customId);

        ClientEntity enregistrer = clientRepository.save(save);
        return clientMapper.toDTO(enregistrer);
    }

    //mise à jour
    @Transactional
    public ClientDTO updateClient(String idClient, ClientDTO clientDTO) {
        //verification existence de client ayant cet identifiant
        ClientEntity existingClient = clientRepository.findById(idClient)
                .orElseThrow(()-> new NotFoundClient(idClient));
        if (clientDTO.nomClient() != null && !clientDTO.nomClient().isBlank()) {
            existingClient.setNomClient(clientDTO.nomClient());
        }
        if (clientDTO.prenomClient() != null && !clientDTO.prenomClient().isBlank()) {
            existingClient.setPrenomClient(clientDTO.prenomClient());
        }
        if (clientDTO.telephoneClient() != null && !clientDTO.telephoneClient().isBlank()) {
            existingClient.setTelephoneClient(clientDTO.telephoneClient());
        }
        if (clientDTO.adresseClient() != null && !clientDTO.adresseClient().isBlank()) {
            existingClient.setAdresseClient(clientDTO.adresseClient());
        }

        ClientEntity updated = clientRepository.save(existingClient);
        return clientMapper.toDTO(updated);
    }

    //delete
    @Transactional
    public void deleteClient(String idClient) {
        ClientEntity existClient = clientRepository.findById(idClient)
                .orElseThrow(()-> new NotFoundClient(idClient));

        if (existClient.getFacture() != null && !existClient.getFacture().isEmpty()) {
            throw new ImpossibleDeleteClient();
        }else {
            clientRepository.deleteById(existClient.getIdClient());
        }
    }

    //liste
    @Transactional(readOnly = true)
    public List<ClientDTO> findAllByOrderByNomClientAsc() {
        return clientRepository.findAllByOrderByNomClientAsc()
                .stream()
                .map(clientMapper::toDTO)
                .toList();
    }

    public long compterNbrClient(){
        return clientRepository.count();
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> findAllByOrderByNomClientAscSearch(String nomClient) {
        return clientRepository.findByNomClientContainingIgnoreCaseOrderByNomClientAsc(nomClient)
                .stream()
                .map(clientMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    Page<ClientDTO> findAllByOrderByNomClientAscPageable(String idClient, String nomClient, String prenomClient, String telephoneClient,
                                                            String adresseClient, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nomClient").ascending());
        Page<ClientEntity> entityPage = clientRepository.findAll((root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            if (idClient != null) {
                predicates.add(cb.like(cb.lower(root.get("idClient")), '%' + idClient.toLowerCase() + '%' ));
            }

            if(nomClient != null ){
                predicates.add(cb.like(cb.lower(root.get("nomClient")), "%"+nomClient.toLowerCase()+"%"));
            }

            if (prenomClient != null) {
                predicates.add(cb.like(cb.lower(root.get("prenomClient")), "%"+prenomClient.toLowerCase()+"%" ));
            }

            if (telephoneClient != null) {
                predicates.add(cb.like(cb.lower(root.get("telephoneClient")), "%"+telephoneClient.toLowerCase()+"%" ));
            }
            if (adresseClient != null) {
                predicates.add(cb.like(cb.lower(root.get("adresseClient")), "%"+adresseClient.toLowerCase()+"%" ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return entityPage.map(clientMapper::toDTO);
    }
}
