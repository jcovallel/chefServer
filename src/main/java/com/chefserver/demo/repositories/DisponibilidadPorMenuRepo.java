package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DisponibilidadPorMenu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DisponibilidadPorMenuRepo extends MongoRepository<DisponibilidadPorMenu, String> {
    @Query(value="{empresa: ?0, menu: ?1}", fields="{ lunesref : 1, martesref : 1, miercolesref : 1, juevesref : 1, " +
            "viernesref : 1, sabadoref : 1, domingoref : 1, lunes : 1, martes : 1, miercoles : 1, " +
            "jueves : 1, viernes : 1, sabado : 1, domingo : 1, menu : 1, empresa : 1, _id : 0}")
    List<DisponibilidadPorMenu> findDispoRefMenus(String empresa, String menu);

    @Query(value="{empresa: ?0, menu: ?1}", fields="{ lunes : 1, martes : 1, miercoles : 1, jueves : 1, " +
            "viernes : 1, sabado : 1, domingo : 1, lunesref : 1, martesref : 1, miercolesref : 1, " +
            "juevesref : 1, viernesref : 1, sabadoref : 1, domingoref : 1, menu : 1, empresa : 1, _id : 0}")
    List<DisponibilidadPorMenu> findDispoMenus(String empresa, String menu);

    void deleteByEmpresa (String empresa);

    void deleteByMenu (String menu);
}
