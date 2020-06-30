package com.chefserver.demo.controller;

import com.chefserver.demo.model.*;
import com.chefserver.demo.ExcelDB.ExcelController;
import com.chefserver.demo.repositories.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.ArrayList;
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
    private DispoHorasRepository dhrepository;

    @Autowired
    private ComentRepository rvrepository;

    @Autowired
    private EmpresasRepository emrepository;

    @Autowired
    private ReservaRepository reserepository;

    @Autowired
    private AvailableRepository arepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @PostMapping("/uploadmenu/{empresa}")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile, @PathVariable String empresa){
        String returnValue = "error";
        try {
            specimenService.saveImage(imageFile, empresa);
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
        List<ComentModel> comentModel;
        if(empresa.equals("Administrador")){
            comentModel = rvrepository.findAll();
        }else{
            comentModel = rvrepository.findByEmpresa(empresa);
        }
        try {
            savetoexcel.comentwriteFile(comentModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //for local
        //String dirPath = "DatosExcel/";
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

    @RequestMapping(value = "/getrol/{nombre}/{rol}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String comparerol( @PathVariable String nombre, @PathVariable int rol) {
        if(emrepository.findByNombre(nombre).getRol() < rol){
            return "{\"response\":true}";
        }else{
            return "{\"response\":false}";
        }
    }

    @RequestMapping(value = "/getrol/{nombre}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getrol( @PathVariable String nombre) {
        return "{\"responserol\":\""+emrepository.findByNombre(nombre).getRol()+"\"}";
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

    @RequestMapping(value = "/getusersmobile/", method = RequestMethod.GET)
    public List<User> getusersmobile() {
        return emrepository.findNameMobileAndExcludeId();
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

    @RequestMapping(value = "/setavailabledays/", method = RequestMethod.POST)
    public void setavadays(@Valid @RequestBody AvailaibleDays amodel) {
        arepository.save(amodel);
    }

    @RequestMapping(value = "/getavailabledays/", method = RequestMethod.GET)
    public List<Dia> getavadays() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        List<Dia> dlist = new ArrayList<Dia>();
        Dia dia;
        if(dmodel.getLunes()){
            dia = new Dia();
            dia.setDia("Lunes");
            dlist.add(dia);
        }
        if(dmodel.getMartes()){
            dia = new Dia();
            dia.setDia("Martes");
            dlist.add(dia);
        }
        if(dmodel.getMiercoles()){
            dia = new Dia();
            dia.setDia("Miércoles");
            dlist.add(dia);
        }
        if(dmodel.getJueves()){
            dia = new Dia();
            dia.setDia("Jueves");
            dlist.add(dia);
        }
        if(dmodel.getViernes()){
            dia = new Dia();
            dia.setDia("Viernes");
            dlist.add(dia);
        }
        return dlist;
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

    @RequestMapping(value = "/reserva/save/{provisional}", method = RequestMethod.POST)
    public void createReservationREG(@Valid @RequestBody DataModel dataModel, @PathVariable String provisional) {
        String dia = provisional.replace(dataModel.getEmpresa(),"");
        if(dia.equals("Miercoles")){
            dia = "Miércoles";
        }
        if(!dataModel.getHoraentrega().equals("")){
            DispoHorasModel dmodel = dhrepository.findByEmpresaAndDia(dataModel.getEmpresa(),dia);
            setFranjaEQ(dataModel.getHoraentrega(), dmodel, dhrepository);
        }
        String contenido = "Hola! "+dataModel.getNombre()+" acaba de reservar\n";
        contenido+="Tipo de menú: "+dataModel.getTipomenu()+"\n";
        DateTimeFormatter fmt = DateTimeFormat.forPattern("e");
        LocalDate ld = LocalDate.parse(dataModel.getFecha());
        int hoy = Integer.parseInt(ld.toString(fmt));
        int reservaday=0;
        switch (dia){
            case "Lunes":{
                reservaday=1;
            }break;
            case "Martes":{
                reservaday=2;
            }break;
            case "Miércoles":{
                reservaday=3;
            }break;
            case "Jueves":{
                reservaday=4;
            }break;
            case "Viernes":{
                reservaday=5;
            }break;
        }
        LocalDate ld2 = ld.plusDays(reservaday-hoy);
        DateTimeFormatter fmt2 = DateTimeFormat.forPattern("EEEE dd - MMMM");
        contenido+="Para el día: "+ld2.toString(fmt2).replace("-","de")+"\n";
        contenido+="Tipo de entrega: "+dataModel.getEntrega()+"\n";
        if(!dataModel.getHoraentrega().equals("")){
            contenido+="Hora de reserva: "+dataModel.getHoraentrega()+"\n";
        }else{
            contenido+="Direccion: "+dataModel.getDireccion()+"\n";
        }
        sendEmail(getmail(dataModel.getEmpresa()),"Tiene una nueva reservacion", contenido);
        reserepository.save(dataModel);
    }

    @RequestMapping(value = "/getdayslist/{empresa}", method = RequestMethod.GET)
    public List<Dia> getdays(@PathVariable String empresa) {
        DisponibilidadModel dmodel = repository.findByEmpresa(empresa);
        List<Dia> dlist = new ArrayList<Dia>();
        Dia dia;
        if(dmodel.getLunes()>0){
            dia = new Dia();
            dia.setDia("Lunes");
            dlist.add(dia);
        }
        if(dmodel.getMartes()>0){
            dia = new Dia();
            dia.setDia("Martes");
            dlist.add(dia);
        }
        if(dmodel.getMiercoles()>0){
            dia = new Dia();
            dia.setDia("Miércoles");
            dlist.add(dia);
        }
        if(dmodel.getJueves()>0){
            dia = new Dia();
            dia.setDia("Jueves");
            dlist.add(dia);
        }
        if(dmodel.getViernes()>0){
            dia = new Dia();
            dia.setDia("Viernes");
            dlist.add(dia);
        }
        return dlist;
    }

    @RequestMapping(value = "/createhours", method = RequestMethod.POST)
    public void createhours(@Valid @RequestBody DispoHorasModel cmodel) {
        dhrepository.save(cmodel);
    }

    @RequestMapping(value = "/gethours/{empresa}/{dia}", method = RequestMethod.GET)
    public List<Horas> gethours(@PathVariable String empresa, @PathVariable String dia) {
        DispoHorasModel dmodel = dhrepository.findByEmpresaAndDia(empresa,dia);
        List<Horas> dlist = new ArrayList<Horas>();
        Horas horas;
        if(dmodel.getFranja1()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("06:40pm - 06:55pm");
            }else if (empresa.equals("Finandina"))
            {
                horas.setHoras("12:00pm-12:15pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("11:30am-11:45am");
            }else{
                horas.setHoras("10:00am - 10:15am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja2()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("06:55pm - 07:10pm");
            }else if (empresa.equals("Finandina"))
            {
                horas.setHoras("12:15pm-12:30pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("11:45am-12:00pm");
            }else{
                horas.setHoras("10:15am - 10:30am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja3()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("07:10pm - 07:25pm");
            }else if (empresa.equals("Finandina"))
            {
                horas.setHoras("12:30pm-12:45pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("12:00pm-12:15pm");
            }else{
                horas.setHoras("10:30am - 10:45am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja4()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("07:25pm - 07:40pm");
            }if (empresa.equals("Finandina"))
            {
                horas.setHoras("12:45pm-01:00pm");
            }if (empresa.equals("Protección"))
            {
                horas.setHoras("12:15pm-12:30pm");
            }else{
                horas.setHoras("10:45am - 11:00am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja5()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("07:40pm - 07:55pm");
            }else if (empresa.equals("Finandina"))
            {
                horas.setHoras("01:00pm-01:15pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("12:30pm-12:45pm");
            }else{
                horas.setHoras("11:00am - 11:15am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja6()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("07:55pm - 08:10pm");
            }else if(empresa.equals("Finandina"))
            {
                horas.setHoras("01:15pm-01:30pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("12:45pm-01:00pm");
            }else{
                horas.setHoras("11:15am - 11:30am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja7()<20){
            horas = new Horas();
            if(empresa.equals("Albahaca")){
                horas.setHoras("08:10pm - 08:25pm");
            }else if (empresa.equals("Finandina"))
            {
                horas.setHoras("01:30pm-01:45pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("01:00pm-01:15pm");
            }else{
                horas.setHoras("11:30am - 11:45am");
            }
            dlist.add(horas);
        }
        if(dmodel.getFranja8()<20){
            horas = new Horas();
            if (empresa.equals("Finandina"))
            {
                horas.setHoras("01:45pm-02:00pm");
            }else if (empresa.equals("Protección"))
            {
                horas.setHoras("01:15pm-01:30pm");
            }else if(!empresa.equals("Albahaca")){
                horas.setHoras("11:45 - 12:00pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja9()<20){
            horas = new Horas();
            if (empresa.equals("Protección"))
            {
                horas.setHoras("01:30pm-01:45pm");
            }else if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))){
                horas.setHoras("12:00pm - 12:15pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja10()<20){
            horas = new Horas();
            if (empresa.equals("Protección"))
            {
                horas.setHoras("01:45pm-02:00pm");
            }else if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))){
                horas.setHoras("12:15pm - 12:30pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja11()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("12:15pm - 12:30pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja12()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("12:45pm - 01:00pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja13()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("01:00pm - 01:15pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja14()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("01:15pm - 01:30pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja15()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("01:30pm - 01:45pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja16()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("01:45pm - 02:00pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja17()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("02:00pm - 02:15pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja18()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("02:15pm - 02:30pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja19()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("02:30pm - 02:45pm");
                dlist.add(horas);
            }
        }
        if(dmodel.getFranja20()<20){
            horas = new Horas();
            if((!empresa.equals("Albahaca"))&&(!empresa.equals("Finandina"))&&(!empresa.equals("Protección"))){
                horas.setHoras("02:45pm - 03:00pm");
                dlist.add(horas);
            }
        }
        return dlist;
    }

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

    public int setFranjaEQ(String equivalente, DispoHorasModel dmodel, DispoHorasRepository dh){
        DispoHorasModel dmodel2 = new DispoHorasModel();
        dmodel2.setId(dmodel.getEmpresa()+dmodel.getDia());
        dmodel2.setEmpresa(dmodel.getEmpresa());
        dmodel2.setDia(dmodel.getDia());
        dmodel2.setFranja1(dmodel.getFranja1());
        dmodel2.setFranja2(dmodel.getFranja2());
        dmodel2.setFranja3(dmodel.getFranja3());
        dmodel2.setFranja4(dmodel.getFranja4());
        dmodel2.setFranja5(dmodel.getFranja5());
        dmodel2.setFranja6(dmodel.getFranja6());
        dmodel2.setFranja7(dmodel.getFranja7());
        dmodel2.setFranja8(dmodel.getFranja8());
        dmodel2.setFranja9(dmodel.getFranja9());
        dmodel2.setFranja10(dmodel.getFranja10());
        dmodel2.setFranja11(dmodel.getFranja11());
        dmodel2.setFranja12(dmodel.getFranja12());
        dmodel2.setFranja13(dmodel.getFranja13());
        dmodel2.setFranja14(dmodel.getFranja14());
        dmodel2.setFranja15(dmodel.getFranja15());
        dmodel2.setFranja16(dmodel.getFranja16());
        dmodel2.setFranja17(dmodel.getFranja17());
        dmodel2.setFranja18(dmodel.getFranja18());
        dmodel2.setFranja19(dmodel.getFranja19());
        dmodel2.setFranja20(dmodel.getFranja20());
        if(equivalente.equals("10:00am - 10:15am")){
            dmodel2.setFranja1(dmodel.getFranja1()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("10:15am - 10:30am")){
            dmodel2.setFranja2(dmodel.getFranja2()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("10:30am - 10:45am")){
            dmodel2.setFranja3(dmodel.getFranja3()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("10:45am - 11:00am")){
            dmodel2.setFranja4(dmodel.getFranja4()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("11:00am - 11:15am")){
            dmodel2.setFranja5(dmodel.getFranja5()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("11:15am - 11:30am")){
            dmodel2.setFranja6(dmodel.getFranja6()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("11:30am - 11:45am")){
            dmodel2.setFranja7(dmodel.getFranja8()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("11:45 - 12:00pm")){
            dmodel2.setFranja8(dmodel.getFranja8()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("12:00pm - 12:15pm")){
            dmodel2.setFranja9(dmodel.getFranja9()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("12:15pm - 12:30pm")){
            dmodel2.setFranja10(dmodel.getFranja10()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("12:30pm - 12:45pm")){
            dmodel2.setFranja11(dmodel.getFranja11()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("12:45pm - 01:00pm")){
            dmodel2.setFranja12(dmodel.getFranja12()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("01:00pm - 01:15pm")){
            dmodel2.setFranja13(dmodel.getFranja13()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("01:15pm - 01:30pm")){
            dmodel2.setFranja14(dmodel.getFranja14()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("01:30pm - 01:45pm")){
            dmodel2.setFranja15(dmodel.getFranja15()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("01:45pm - 02:00pm")){
            dmodel2.setFranja16(dmodel.getFranja16()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("02:00pm - 02:15pm")){
            dmodel2.setFranja17(dmodel.getFranja17()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("02:15pm - 02:30pm")){
            dmodel2.setFranja18(dmodel.getFranja18()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("02:30pm - 02:45pm")){
            dmodel2.setFranja19(dmodel.getFranja19()+1);
            dh.save(dmodel2);
            return 0;
        }
        if(equivalente.equals("02:45pm - 03:00pm")){
            dmodel2.setFranja20(dmodel.getFranja20()+1);
            dh.save(dmodel2);
            return 0;
        }
        return 0;
    }

    @Scheduled(cron = "0 0 10 ? * MON", zone = "GMT-5")
    public void Nomasporlun() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        AvailaibleDays newmodel = new AvailaibleDays();
        newmodel.setid("availaible");
        newmodel.setLunes(false);
        newmodel.setMartes(true);
        newmodel.setMiercoles(true);
        newmodel.setJueves(true);
        newmodel.setViernes(true);
        arepository.delete(dmodel);
        arepository.save(newmodel);
    }
    @Scheduled(cron = "0 0 10 ? * TUE", zone = "GMT-5")
    public void Nomaspormar() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        AvailaibleDays newmodel = new AvailaibleDays();
        newmodel.setid("availaible");
        newmodel.setLunes(false);
        newmodel.setMartes(false);
        newmodel.setMiercoles(true);
        newmodel.setJueves(true);
        newmodel.setViernes(true);
        arepository.delete(dmodel);
        arepository.save(newmodel);
    }
    @Scheduled(cron = "0 0 10 ? * WED", zone = "GMT-5")
    public void Nomaspormie() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        AvailaibleDays newmodel = new AvailaibleDays();
        newmodel.setid("availaible");
        newmodel.setLunes(false);
        newmodel.setMartes(false);
        newmodel.setMiercoles(false);
        newmodel.setJueves(true);
        newmodel.setViernes(true);
        arepository.delete(dmodel);
        arepository.save(newmodel);
    }
    @Scheduled(cron = "0 0 10 ? * THU", zone = "GMT-5")
    public void Nomasporjue() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        AvailaibleDays newmodel = new AvailaibleDays();
        newmodel.setid("availaible");
        newmodel.setLunes(false);
        newmodel.setMartes(false);
        newmodel.setMiercoles(false);
        newmodel.setJueves(false);
        newmodel.setViernes(true);
        arepository.delete(dmodel);
        arepository.save(newmodel);
    }
    @Scheduled(cron = "0 0 10 ? * FRI", zone = "GMT-5")
    public void Nomasporvie() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        AvailaibleDays newmodel = new AvailaibleDays();
        newmodel.setid("availaible");
        newmodel.setLunes(false);
        newmodel.setMartes(false);
        newmodel.setMiercoles(false);
        newmodel.setJueves(false);
        newmodel.setViernes(false);
        arepository.delete(dmodel);
        arepository.save(newmodel);
    }
    @Scheduled(cron = "0 0 0 ? * MON", zone = "GMT-5")
    public void reset() {
        List<AvailaibleDays> lresult = arepository.findAll();
        AvailaibleDays dmodel = lresult.get(0);
        AvailaibleDays newmodel = new AvailaibleDays();
        newmodel.setid("availaible");
        newmodel.setLunes(true);
        newmodel.setMartes(true);
        newmodel.setMiercoles(true);
        newmodel.setJueves(true);
        newmodel.setViernes(true);
        arepository.delete(dmodel);
        arepository.save(newmodel);
    }
    @Scheduled(cron = "0 0 15 ? * FRI", zone = "GMT-5")
    public void resethours() {
        List<User> nombres = getusers();
        for(int i =0; i<nombres.size(); i++){
            if(!nombres.get(i).getNombre().equals("Administrador")){
                DispoHorasModel dmodel;
                DispoHorasModel dmodel2;
                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Lunes");
                dmodel2 = new DispoHorasModel();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Lunes");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Martes");
                dmodel2 = new DispoHorasModel();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Martes");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Miércoles");
                dmodel2 = new DispoHorasModel();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Miércoles");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Jueves");
                dmodel2 = new DispoHorasModel();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Jueves");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Viernes");
                dmodel2 = new DispoHorasModel();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Viernes");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);
            }
        }
    }
}

