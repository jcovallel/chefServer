package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DisponibilidadPorMenu;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DispoRepository extends MongoRepository<DisponibilidadPorMenu, String> {
    DisponibilidadPorMenu findByEmpresa(String empresa);
}
