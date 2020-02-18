package com.chefserver.demo.repositories;

import com.chefserver.demo.model.ComentModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComentRepository extends MongoRepository<ComentModel, String> {
}
