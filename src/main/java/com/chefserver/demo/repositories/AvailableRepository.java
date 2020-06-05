package com.chefserver.demo.repositories;

import com.chefserver.demo.model.AvailaibleDays;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AvailableRepository extends MongoRepository<AvailaibleDays, String> {
}
