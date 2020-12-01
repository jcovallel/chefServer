package com.chefserver.demo.repositories;

import com.chefserver.demo.model.ListaMenusEmpresas;
import com.chefserver.demo.model.MenuEmpresa;
import com.chefserver.demo.model.MenuTrue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ListaMenusEmpresaRepository extends MongoRepository<ListaMenusEmpresas, String> {

    @Query(value="{empresa: ?0}", fields="{ check : 1, menu : 1, empresa : 1, _id : 0}")
    List<MenuEmpresa> findListaMenus(String empresa);

    @Query(value="{empresa: ?0, check: true}", fields="{ menu : 1, empresa : 1, check : 1, _id : 0}")
    List<MenuTrue> findListaMenusTrue(String empresa);

    void deleteByEmpresa (String empresa);
}
