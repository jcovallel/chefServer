package com.chefserver.demo.controller;

import com.chefserver.demo.model.*;
import com.chefserver.demo.ExcelDB.ExcelController;
import com.chefserver.demo.repositories.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.*;

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
    private HorariosMenusRepository hmrepository;

    @Autowired
    private dispoDiasSitioRepository ddsitiorepository;

    @Autowired
    private ListaMenusRepository lmrepository;

    @Autowired
    private ListaMenusEmpresaRepository lmerepository;

    @Autowired
    private ReservaRepository reserepository;

    @Autowired
    private AvailableRepository arepository;

    @Autowired
    private JavaMailSender javaMailSender;


    @RequestMapping(value = "/prueba",method = RequestMethod.GET )
    public ResponseEntity<byte[]> showImages () throws IOException {
        String boundary="---------THIS_IS_THE_BOUNDARY";
        List<String> imageNames = Arrays.asList(new String[]{"1.jpg"});
        List<String> contentTypes = Arrays.asList(new String[]{MediaType.IMAGE_JPEG_VALUE});
        List<Byte[]> imagesData = new ArrayList<Byte[]>();
        imagesData.add(ArrayUtils.toObject(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("Images/prueba/Menu0.jpg"))));
        //imagesData.add(ArrayUtils.toObject(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("Images/prueba/Menu1.jpg"))));
        byte[] allImages = getMultipleImageResponse(boundary, imageNames,contentTypes, imagesData);
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type","multipart/x-mixed-replace; boundary=" + boundary);
        return new ResponseEntity<byte[]>(allImages,headers, HttpStatus.CREATED);
    }

    private static byte[] getMultipleImageResponse(String boundary, List<String> imageNames, List<String> contentTypes, List<Byte[]> imagesData){
        byte[] finalByteArray = new byte[0];
        Integer imagesCounter = -1;
        for(String imageName : imageNames){
            imagesCounter++;
            String header="--" + boundary
                    + "\r\nContent-Disposition: form-data; name=\"" + imageName
                    + "\"; filename=\"" + imageName + "\"\r\n"
                    + "Content-type: " + contentTypes.get(imagesCounter) + "\r\n\r\n";
            byte[] currentImageByteArray=ArrayUtils.addAll(header.getBytes(), ArrayUtils.toPrimitive(imagesData.get(imagesCounter)));
            finalByteArray = ArrayUtils.addAll(finalByteArray,ArrayUtils.addAll(currentImageByteArray, "\r\n\r\n".getBytes()));
            if (imagesCounter == imageNames.size() - 1) {
                String end = "--" + boundary + "--";
                finalByteArray = ArrayUtils.addAll(finalByteArray, end.getBytes());
            }
        }
        return finalByteArray;
    }





    @PostMapping("/uploadmenu/{empresa}")
    public String uploadImage(@RequestParam("imageFile") MultipartFile[] imageFile, @PathVariable String empresa){
        String returnValue = "error";
        try {
            for(int i=0; i<imageFile.length; i++){
                specimenService.saveImage(imageFile[i], empresa+"?"+i);
            }
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
        if(emrepository.findByNombre(empresa).getRol()<3){
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
    public void createuser(@Valid @RequestBody Usuarios emodel) {
        emrepository.insert(emodel);
    }

    @RequestMapping(value = "/createmenu/", method = RequestMethod.POST)
    public void createmenu(@Valid @RequestBody ListaMenus menus) { lmrepository.insert(menus);}

    @RequestMapping(value = "/createhorariomenus/", method = RequestMethod.POST)
    public void createHorarioMenus(@Valid @RequestBody HorariosMenus hmenus) { hmrepository.save(hmenus);}

    @RequestMapping(value = "/createdispomenu/", method = RequestMethod.POST)
    public void createDispoMenu(@Valid @RequestBody DisponibilidadPorMenu dispomenu) { repository.save(dispomenu);}

    @RequestMapping(value = "/creatediassitio/", method = RequestMethod.POST)
    public void createDiasSitio(@Valid @RequestBody DiasDisponiblesPorSitio dds) {
        ddsitiorepository.save(dds);
    }

    @RequestMapping(value = "/menuwithempresa/", method = RequestMethod.POST)
    public void menuwithEmpresa(@Valid @RequestBody List<ListaMenusEmpresas> menus) {
        for(int i=0; i<menus.size(); i++){
            lmerepository.save(menus.get(i));
        }
    }

    @RequestMapping(value = "/modifymenu/{user}/{nmenu}", method = RequestMethod.PUT)
    public void modifymenu(@PathVariable String user, @PathVariable String nmenu) {
        ListaMenus oldmenu = lmrepository.findByMenu(user);
        ListaMenus newmenu = new ListaMenus();
        newmenu.setId(nmenu);
        newmenu.setMenu(nmenu);
        lmrepository.deleteById(user);
        lmrepository.save(newmenu);
        //ARREGLAR ESTO TILDES Y ÑS NO PASAN POR PATHVARIABLE, JSON NEEDED
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
        return "[{\"response\":"+emrepository.findByNombre(nombre).getRol()+"}]";
    }

    @RequestMapping(value = "/sendmail/{user}/{mail}/{cambio}", method = RequestMethod.GET)
    public Boolean comparemail( @PathVariable String user, @PathVariable String mail, @PathVariable Boolean cambio) throws NoSuchAlgorithmException {
        if(emrepository.findByNombre(user).getCorreo().equals(mail)){
            if(cambio){
                String tpass = passGen();
                Usuarios emodelold = emrepository.findByNombre(user);
                Usuarios emodelnew = new Usuarios();
                emodelnew.setPassword(hash(tpass));
                emodelnew.setId(emodelold.getNombre());
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
        Usuarios emodelold = emrepository.findByNombre(user);
        Usuarios emodelnew = new Usuarios();
        emodelnew.setPassword(emodelold.getPassword());
        emodelnew.setRol(emodelold.getRol());

        if(!nmail.equals("NULL")){
            if(!nnombre.equals("NULL")){
                emodelnew.setId(nnombre);
                emodelnew.setNombre(nnombre);
                emodelnew.setCorreo(nmail);
                //emrepository.save(emodelnew);
            }else{
                emodelnew.setId(emodelold.getNombre());
                emodelnew.setNombre(emodelold.getNombre());
                emodelnew.setCorreo(nmail);
                //emrepository.save(emodelnew);
            }
        }else{
            if(!nnombre.equals("NULL")){
                emodelnew.setId(nnombre);
                emodelnew.setNombre(nnombre);
                emodelnew.setCorreo(emodelold.getCorreo());
                //emrepository.save(emodelnew);
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

    @RequestMapping(value = "/getmenus/", method = RequestMethod.GET)
    public List<Menu> getmenus() {
        return lmrepository.findListaMenus();
    }

    @RequestMapping(value = "/getdispodiassitio/{empresa}", method = RequestMethod.GET)
    public List<DiasDispoSitio> getDispoDiasSitio(@PathVariable String empresa) {
        return ddsitiorepository.findListaDias(empresa);
    }

    @RequestMapping(value = "/gethorariomenu/{empresa}/{menu}", method = RequestMethod.GET)
    public List<HorarioMenusReturn> getHorarioMenu(@PathVariable String empresa, @PathVariable String menu) {
        return hmrepository.findListaHoras(empresa, menu);
    }

    @RequestMapping(value = "/getdispomenuref/{empresa}/{menu}", method = RequestMethod.GET)
    public List<DisponibilidadPorMenuRefReturn> getDisponibilidadRefMenu(@PathVariable String empresa, @PathVariable String menu) {
        return repository.findDispoRefMenus(empresa, menu);
    }

    @RequestMapping(value = "/getdispomenu/{empresa}/{menu}", method = RequestMethod.GET)
    public List<DisponibilidadPorMenuReturn> getDisponibilidadMenu(@PathVariable String empresa, @PathVariable String menu) {
        return repository.findDispoMenus(empresa, menu);
    }

    @RequestMapping(value = "/getmenusempresa/{empresa}", method = RequestMethod.GET)
    public List<MenuEmpresa> getmenusEmpresa(@PathVariable String empresa) {
        return lmerepository.findListaMenus(empresa);
    }

    @RequestMapping(value = "/getmenustrueempresa/{empresa}", method = RequestMethod.GET)
    public List<MenuTrue> getmenusTrueEmpresa(@PathVariable String empresa) {
        return lmerepository.findListaMenusTrue(empresa);
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
        Usuarios emodelold = emrepository.findByNombre(user);
        Usuarios emodelnew = new Usuarios();
        emodelnew.setId(emodelold.getNombre());
        emodelnew.setNombre(emodelold.getNombre());
        emodelnew.setRol(emodelold.getRol());

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
    public void setavadays(@Valid @RequestBody DiasDisponiblesPorSitio amodel) {
        arepository.save(amodel);
    }

    @RequestMapping(value = "/getavailabledays/", method = RequestMethod.GET)
    public List<Dia> getavadays() {
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
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

    /*@RequestMapping(value = "/disponibilidad/{empresa}/{dia}", method = RequestMethod.GET)
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
    public void modifyDispo(@Valid @RequestBody DisponibilidadPorMenu dispoModel, @PathVariable String empresa) {
        if(repository.findByEmpresa(empresa)!=null){
            DisponibilidadPorMenu current = repository.findByEmpresa(empresa);
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
    }*/

    /*@RequestMapping(value = "/disponibilidad/{empresa}", method = RequestMethod.DELETE)
    public void deleteDispo(@PathVariable String empresa) {
        repository.delete(repository.findByEmpresa(empresa));
    }*/

    /*@RequestMapping(value = "/deleteuser/{empresa}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable String empresa) {
        emrepository.deleteById(empresa);
    }*/

    @RequestMapping(value = "/deleteuser/{empresa}", method = RequestMethod.GET)
    public void deletuser(@PathVariable String empresa) {
        emrepository.deleteById(empresa);
    }

    @RequestMapping(value = "/deletedispodiassitio/{empresa}", method = RequestMethod.GET)
    public void deletDispoDiasSitio(@PathVariable String empresa) {
         ddsitiorepository.deleteById(empresa);
    }

    @RequestMapping(value = "/deletehorariomenus/{empresa}", method = RequestMethod.GET)
    public void deletHorarioMenus(@PathVariable String empresa) {
        hmrepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletelistamenusempresa/{empresa}", method = RequestMethod.GET)
    public void deletListaMenusEmpresa(@PathVariable String empresa) {
        lmerepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletedispomenu/{empresa}", method = RequestMethod.GET)
    public void deletDispoMenu(@PathVariable String empresa) {
        repository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletemenu/{menu}", method = RequestMethod.GET)
    public void deletmenu(@PathVariable String menu) {
        lmrepository.deleteById(menu);
    }

    @RequestMapping(value = "/deletecoment/{empresa}", method = RequestMethod.GET)
    public void deletcoment(@PathVariable String empresa) {
        rvrepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletedmodel/{empresa}", method = RequestMethod.GET)
    public void deletdatamodel(@PathVariable String empresa) {
        reserepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletedhmodel/{empresa}", method = RequestMethod.GET)
    public void deletdhmodel(@PathVariable String empresa) {
        dhrepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletedispomodel/{empresa}", method = RequestMethod.GET)
    public void deletdispomodel(@PathVariable String empresa) {
        repository.deleteById(empresa);
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
            DisponibilidadPorFranjaHoraria dmodel = dhrepository.findByEmpresaAndDia(dataModel.getEmpresa(),dia);
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

    /*@RequestMapping(value = "/getdayslist/{empresa}", method = RequestMethod.GET)
    public List<Dia> getdays(@PathVariable String empresa) {
        DisponibilidadPorMenu dmodel = repository.findByEmpresa(empresa);
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
    }*/

    @RequestMapping(value = "/createhours", method = RequestMethod.POST)
    public void createhours(@Valid @RequestBody DisponibilidadPorFranjaHoraria cmodel) {
        dhrepository.save(cmodel);
    }

    @RequestMapping(value = "/gethours/{empresa}/{dia}/{menu}", method = RequestMethod.GET)
    public List<Horas> gethours(@PathVariable String empresa, @PathVariable String dia, @PathVariable String menu) {
        DisponibilidadPorFranjaHoraria dmodel = dhrepository.findByEmpresaAndDia(empresa,dia);
        List<Horas> dlist = new ArrayList<Horas>();

        HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        /*Adding elements to HashMap*/
        hmap.put("12:00AM", 1);
        hmap.put("12:15AM", 2);
        hmap.put("12:30AM", 3);
        hmap.put("12:45AM", 4);
        hmap.put("01:00AM", 5);
        hmap.put("01:15AM", 6);
        hmap.put("01:30AM", 7);
        hmap.put("01:45AM", 8);
        hmap.put("02:00AM", 9);
        hmap.put("02:15AM", 10);
        hmap.put("02:30AM", 11);
        hmap.put("02:45AM", 12);
        hmap.put("03:00AM", 13);
        hmap.put("03:15AM", 14);
        hmap.put("03:30AM", 15);
        hmap.put("03:45AM", 16);
        hmap.put("04:00AM", 17);
        hmap.put("04:15AM", 18);
        hmap.put("04:30AM", 19);
        hmap.put("04:45AM", 20);
        hmap.put("05:00AM", 21);
        hmap.put("05:15AM", 22);
        hmap.put("05:30AM", 23);
        hmap.put("05:45AM", 24);
        hmap.put("06:00AM", 25);
        hmap.put("06:15AM", 26);
        hmap.put("06:30AM", 27);
        hmap.put("06:45AM", 28);
        hmap.put("07:00AM", 29);
        hmap.put("07:15AM", 30);
        hmap.put("07:30AM", 31);
        hmap.put("07:45AM", 32);
        hmap.put("08:00AM", 33);
        hmap.put("08:15AM", 34);
        hmap.put("08:30AM", 35);
        hmap.put("08:45AM", 36);
        hmap.put("09:00AM", 37);
        hmap.put("09:15AM", 38);
        hmap.put("09:30AM", 39);
        hmap.put("09:45AM", 40);
        hmap.put("10:00AM", 41);
        hmap.put("10:15AM", 42);
        hmap.put("10:30AM", 43);
        hmap.put("10:45AM", 44);
        hmap.put("11:00AM", 45);
        hmap.put("11:15AM", 46);
        hmap.put("11:30AM", 47);
        hmap.put("11:45AM", 48);
        hmap.put("12:00PM", 49);
        hmap.put("12:15PM", 50);
        hmap.put("12:30PM", 51);
        hmap.put("12:45PM", 52);
        hmap.put("01:00PM", 53);
        hmap.put("01:15PM", 54);
        hmap.put("01:30PM", 55);
        hmap.put("01:45PM", 56);
        hmap.put("02:00PM", 57);
        hmap.put("02:15PM", 58);
        hmap.put("02:30PM", 59);
        hmap.put("02:45PM", 60);
        hmap.put("03:00PM", 61);
        hmap.put("03:15PM", 62);
        hmap.put("03:30PM", 63);
        hmap.put("03:45PM", 64);
        hmap.put("04:00PM", 65);
        hmap.put("04:15PM", 66);
        hmap.put("04:30PM", 67);
        hmap.put("04:45PM", 68);
        hmap.put("05:00PM", 69);
        hmap.put("05:15PM", 70);
        hmap.put("05:30PM", 71);
        hmap.put("05:45PM", 72);
        hmap.put("06:00PM", 73);
        hmap.put("06:15PM", 74);
        hmap.put("06:30PM", 75);
        hmap.put("06:45PM", 76);
        hmap.put("07:00PM", 77);
        hmap.put("07:15PM", 78);
        hmap.put("07:30PM", 79);
        hmap.put("07:45PM", 80);
        hmap.put("08:00PM", 81);
        hmap.put("08:15PM", 82);
        hmap.put("08:30PM", 83);
        hmap.put("08:45PM", 84);
        hmap.put("09:00PM", 85);
        hmap.put("09:15PM", 86);
        hmap.put("09:30PM", 87);
        hmap.put("09:45PM", 88);
        hmap.put("10:00PM", 89);
        hmap.put("10:15PM", 90);
        hmap.put("10:30PM", 91);
        hmap.put("10:45PM", 92);
        hmap.put("11:00PM", 93);
        hmap.put("11:15PM", 94);
        hmap.put("11:30PM", 95);
        hmap.put("11:45PM", 96);

        List<HorarioMenusReturn> horarios = hmrepository.findListaHoras(empresa, menu);
        String hini = horarios.get(0).gethInicioRes();
        String hfin = horarios.get(0).gethFinRes();

        if(dmodel.getFranja1()<20 && hmap.get(hini)<=1 && hmap.get(hfin)>1 ) {
            Horas horas = new Horas();
            horas.setHoras("12:00AM - 12:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja2()<20 && hmap.get(hini)<=2 && hmap.get(hfin)>2 ) {
            Horas horas = new Horas();
            horas.setHoras("12:15AM - 12:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja3()<20 && hmap.get(hini)<=3 && hmap.get(hfin)>3 ) {
            Horas horas = new Horas();
            horas.setHoras("12:30AM - 12:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja4()<20 && hmap.get(hini)<=4 && hmap.get(hfin)>4 ) {
            Horas horas = new Horas();
            horas.setHoras("12:45AM - 01:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja5()<20 && hmap.get(hini)<=5 && hmap.get(hfin)>5 ) {
            Horas horas = new Horas();
            horas.setHoras("01:00AM - 01:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja6()<20 && hmap.get(hini)<=6 && hmap.get(hfin)>6 ) {
            Horas horas = new Horas();
            horas.setHoras("01:15AM - 01:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja7()<20 && hmap.get(hini)<=7 && hmap.get(hfin)>7 ) {
            Horas horas = new Horas();
            horas.setHoras("01:30AM - 01:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja8()<20 && hmap.get(hini)<=8 && hmap.get(hfin)>8 ) {
            Horas horas = new Horas();
            horas.setHoras("01:45AM - 02:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja9()<20 && hmap.get(hini)<=9 && hmap.get(hfin)>9 ) {
            Horas horas = new Horas();
            horas.setHoras("02:00AM - 02:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja10()<20 && hmap.get(hini)<=10 && hmap.get(hfin)>10 ) {
            Horas horas = new Horas();
            horas.setHoras("02:15AM - 02:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja11()<20 && hmap.get(hini)<=11 && hmap.get(hfin)>11 ) {
            Horas horas = new Horas();
            horas.setHoras("02:30AM - 02:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja12()<20 && hmap.get(hini)<=12 && hmap.get(hfin)>12 ) {
            Horas horas = new Horas();
            horas.setHoras("02:45AM - 03:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja13()<20 && hmap.get(hini)<=13 && hmap.get(hfin)>13 ) {
            Horas horas = new Horas();
            horas.setHoras("03:00AM - 03:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja14()<20 && hmap.get(hini)<=14 && hmap.get(hfin)>14 ) {
            Horas horas = new Horas();
            horas.setHoras("03:15AM - 03:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja15()<20 && hmap.get(hini)<=15 && hmap.get(hfin)>15 ) {
            Horas horas = new Horas();
            horas.setHoras("03:30AM - 03:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja16()<20 && hmap.get(hini)<=16 && hmap.get(hfin)>16 ) {
            Horas horas = new Horas();
            horas.setHoras("03:45AM - 04:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja17()<20 && hmap.get(hini)<=17 && hmap.get(hfin)>17 ) {
            Horas horas = new Horas();
            horas.setHoras("04:00AM - 04:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja18()<20 && hmap.get(hini)<=18 && hmap.get(hfin)>18 ) {
            Horas horas = new Horas();
            horas.setHoras("04:15AM - 04:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja19()<20 && hmap.get(hini)<=19 && hmap.get(hfin)>19 ) {
            Horas horas = new Horas();
            horas.setHoras("04:30AM - 04:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja20()<20 && hmap.get(hini)<=20 && hmap.get(hfin)>20 ) {
            Horas horas = new Horas();
            horas.setHoras("04:45AM - 05:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja21()<20 && hmap.get(hini)<=21 && hmap.get(hfin)>21 ) {
            Horas horas = new Horas();
            horas.setHoras("05:00AM - 05:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja22()<20 && hmap.get(hini)<=22 && hmap.get(hfin)>22 ) {
            Horas horas = new Horas();
            horas.setHoras("05:15AM - 05:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja23()<20 && hmap.get(hini)<=23 && hmap.get(hfin)>23 ) {
            Horas horas = new Horas();
            horas.setHoras("05:30AM - 05:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja24()<20 && hmap.get(hini)<=24 && hmap.get(hfin)>24 ) {
            Horas horas = new Horas();
            horas.setHoras("05:45AM - 06:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja25()<20 && hmap.get(hini)<=25 && hmap.get(hfin)>25 ) {
            Horas horas = new Horas();
            horas.setHoras("06:00AM - 06:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja26()<20 && hmap.get(hini)<=26 && hmap.get(hfin)>26 ) {
            Horas horas = new Horas();
            horas.setHoras("06:15AM - 06:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja27()<20 && hmap.get(hini)<=27 && hmap.get(hfin)>27 ) {
            Horas horas = new Horas();
            horas.setHoras("06:30AM - 06:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja28()<20 && hmap.get(hini)<=28 && hmap.get(hfin)>28 ) {
            Horas horas = new Horas();
            horas.setHoras("06:45AM - 07:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja29()<20 && hmap.get(hini)<=29 && hmap.get(hfin)>29 ) {
            Horas horas = new Horas();
            horas.setHoras("07:00AM - 07:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja30()<20 && hmap.get(hini)<=30 && hmap.get(hfin)>30 ) {
            Horas horas = new Horas();
            horas.setHoras("07:15AM - 07:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja31()<20 && hmap.get(hini)<=31 && hmap.get(hfin)>31 ) {
            Horas horas = new Horas();
            horas.setHoras("07:30AM - 07:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja32()<20 && hmap.get(hini)<=32 && hmap.get(hfin)>32 ) {
            Horas horas = new Horas();
            horas.setHoras("07:45AM - 08:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja33()<20 && hmap.get(hini)<=33 && hmap.get(hfin)>33 ) {
            Horas horas = new Horas();
            horas.setHoras("08:00AM - 08:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja34()<20 && hmap.get(hini)<=34 && hmap.get(hfin)>34 ) {
            Horas horas = new Horas();
            horas.setHoras("08:15AM - 08:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja35()<20 && hmap.get(hini)<=35 && hmap.get(hfin)>35 ) {
            Horas horas = new Horas();
            horas.setHoras("08:30AM - 08:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja36()<20 && hmap.get(hini)<=36 && hmap.get(hfin)>36 ) {
            Horas horas = new Horas();
            horas.setHoras("08:45AM - 09:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja37()<20 && hmap.get(hini)<=37 && hmap.get(hfin)>37 ) {
            Horas horas = new Horas();
            horas.setHoras("09:00AM - 09:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja38()<20 && hmap.get(hini)<=38 && hmap.get(hfin)>38 ) {
            Horas horas = new Horas();
            horas.setHoras("09:15AM - 09:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja39()<20 && hmap.get(hini)<=39 && hmap.get(hfin)>39 ) {
            Horas horas = new Horas();
            horas.setHoras("09:30AM - 09:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja40()<20 && hmap.get(hini)<=40 && hmap.get(hfin)>40 ) {
            Horas horas = new Horas();
            horas.setHoras("09:45AM - 10:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja41()<20 && hmap.get(hini)<=41 && hmap.get(hfin)>41 ) {
            Horas horas = new Horas();
            horas.setHoras("10:00AM - 10:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja42()<20 && hmap.get(hini)<=42 && hmap.get(hfin)>42 ) {
            Horas horas = new Horas();
            horas.setHoras("10:15AM - 10:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja43()<20 && hmap.get(hini)<=43 && hmap.get(hfin)>43 ) {
            Horas horas = new Horas();
            horas.setHoras("10:30AM - 10:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja44()<20 && hmap.get(hini)<=44 && hmap.get(hfin)>44 ) {
            Horas horas = new Horas();
            horas.setHoras("10:45AM - 11:00AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja45()<20 && hmap.get(hini)<=45 && hmap.get(hfin)>45 ) {
            Horas horas = new Horas();
            horas.setHoras("11:00AM - 11:15AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja46()<20 && hmap.get(hini)<=46 && hmap.get(hfin)>46 ) {
            Horas horas = new Horas();
            horas.setHoras("11:15AM - 11:30AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja47()<20 && hmap.get(hini)<=47 && hmap.get(hfin)>47 ) {
            Horas horas = new Horas();
            horas.setHoras("11:30AM - 11:45AM");
            dlist.add(horas);
        }
        if(dmodel.getFranja48()<20 && hmap.get(hini)<=48 && hmap.get(hfin)>48 ) {
            Horas horas = new Horas();
            horas.setHoras("11:45AM - 12:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja49()<20 && hmap.get(hini)<=49 && hmap.get(hfin)>49 ) {
            Horas horas = new Horas();
            horas.setHoras("12:00PM - 12:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja50()<20 && hmap.get(hini)<=50 && hmap.get(hfin)>50 ) {
            Horas horas = new Horas();
            horas.setHoras("12:15PM - 12:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja51()<20 && hmap.get(hini)<=51 && hmap.get(hfin)>51 ) {
            Horas horas = new Horas();
            horas.setHoras("12:30PM - 12:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja52()<20 && hmap.get(hini)<=52 && hmap.get(hfin)>52 ) {
            Horas horas = new Horas();
            horas.setHoras("12:45PM - 01:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja53()<20 && hmap.get(hini)<=53 && hmap.get(hfin)>53 ) {
            Horas horas = new Horas();
            horas.setHoras("01:00PM - 01:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja54()<20 && hmap.get(hini)<=54 && hmap.get(hfin)>54 ) {
            Horas horas = new Horas();
            horas.setHoras("01:15PM - 01:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja55()<20 && hmap.get(hini)<=55 && hmap.get(hfin)>55 ) {
            Horas horas = new Horas();
            horas.setHoras("01:30PM - 01:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja56()<20 && hmap.get(hini)<=56 && hmap.get(hfin)>56 ) {
            Horas horas = new Horas();
            horas.setHoras("01:45PM - 02:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja57()<20 && hmap.get(hini)<=57 && hmap.get(hfin)>57 ) {
            Horas horas = new Horas();
            horas.setHoras("02:00PM - 02:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja58()<20 && hmap.get(hini)<=58 && hmap.get(hfin)>58 ) {
            Horas horas = new Horas();
            horas.setHoras("02:15PM - 02:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja59()<20 && hmap.get(hini)<=59 && hmap.get(hfin)>59 ) {
            Horas horas = new Horas();
            horas.setHoras("02:30PM - 02:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja60()<20 && hmap.get(hini)<=60 && hmap.get(hfin)>60 ) {
            Horas horas = new Horas();
            horas.setHoras("02:45PM - 03:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja61()<20 && hmap.get(hini)<=61 && hmap.get(hfin)>61 ) {
            Horas horas = new Horas();
            horas.setHoras("03:00PM - 03:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja62()<20 && hmap.get(hini)<=62 && hmap.get(hfin)>62 ) {
            Horas horas = new Horas();
            horas.setHoras("03:15PM - 03:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja63()<20 && hmap.get(hini)<=63 && hmap.get(hfin)>63 ) {
            Horas horas = new Horas();
            horas.setHoras("03:30PM - 03:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja64()<20 && hmap.get(hini)<=64 && hmap.get(hfin)>64 ) {
            Horas horas = new Horas();
            horas.setHoras("03:45PM - 04:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja65()<20 && hmap.get(hini)<=65 && hmap.get(hfin)>65 ) {
            Horas horas = new Horas();
            horas.setHoras("04:00PM - 04:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja66()<20 && hmap.get(hini)<=66 && hmap.get(hfin)>66 ) {
            Horas horas = new Horas();
            horas.setHoras("04:15PM - 04:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja67()<20 && hmap.get(hini)<=67 && hmap.get(hfin)>67 ) {
            Horas horas = new Horas();
            horas.setHoras("04:30PM - 04:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja68()<20 && hmap.get(hini)<=68 && hmap.get(hfin)>68 ) {
            Horas horas = new Horas();
            horas.setHoras("04:45PM - 05:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja69()<20 && hmap.get(hini)<=69 && hmap.get(hfin)>69 ) {
            Horas horas = new Horas();
            horas.setHoras("05:00PM - 05:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja70()<20 && hmap.get(hini)<=70 && hmap.get(hfin)>70 ) {
            Horas horas = new Horas();
            horas.setHoras("05:15PM - 05:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja71()<20 && hmap.get(hini)<=71 && hmap.get(hfin)>71 ) {
            Horas horas = new Horas();
            horas.setHoras("05:30PM - 05:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja72()<20 && hmap.get(hini)<=72 && hmap.get(hfin)>72 ) {
            Horas horas = new Horas();
            horas.setHoras("05:45PM - 06:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja73()<20 && hmap.get(hini)<=73 && hmap.get(hfin)>73 ) {
            Horas horas = new Horas();
            horas.setHoras("06:00PM - 06:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja74()<20 && hmap.get(hini)<=74 && hmap.get(hfin)>74 ) {
            Horas horas = new Horas();
            horas.setHoras("06:15PM - 06:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja75()<20 && hmap.get(hini)<=75 && hmap.get(hfin)>75 ) {
            Horas horas = new Horas();
            horas.setHoras("06:30PM - 06:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja76()<20 && hmap.get(hini)<=76 && hmap.get(hfin)>76 ) {
            Horas horas = new Horas();
            horas.setHoras("06:45PM - 07:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja77()<20 && hmap.get(hini)<=77 && hmap.get(hfin)>77 ) {
            Horas horas = new Horas();
            horas.setHoras("07:00PM - 07:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja78()<20 && hmap.get(hini)<=78 && hmap.get(hfin)>78 ) {
            Horas horas = new Horas();
            horas.setHoras("07:15PM - 07:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja79()<20 && hmap.get(hini)<=79 && hmap.get(hfin)>79 ) {
            Horas horas = new Horas();
            horas.setHoras("07:30PM - 07:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja80()<20 && hmap.get(hini)<=80 && hmap.get(hfin)>80 ) {
            Horas horas = new Horas();
            horas.setHoras("07:45PM - 08:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja81()<20 && hmap.get(hini)<=81 && hmap.get(hfin)>81 ) {
            Horas horas = new Horas();
            horas.setHoras("08:00PM - 08:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja82()<20 && hmap.get(hini)<=82 && hmap.get(hfin)>82 ) {
            Horas horas = new Horas();
            horas.setHoras("08:15PM - 08:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja83()<20 && hmap.get(hini)<=83 && hmap.get(hfin)>83 ) {
            Horas horas = new Horas();
            horas.setHoras("08:30PM - 08:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja84()<20 && hmap.get(hini)<=84 && hmap.get(hfin)>84 ) {
            Horas horas = new Horas();
            horas.setHoras("08:45PM - 09:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja85()<20 && hmap.get(hini)<=85 && hmap.get(hfin)>85 ) {
            Horas horas = new Horas();
            horas.setHoras("09:00PM - 09:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja86()<20 && hmap.get(hini)<=86 && hmap.get(hfin)>86 ) {
            Horas horas = new Horas();
            horas.setHoras("09:15PM - 09:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja87()<20 && hmap.get(hini)<=87 && hmap.get(hfin)>87 ) {
            Horas horas = new Horas();
            horas.setHoras("09:30PM - 09:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja88()<20 && hmap.get(hini)<=88 && hmap.get(hfin)>88 ) {
            Horas horas = new Horas();
            horas.setHoras("09:45PM - 10:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja89()<20 && hmap.get(hini)<=89 && hmap.get(hfin)>89 ) {
            Horas horas = new Horas();
            horas.setHoras("10:00PM - 10:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja90()<20 && hmap.get(hini)<=90 && hmap.get(hfin)>90 ) {
            Horas horas = new Horas();
            horas.setHoras("10:15PM - 10:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja91()<20 && hmap.get(hini)<=91 && hmap.get(hfin)>91 ) {
            Horas horas = new Horas();
            horas.setHoras("10:30PM - 10:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja92()<20 && hmap.get(hini)<=92 && hmap.get(hfin)>92 ) {
            Horas horas = new Horas();
            horas.setHoras("10:45PM - 11:00PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja93()<20 && hmap.get(hini)<=93 && hmap.get(hfin)>93 ) {
            Horas horas = new Horas();
            horas.setHoras("11:00PM - 11:15PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja94()<20 && hmap.get(hini)<=94 && hmap.get(hfin)>94 ) {
            Horas horas = new Horas();
            horas.setHoras("11:15PM - 11:30PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja95()<20 && hmap.get(hini)<=95 && hmap.get(hfin)>95 ) {
            Horas horas = new Horas();
            horas.setHoras("11:30PM - 11:45PM");
            dlist.add(horas);
        }
        if(dmodel.getFranja96()<20 && hmap.get(hini)<=96 && hmap.get(hfin)>96 ) {
            Horas horas = new Horas();
            horas.setHoras("11:45PM - 12:00AM");
            dlist.add(horas);
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

    public int setFranjaEQ(String equivalente, DisponibilidadPorFranjaHoraria dmodel, DispoHorasRepository dh){
        DisponibilidadPorFranjaHoraria dmodel2 = new DisponibilidadPorFranjaHoraria();
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
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
        DiasDisponiblesPorSitio newmodel = new DiasDisponiblesPorSitio();
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
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
        DiasDisponiblesPorSitio newmodel = new DiasDisponiblesPorSitio();
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
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
        DiasDisponiblesPorSitio newmodel = new DiasDisponiblesPorSitio();
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
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
        DiasDisponiblesPorSitio newmodel = new DiasDisponiblesPorSitio();
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
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
        DiasDisponiblesPorSitio newmodel = new DiasDisponiblesPorSitio();
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
        List<DiasDisponiblesPorSitio> lresult = arepository.findAll();
        DiasDisponiblesPorSitio dmodel = lresult.get(0);
        DiasDisponiblesPorSitio newmodel = new DiasDisponiblesPorSitio();
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
                DisponibilidadPorFranjaHoraria dmodel;
                DisponibilidadPorFranjaHoraria dmodel2;
                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Lunes");
                dmodel2 = new DisponibilidadPorFranjaHoraria();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Lunes");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Martes");
                dmodel2 = new DisponibilidadPorFranjaHoraria();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Martes");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Miércoles");
                dmodel2 = new DisponibilidadPorFranjaHoraria();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Miércoles");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Jueves");
                dmodel2 = new DisponibilidadPorFranjaHoraria();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Jueves");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);

                dmodel = dhrepository.findByEmpresaAndDia(nombres.get(i).getNombre(),"Viernes");
                dmodel2 = new DisponibilidadPorFranjaHoraria();
                dmodel2.setId(dmodel.getId());
                dmodel2.setEmpresa(dmodel.getEmpresa());
                dmodel2.setDia("Viernes");
                dhrepository.delete(dmodel);
                dhrepository.save(dmodel2);
            }
        }
    }
}

