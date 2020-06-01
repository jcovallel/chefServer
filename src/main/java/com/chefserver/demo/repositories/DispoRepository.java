package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DisponibilidadModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DispoRepository extends MongoRepository<DisponibilidadModel, String> {
    DisponibilidadModel findByEmpresa(String empresa);
}
