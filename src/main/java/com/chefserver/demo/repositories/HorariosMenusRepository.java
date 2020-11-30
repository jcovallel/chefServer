package com.chefserver.demo.repositories;

import com.chefserver.demo.model.HorarioMenusReturn;
import com.chefserver.demo.model.HorariosMenus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface HorariosMenusRepository extends MongoRepository<HorariosMenus, String> {
    @Query(value="{empresa: ?0, menu: ?1}", fields="{ hInicioRes : 1, hFinRes : 1, hInicioEnt : 1, hFinEnt : 1, menu : 1, empresa : 1, _id : 0}")
    List<HorarioMenusReturn> findListaHoras(String empresa, String menu);
}
