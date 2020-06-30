package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DispoHorasModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DispoHorasRepository extends MongoRepository<DispoHorasModel, String> {
    DispoHorasModel findByEmpresaAndDia(String empresa, String dia);
    void deleteByEmpresa(String empresa);
}
