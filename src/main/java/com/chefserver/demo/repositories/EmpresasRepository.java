package com.chefserver.demo.repositories;

import com.chefserver.demo.model.Usuarios;
import com.chefserver.demo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EmpresasRepository extends MongoRepository<Usuarios, String> {
    Usuarios findByNombre(String nombre);

    @Query(value="{}", fields="{nombre : 1, _id : 0}")
    List<Usuarios> findNameAndExcludeId();

    @Query(value="{rol: 3}", fields="{nombre : 1, _id : 0}")
    List<Usuarios> findNameMobileAndExcludeId();

    @Query(value="{rol: 1}", fields="{imgnum : 1, _id : 0}")
    List<Usuarios> findImgnumTips();

    @Query(value="{rol: 1}", fields="{nombre : 1, _id : 0}")
    List<Usuarios> findAdminName();

    @Query(value="{_id: ?0}", fields="{imgnum : 1, _id : 0}")
    List<Usuarios> findImgnum(String empresa);
}
