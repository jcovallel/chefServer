package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DiasDispoSitio;
import com.chefserver.demo.model.DiasDisponiblesPorSitio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface dispoDiasSitioRepository extends MongoRepository<DiasDisponiblesPorSitio, String> {
    @Query(value="{empresa: ?0}", fields="{ lunes : 1, martes : 1, miercoles : 1, jueves : 1, viernes : 1, sabado : 1, domingo : 1, empresa : 1, _id : 0}")
    List<DiasDispoSitio> findListaDias(String empresa);
}
