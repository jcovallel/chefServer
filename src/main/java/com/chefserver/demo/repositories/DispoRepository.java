package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DisponibilidadPorMenu;
import com.chefserver.demo.model.DisponibilidadPorMenuReturn;
import com.chefserver.demo.model.HorarioMenusReturn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DispoRepository extends MongoRepository<DisponibilidadPorMenu, String> {
    @Query(value="{empresa: ?0, menu: ?1}", fields="{ Lunesref : 1, Martesref : 1, Miercolesref : 1, Juevesref : 1, " +
            "Viernesref : 1, Sabadoref : 1, Domingoref : 1, Lunes : 1, Martes : 1, Miercoles : 1, " +
            "Jueves : 1, Viernes : 1, Sabado : 1, Domingo : 1, menu : 1, empresa : 1, _id : 0}")
    List<DisponibilidadPorMenuReturn> findDispoRefMenus(String empresa, String menu);

    @Query(value="{empresa: ?0, menu: ?1}", fields="{ Lunes : 1, Martes : 1, Miercoles : 1, Jueves : 1, " +
            "Viernes : 1, Sabado : 1, Domingo : 1, Lunesref : 1, Martesref : 1, Miercolesref : 1, " +
            "Juevesref : 1, Viernesref : 1, Sabadoref : 1, Domingoref : 1, menu : 1, empresa : 1, _id : 0}")
    List<DisponibilidadPorMenuReturn> findDispoMenus(String empresa, String menu);
}
