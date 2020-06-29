package com.chefserver.demo.repositories;

import com.chefserver.demo.model.EmpresasModel;
import com.chefserver.demo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EmpresasRepository extends MongoRepository<EmpresasModel, String> {
    EmpresasModel findByNombre(String nombre);

    @Query(value="{}", fields="{nombre : 1, _id : 0}")
    List<User> findNameAndExcludeId();

    @Query(value="{rol: 3}", fields="{nombre : 1, _id : 0}")
    List<User> findNameMobileAndExcludeId();
}
