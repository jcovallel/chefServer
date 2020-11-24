package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DisponibilidadPorFranjaHoraria;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DispoHorasRepository extends MongoRepository<DisponibilidadPorFranjaHoraria, String> {
    DisponibilidadPorFranjaHoraria findByEmpresaAndDia(String empresa, String dia);
    void deleteByEmpresa(String empresa);
}
