package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DataModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReservaRepository extends MongoRepository<DataModel, String> {
    List<DataModel> findByEmpresa(String nombre);
}
