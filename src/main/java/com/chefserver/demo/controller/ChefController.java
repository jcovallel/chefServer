package com.chefserver.demo.controller;

import com.chefserver.demo.model.ComentModel;
import com.chefserver.demo.model.DataModel;
import com.chefserver.demo.model.DisponibilidadModel;
import com.chefserver.demo.ExcelDB.ExcelController;
import com.chefserver.demo.repositories.ComentRepository;
import com.chefserver.demo.repositories.DispoRepository;
import com.chefserver.demo.repositories.ISpecimenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/chef")
public class ChefController {
    @Autowired
    private ISpecimenService specimenService;

    @Autowired
    private DispoRepository repository;

    @Autowired
    private ComentRepository rvrepository;

    @PostMapping("/uploadmenu")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile){
        String returnValue = "error";
        try {
            specimenService.saveImage(imageFile);
            returnValue = "exitosa";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    @GetMapping(value = "/download_excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> download() {
        //for local
        // String dirPath = "DatosExcel/";
        //for gcloud
        String dirPath = "../DatosExcel/";
        byte[] byteArray;  // data comes from external service call in byte[]
        byteArray = null;
        try {
            String fileName = "Reservaciones.xlsx";
            byteArray = Files.readAllBytes(Paths.get(dirPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "reservas.xlsx" + "\"")
                .body(byteArray);
    }

    @RequestMapping(value = "/review/", method = RequestMethod.POST)
    public void createreview(@Valid @RequestBody ComentModel cmodel) {
        rvrepository.save(cmodel);
    }

    @RequestMapping(value = "/review/", method = RequestMethod.GET)
    public List<?> getreviews() {
        return rvrepository.findAll();
    }

    @RequestMapping(value = "/disponibilidad/{dia}", method = RequestMethod.GET)
    public int getDisponibilidad(@PathVariable String dia) {
        switch (dia){
            case "Lunes":{
                return repository.findById(0).lunes;
            }
            case "Martes":{
                return repository.findById(0).martes;
            }
            case "Miercoles":{
                return repository.findById(0).miercoles;
            }
            case "Jueves":{
                return repository.findById(0).jueves;
            }
            case "Viernes":{
                return repository.findById(0).viernes;
            }
        }
        return 0;
    }

    @RequestMapping(value = "/disponibilidad/", method = RequestMethod.PUT)
    public void modifyDispo(@Valid @RequestBody DisponibilidadModel dispoModel) {
        System.out.println("peticion solicitada por: "+dispoModel);
        if(repository.findById(0)!=null){
            DisponibilidadModel current = repository.findById(0);
            repository.delete(current);
            if(dispoModel.getLunes()==null){
                dispoModel.setLunes(current.getLunes());
            }
            if(dispoModel.getMartes()==null){
                dispoModel.setMartes(current.getMartes());
            }
            if(dispoModel.getMiercoles()==null){
                dispoModel.setMiercoles(current.getMiercoles());
            }
            if(dispoModel.getJueves()==null){
                dispoModel.setJueves(current.getJueves());
            }
            if(dispoModel.getViernes()==null){
                dispoModel.setViernes(current.getViernes());
            }
        }
        repository.save(dispoModel);
    }

    @RequestMapping(value = "/disponibilidad/{id}", method = RequestMethod.DELETE)
    public void deletePet(@PathVariable int id) {
        repository.delete(repository.findById(id));
    }

    @RequestMapping(value = "/reserva/save", method = RequestMethod.POST)
    public void createReservationREG(@Valid @RequestBody DataModel dataModel) {
        ExcelController savetoexcel = new ExcelController();
        try {
            savetoexcel.writeFile(dataModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
