package com.chefserver.demo.controller;

import com.chefserver.demo.model.*;
import com.chefserver.demo.ExcelDB.ExcelController;
import com.chefserver.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
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

    @Autowired
    private JavaMailSender javaMailSender;

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

    @GetMapping(value = "/download_excel_comen/{empresa}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadcomen(@PathVariable String empresa) {

        ExcelController savetoexcel = new ExcelController();
        List<ComentModel> comentModel = rvrepository.findByEmpresa(empresa);
        try {
            savetoexcel.comentwriteFile(comentModel);
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
            String fileName = "Comentarios.xlsx";
            byteArray = Files.readAllBytes(Paths.get(dirPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "comentarios.xlsx" + "\"")
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
                EmpresasModel emodelold = emrepository.findByNombre(user);
                EmpresasModel emodelnew = new EmpresasModel();
                emodelnew.setPassword(hash(tpass));
                emodelnew.setNombreid(emodelold.getNombre());
                emodelnew.setNombre(emodelold.getNombre());
                emodelnew.setCorreo(emodelold.getCorreo());
                emrepository.deleteById(user);
                emrepository.save(emodelnew);
                String contenido =  "Su nueva contraseña temporal es: " + tpass + ", recuerde que debe cambiarla una vez inicie sesion";
                sendEmail(mail,"Recuperacion contraseña Kitchen Works", contenido);
            }
            return true;
        }else{
            return false;
        }
    }

    @RequestMapping(value = "/modifyinfoadmi/{user}/{nnombre}/{nmail}", method = RequestMethod.PUT)
    public void modifyinfoadmi(@PathVariable String user, @PathVariable String nnombre, @PathVariable String nmail) {
        EmpresasModel emodelold = emrepository.findByNombre(user);
        EmpresasModel emodelnew = new EmpresasModel();
        emodelnew.setPassword(emodelold.getPassword());

        if(!nmail.equals("NULL")){
            if(!nnombre.equals("NULL")){
                emodelnew.setNombreid(nnombre);
                emodelnew.setNombre(nnombre);
                emodelnew.setCorreo(nmail);
                emrepository.save(emodelnew);
            }else{
                emodelnew.setNombreid(emodelold.getNombre());
                emodelnew.setNombre(emodelold.getNombre());
                emodelnew.setCorreo(nmail);
                emrepository.save(emodelnew);
            }
        }else{
            if(!nnombre.equals("NULL")){
                emodelnew.setNombreid(nnombre);
                emodelnew.setNombre(nnombre);
                emodelnew.setCorreo(emodelold.getCorreo());
                emrepository.save(emodelnew);
            }
        }
        emrepository.deleteById(user);
        emrepository.save(emodelnew);
    }

    @RequestMapping(value = "/getusers/", method = RequestMethod.GET)
    public List<User> getusers() {
        return emrepository.findNameAndExcludeId();
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
        EmpresasModel emodelold = emrepository.findByNombre(user);
        EmpresasModel emodelnew = new EmpresasModel();
        emodelnew.setNombreid(emodelold.getNombre());
        emodelnew.setNombre(emodelold.getNombre());

        if(!nmail.equals("NULL")){
            if(!npass.equals("NULL")){
                emodelnew.setPassword(npass);
                emodelnew.setCorreo(nmail);
                emrepository.save(emodelnew);
            }else{
                emodelnew.setPassword(emodelold.getPassword());
                emodelnew.setCorreo(nmail);
                emrepository.save(emodelnew);
            }
        }else{
            if(!npass.equals("NULL")){
                emodelnew.setPassword(npass);
                emodelnew.setCorreo(emodelold.getCorreo());
                emrepository.save(emodelnew);
            }
        }
        emrepository.deleteById(user);
        emrepository.save(emodelnew);
    }

    @RequestMapping(value = "/disponibilidad/{empresa}/{dia}", method = RequestMethod.GET)
    public int getDisponibilidad(@PathVariable String empresa, @PathVariable String dia) {
        switch (dia){
            case "Lunes":{
                return repository.findByEmpresa(empresa).getLunes();
            }
            case "Martes":{
                return repository.findByEmpresa(empresa).getMartes();
            }
            case "Miercoles":{
                return repository.findByEmpresa(empresa).getMiercoles();
            }
            case "Jueves":{
                return repository.findByEmpresa(empresa).getJueves();
            }
            case "Viernes":{
                return repository.findByEmpresa(empresa).getViernes();
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

    /*@RequestMapping(value = "/deleteuser/{empresa}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable String empresa) {
        emrepository.deleteById(empresa);
    }*/

    @RequestMapping(value = "/deleteuser/{empresa}", method = RequestMethod.GET)
    public void deleteresta(@PathVariable String empresa) {
        emrepository.deleteById(empresa);
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

    void sendEmail(String to, String subject, String content) {

        SimpleMailMessage email = new SimpleMailMessage();

        email.setTo(to);
        email.setSubject(subject);
        email.setText(content);

        javaMailSender.send(email);

    }

    public String passGen() throws NoSuchAlgorithmException {
        String[] symbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        int length = 8;
        Random random = SecureRandom.getInstanceStrong();    // as of JDK 8, this should return the strongest algorithm available to the JVM
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int indexRandom = random.nextInt( symbols.length );
            sb.append( symbols[indexRandom] );
        }
        String password = sb.toString();
        return  password;
    }

    public String hash(String pass){
        String algoritmoHash = "SHA-256";
        byte[] bytePass = pass.getBytes();
        byte[] passHashed;
        String passHashedValue = "";
        try {
            MessageDigest funcionHash = MessageDigest.getInstance(algoritmoHash);
            funcionHash.update(bytePass);
            passHashed = funcionHash.digest();
            passHashedValue = DatatypeConverter.printHexBinary(passHashed);
        } catch (Exception e) {

        }
        return passHashedValue;
    }
}

