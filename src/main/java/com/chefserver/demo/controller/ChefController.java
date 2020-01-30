package com.chefserver.demo.controller;

import com.chefserver.demo.model.DataModel;
import com.chefserver.demo.ExcelDB.ExcelController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/reservacion")
public class ChefController {
    //@Autowired
    //private ChatRepository repository;

    //@RequestMapping(value = "/", method = RequestMethod.GET)
    /*public List<ChatModel> getAllChats() {
        return repository.findAll();
    }

    @RequestMapping(value = "/{emisor}/{receptor}", method = RequestMethod.GET)
    public List<ChatModel> getChatByEmiRec(@PathVariable int emisor, @PathVariable int receptor) {
        return repository.findByEmisorAndReceptor(emisor,receptor);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void modifyPetById(@PathVariable("id") ObjectId id, @Valid @RequestBody ChatModel chatModel) {
        chatModel.set_id(id);
        repository.save(chatModel);
    }*/

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void createReservationREG(@Valid @RequestBody DataModel dataModel) {
        ExcelController savetoexcel = new ExcelController();
        try {
            savetoexcel.writeFile(dataModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deletePet(@PathVariable ObjectId id) {
        repository.delete(repository.findBy_id(id));
    }*/
}
