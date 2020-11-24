package com.chefserver.demo.repositories;

import com.chefserver.demo.model.Menu;
import com.chefserver.demo.model.ListaMenus;
import com.chefserver.demo.model.Usuarios;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ListaMenusRepository extends MongoRepository<ListaMenus, String> {
    ListaMenus findByMenu(String menu);

    @Query(value="{}", fields="{menu : 1, _id : 0}")
    List<Menu> findListaMenus();
}
