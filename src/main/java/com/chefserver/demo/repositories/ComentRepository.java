package com.chefserver.demo.repositories;

import com.chefserver.demo.model.ComentModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ComentRepository extends MongoRepository<ComentModel, String> {
    List<ComentModel> findByEmpresa(String empresa);
}
