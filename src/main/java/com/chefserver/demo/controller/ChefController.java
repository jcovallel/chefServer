package com.chefserver.demo.controller;

import com.chefserver.demo.model.*;
import com.chefserver.demo.ExcelDB.ExcelController;
import com.chefserver.demo.repositories.*;
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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/chef")
public class ChefController {
    @Autowired
    private ISpecimenService specimenService;

    @Autowired
    private DispoRepository repository;

    @Autowired
    private ComentRepository rvrepository;

    @Autowired
    private EmpresasRepository emrepository;

    @Autowired
    private ReservaRepository reserepository;

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

    @GetMapping(value = "/download_excel/{empresa}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> download(@PathVariable String empresa) {

        ExcelController savetoexcel = new ExcelController();
        List<DataModel> dataModel = reserepository.findByEmpresa(empresa);
        try {
            savetoexcel.writeFile(dataModel);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    @RequestMapping(value = "/createuser/", method = RequestMethod.POST)
    public void createuser(@Valid @RequestBody EmpresasModel emodel) {
        emrepository.save(emodel);
    }

    @RequestMapping(value = "/getpass/{user}/{pass}", method = RequestMethod.GET)
    public Boolean comparepass( @PathVariable String user, @PathVariable String pass) {
        if(emrepository.findByNombre(user).getPassword().equals(pass)){
            return true;
        }else{
            return false;
        }
    }

    @RequestMapping(value = "/sendmail/{user}/{mail}/{cambio}", method = RequestMethod.GET)
    public Boolean comparemail( @PathVariable String user, @PathVariable String mail, @PathVariable Boolean cambio) throws NoSuchAlgorithmException {
        if(emrepository.findByNombre(user).getCorreo().equals(mail)){
            if(cambio){
                String tpass = passGen();
                emrepository.findByNombre(user).setPassword(tpass);
                EnvioEmail newmail = new EnvioEmail();
                String contenido =  "Su nueva contraseña temporal es:" + tpass + ", recuerde que debe cambiarla una vez inicie sesion";
                newmail.sendEmail(mail,"Recuperacion contraseña Kitchen Works", contenido);
            }
            return true;
        }else{
            return false;
        }
    }

    @RequestMapping(value = "/modifyinfoadmi/{user}/{nnombre}/{nmail}", method = RequestMethod.PUT)
    public void modifyinfoadmi(@PathVariable String empresa, @PathVariable String nnombre, @PathVariable String nmail) {
        EmpresasModel emodel = emrepository.findByNombre(empresa);
        emrepository.deleteByNombre(empresa);
        if(nnombre!=null){
            emodel.setNombre(nnombre);
        }
        if (nmail!=null){
            emodel.setCorreo(nmail);
        }
        emrepository.save(emodel);
    }

    @RequestMapping(value = "/getusers/", method = RequestMethod.GET)
    public List<User> getusers() {
        return emrepository.findNameAndExcludeId();
    }

    @RequestMapping(value = "/deleteresta/", method = RequestMethod.GET)
    public void deleteresta(@PathVariable String empresa) {
        emrepository.deleteByNombre(empresa);
    }

    @RequestMapping(value = "/review/", method = RequestMethod.POST)
    public void createreview(@Valid @RequestBody ComentModel cmodel) {
        rvrepository.save(cmodel);
    }

    @RequestMapping(value = "/admin/review/", method = RequestMethod.GET)
    public List<?> getreviewsAdmi() {
        return rvrepository.findAll();
    }

    @RequestMapping(value = "/user/review/{empresa}", method = RequestMethod.GET)
    public List<ComentModel> getreviewsUS(@PathVariable String empresa) {
        return rvrepository.findByEmpresa(empresa);
    }

    @RequestMapping(value = "/modifydatausers/{user}/{npass}/{nmail}", method = RequestMethod.PUT)
    public void modifydatauser(@PathVariable String user, @PathVariable String npass, @PathVariable String nmail) {
        EmpresasModel emodel = emrepository.findByNombre(user);
        emrepository.deleteByNombre(user);
        if(npass!=null){
            emodel.setPassword(npass);
        }
        if (nmail!=null){
            emodel.setCorreo(nmail);
        }
        emrepository.save(emodel);
    }

    @RequestMapping(value = "/disponibilidad/{empresa}/{dia}", method = RequestMethod.GET)
    public int getDisponibilidad(@PathVariable String empresa, @PathVariable String dia) {
        switch (dia){
            case "Lunes":{
                return repository.findByEmpresa(empresa).Lunes;
            }
            case "Martes":{
                return repository.findByEmpresa(empresa).Martes;
            }
            case "Miercoles":{
                return repository.findByEmpresa(empresa).Miercoles;
            }
            case "Jueves":{
                return repository.findByEmpresa(empresa).Jueves;
            }
            case "Viernes":{
                return repository.findByEmpresa(empresa).Viernes;
            }
        }
        return 0;
    }

    @RequestMapping(value = "/disponibilidad/{empresa}", method = RequestMethod.PUT)
    public void modifyDispo(@Valid @RequestBody DisponibilidadModel dispoModel, @PathVariable String empresa) {
        if(repository.findByEmpresa(empresa)!=null){
            DisponibilidadModel current = repository.findByEmpresa(empresa);
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

    @RequestMapping(value = "/disponibilidad/{empresa}", method = RequestMethod.DELETE)
    public void deleteDispo(@PathVariable String empresa) {
        repository.delete(repository.findByEmpresa(empresa));
    }

    @RequestMapping(value = "/getmail/{empresa}", method = RequestMethod.GET)
    public String getmail(@PathVariable String empresa) {
        return emrepository.findByNombre(empresa).getCorreo();
    }

    @RequestMapping(value = "/reserva/save", method = RequestMethod.POST)
    public void createReservationREG(@Valid @RequestBody DataModel dataModel) {
        reserepository.save(dataModel);
    }

    /*@RequestMapping(value = "/reserva/save", method = RequestMethod.POST)
    public void createReservationREG(@Valid @RequestBody DataModel dataModel) {
        ExcelController savetoexcel = new ExcelController();
        try {
            savetoexcel.writeFile(dataModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public String passGen() throws NoSuchAlgorithmException {
        String[] symbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        int length = 6;
        Random random = SecureRandom.getInstanceStrong();    // as of JDK 8, this should return the strongest algorithm available to the JVM
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int indexRandom = random.nextInt( symbols.length );
            sb.append( symbols[indexRandom] );
        }
        String password = sb.toString();
        return  password;
    }
}

