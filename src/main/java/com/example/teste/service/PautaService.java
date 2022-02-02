package com.example.teste.service;

import com.example.teste.entities.Pauta;
import com.example.teste.entities.Votacao;
import com.example.teste.entities.dto.PautaTotalVotosDTO;
import com.example.teste.entities.response.PautaResponse;
import com.example.teste.repositories.PautaRepository;
import com.example.teste.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class PautaService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private PautaRepository pautaRepository;

    public void salvar(Pauta pauta) {
        pautaRepository.save(pauta);
    }

    public PautaResponse salvar(String assunto){
        PautaResponse pautaResponse = new PautaResponse();
        try {
            //Realizar a geração do numero da pauta
            Integer numeroDaPauta = gerarNumeroDaPauta();

            //Chamara interface para relizar a gravação no banco
            pautaRepository.save(new Pauta(assunto, numeroDaPauta));

            //Monta objeto de Retorno
            pautaResponse.setCode(HttpStatus.OK.value());
            pautaResponse.setMessagem("Pauta cadastrar com sucesso!");
            pautaResponse.setNumeroPauta(numeroDaPauta);
        }catch (Exception e){
            LOGGER.error("Erro ao realizar o cadastro da Pauta", e);
            pautaResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
            pautaResponse.setMessagem("Erro ao cadastrar uma pauta!");
        }
        return pautaResponse;
    }

    public List<PautaTotalVotosDTO> listaPautasTotalVotos(){

        List<PautaTotalVotosDTO> retornoPautaTotalVotosDTOS = new ArrayList<>();
        for (Pauta item: pautaRepository.findAll()) {

            PautaTotalVotosDTO pautaTotalVotosDTO = new PautaTotalVotosDTO();
            pautaTotalVotosDTO.setNumeroPauta(item.getNumeroPauta());
            pautaTotalVotosDTO.setAssunto(item.getAssunto());
            pautaTotalVotosDTO.setSim(item.getVotacoes().stream().filter(v -> v.getVoto().toUpperCase(Locale.ROOT).equals("SIM")).count());
            pautaTotalVotosDTO.setNao(item.getVotacoes().stream().filter(v -> v.getVoto().toUpperCase(Locale.ROOT).equals("NAO")).count());
            retornoPautaTotalVotosDTOS.add(pautaTotalVotosDTO);
        }

        return retornoPautaTotalVotosDTOS;
    }

    public List<Pauta> listaPautas(){
        return pautaRepository.findAll();
    }

    public Integer gerarNumeroDaPauta(){
        Integer numero = Utils.geradorNumeroRandow();
        if (verificaPautaExistente(numero)){
            this.gerarNumeroDaPauta();
        }
        return numero;
    }

    public Boolean verificaPautaExistente(Integer numeroPauta){
        Pauta pauta = pautaRepository.findByNumeroPauta(numeroPauta);
        if (Objects.nonNull(pauta)){
            return true;
        }
        return false;
    }
}