package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DiasDisponiblesPorSitio;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AvailableRepository extends MongoRepository<DiasDisponiblesPorSitio, String> {
}
