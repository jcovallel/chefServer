package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DataModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservaRepository extends MongoRepository<DataModel, String> {
}
