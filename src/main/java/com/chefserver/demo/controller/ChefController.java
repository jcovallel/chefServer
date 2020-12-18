package com.chefserver.demo.controller;

import com.chefserver.demo.model.*;
import com.chefserver.demo.ExcelDB.ExcelController;
import com.chefserver.demo.repositories.*;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/chef")
public class ChefController {
    @Autowired
    private ISpecimenService specimenService;

    @Autowired
    private DisponibilidadPorMenuRepo disponibilidadPorMenuRepo;

    @Autowired
    private DisponibilidadFranjaHoraRepo disponibilidadFranjaHoraRepo;

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
        //String dirPath = "DatosExcel/";
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
    public void createDispoMenu(@Valid @RequestBody DisponibilidadPorMenu dispomenu) { disponibilidadPorMenuRepo.save(dispomenu);}

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
/*#################################################################################################################################
##################################################  MODIFIERS  ####################################################################
#################################################################################################################################*/

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  PARA MENUS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    @RequestMapping(value = "/modifymenu/", method = RequestMethod.POST)
    public void modifymenu(@Valid @RequestBody ListaMenus menus) {
        lmrepository.deleteById(menus.getId());
        ListaMenus newmenu = new ListaMenus();
        newmenu.setId(menus.getMenu());
        newmenu.setMenu(menus.getMenu());
        lmrepository.save(newmenu);
    }

    @RequestMapping(value = "/modifyhorariomenus/", method = RequestMethod.POST)
    public void modifyHorarioMenus(@Valid @RequestBody HorariosMenus hmenus) {
        HorariosMenus oldHM = hmrepository.findById(hmenus.getId()).orElse(null);
        HorariosMenus newHM = new HorariosMenus();
        newHM.setId(hmenus.getEmpresa()+hmenus.getMenu());
        newHM.setEmpresa(hmenus.getEmpresa());
        newHM.setMenu(hmenus.getMenu());
        newHM.sethInicioRes(oldHM.gethInicioRes());
        newHM.sethFinRes(oldHM.gethFinRes());
        newHM.sethInicioEnt(oldHM.gethInicioEnt());
        newHM.sethFinEnt(oldHM.gethFinEnt());
        hmrepository.save(newHM);
        hmrepository.deleteById(hmenus.getId());
    }

    @RequestMapping(value = "/modifydispomenu/", method = RequestMethod.POST)
    public void modifyDispoMenu(@Valid @RequestBody DisponibilidadPorMenu dispomenu) {
        DisponibilidadPorMenu oldDM = disponibilidadPorMenuRepo.findById(dispomenu.getId()).orElse(null);
        DisponibilidadPorMenu newDM = new DisponibilidadPorMenu();
        newDM.setId(dispomenu.getEmpresa()+dispomenu.getMenu());
        newDM.setEmpresa(dispomenu.getEmpresa());
        newDM.setMenu(dispomenu.getMenu());
        newDM.setLunesref(oldDM.getLunesref());
        newDM.setMartesref(oldDM.getMartesref());
        newDM.setMiercolesref(oldDM.getMiercolesref());
        newDM.setJuevesref(oldDM.getJuevesref());
        newDM.setViernesref(oldDM.getViernesref());
        newDM.setSabadoref(oldDM.getSabadoref());
        newDM.setDomingoref(oldDM.getDomingoref());
        newDM.setLunes(oldDM.getLunes());
        newDM.setMartes(oldDM.getMartes());
        newDM.setMiercoles(oldDM.getMiercoles());
        newDM.setJueves(oldDM.getJueves());
        newDM.setViernes(oldDM.getViernes());
        newDM.setSabado(oldDM.getSabado());
        newDM.setDomingo(oldDM.getDomingo());
        disponibilidadPorMenuRepo.save(newDM);
        disponibilidadPorMenuRepo.deleteById(dispomenu.getId());
    }

    @RequestMapping(value = "/modifymenuempresa/", method = RequestMethod.POST)
    public void modifyMenuEmpresa(@Valid @RequestBody ListaMenusEmpresas menus) {
        ListaMenusEmpresas oldME = lmerepository.findById(menus.getId()).orElse(null);
        ListaMenusEmpresas newME = new ListaMenusEmpresas();
        newME.setId(menus.getEmpresa()+menus.getMenu());
        newME.setEmpresa(menus.getEmpresa());
        newME.setMenu(menus.getMenu());
        newME.setCheck(oldME.getCheck());
        lmerepository.deleteById(menus.getId());
        lmerepository.save(newME);
    }

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  PARA USUARIOS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    @RequestMapping(value = "/modifyimgnum/", method = RequestMethod.POST)
    public void modifyimgnum(@Valid @RequestBody Usuarios emodel) {
        Usuarios olduser = emrepository.findById(emodel.getId()).orElse(null);
        Usuarios newuser = new Usuarios();
        newuser.setId(olduser.getId());
        newuser.setNombre(olduser.getNombre());
        newuser.setCorreo(olduser.getCorreo());
        newuser.setPassword(olduser.getPassword());
        newuser.setRol(olduser.getRol());
        newuser.setImgnum(emodel.getImgnum());
        emrepository.deleteById(emodel.getId());
        emrepository.save(newuser);
    }


    @RequestMapping(value = "/modifyinfoadmi/", method = RequestMethod.POST)
    public void modifyinfoadmi(@Valid @RequestBody Usuarios emodel) {
        Usuarios olduser = emrepository.findById(emodel.getId()).orElse(null);
        Usuarios newuser = new Usuarios();
        newuser.setPassword(olduser.getPassword());
        newuser.setRol(olduser.getRol());
        newuser.setImgnum(olduser.getImgnum());

        if(!emodel.getCorreo().equals("NULL")){
            if(!emodel.getNombre().equals("NULL")){
                newuser.setId(emodel.getNombre());
                newuser.setNombre(emodel.getNombre());
                newuser.setCorreo(emodel.getCorreo());
                if(olduser.getImgnum()>0){
                    //for local
                    //File sourceFile = new File("src/main/resources/Images/"+emodel.getId());
                    //File dest = new File("src/main/resources/Images/"+emodel.getNombre());
                    //for gcloud
                    File sourceFile = new File("../src/main/resources/Images/"+emodel.getId());
                    File dest = new File("../src/main/resources/Images/"+emodel.getNombre());
                    sourceFile.renameTo(dest);
                }
            }else{
                newuser.setId(olduser.getNombre());
                newuser.setNombre(olduser.getNombre());
                newuser.setCorreo(emodel.getCorreo());
            }
        }else{
            if(!emodel.getNombre().equals("NULL")){
                newuser.setId(emodel.getNombre());
                newuser.setNombre(emodel.getNombre());
                newuser.setCorreo(olduser.getCorreo());
                if(olduser.getImgnum()>0){
                    //for local
                    //File sourceFile = new File("src/main/resources/Images/"+emodel.getId());
                    //File dest = new File("src/main/resources/Images/"+emodel.getNombre());
                    //for gcloud
                    File sourceFile = new File("../src/main/resources/Images/"+emodel.getId());
                    File dest = new File("../src/main/resources/Images/"+emodel.getNombre());
                    sourceFile.renameTo(dest);
                }
            }
        }
        emrepository.deleteById(emodel.getId());
        emrepository.save(newuser);
    }

    @RequestMapping(value = "/modifydiassitio/", method = RequestMethod.POST)
    public void modifyDiasSitio(@Valid @RequestBody DiasDisponiblesPorSitio dds) {
        DiasDisponiblesPorSitio olddds = ddsitiorepository.findById(dds.getid()).orElse(null);
        DiasDisponiblesPorSitio newdds = new DiasDisponiblesPorSitio();
        newdds.setid(dds.getEmpresa());
        newdds.setEmpresa(dds.getEmpresa());
        newdds.setLunes(olddds.getLunes());
        newdds.setMartes(olddds.getMartes());
        newdds.setMiercoles(olddds.getMiercoles());
        newdds.setJueves(olddds.getJueves());
        newdds.setViernes(olddds.getViernes());
        newdds.setSabado(olddds.getSabado());
        newdds.setDomingo(olddds.getDomingo());
        ddsitiorepository.save(newdds);
        ddsitiorepository.deleteById(dds.getid());
    }

    @RequestMapping(value = "/modifyhours", method = RequestMethod.POST)
    public void modifyhours(@Valid @RequestBody DisponibilidadPorFranjaHoraria cmodel) {
        DisponibilidadPorFranjaHoraria oldDpfh = disponibilidadFranjaHoraRepo.findById(cmodel.getId()).orElse(null);
        DisponibilidadPorFranjaHoraria newDpfh = new DisponibilidadPorFranjaHoraria();
        newDpfh.setId(cmodel.getEmpresa()+cmodel.getDia());
        newDpfh.setEmpresa(cmodel.getEmpresa());
        newDpfh.setDia(cmodel.getDia());
        newDpfh.setFranja1(oldDpfh.getFranja1());
        newDpfh.setFranja2(oldDpfh.getFranja2());
        newDpfh.setFranja3(oldDpfh.getFranja3());
        newDpfh.setFranja4(oldDpfh.getFranja4());
        newDpfh.setFranja5(oldDpfh.getFranja5());
        newDpfh.setFranja6(oldDpfh.getFranja6());
        newDpfh.setFranja7(oldDpfh.getFranja7());
        newDpfh.setFranja8(oldDpfh.getFranja8());
        newDpfh.setFranja9(oldDpfh.getFranja9());
        newDpfh.setFranja10(oldDpfh.getFranja10());
        newDpfh.setFranja11(oldDpfh.getFranja11());
        newDpfh.setFranja12(oldDpfh.getFranja12());
        newDpfh.setFranja13(oldDpfh.getFranja13());
        newDpfh.setFranja14(oldDpfh.getFranja14());
        newDpfh.setFranja15(oldDpfh.getFranja15());
        newDpfh.setFranja16(oldDpfh.getFranja16());
        newDpfh.setFranja17(oldDpfh.getFranja17());
        newDpfh.setFranja18(oldDpfh.getFranja18());
        newDpfh.setFranja19(oldDpfh.getFranja19());
        newDpfh.setFranja20(oldDpfh.getFranja20());
        newDpfh.setFranja21(oldDpfh.getFranja21());
        newDpfh.setFranja22(oldDpfh.getFranja22());
        newDpfh.setFranja23(oldDpfh.getFranja23());
        newDpfh.setFranja24(oldDpfh.getFranja24());
        newDpfh.setFranja25(oldDpfh.getFranja25());
        newDpfh.setFranja26(oldDpfh.getFranja26());
        newDpfh.setFranja27(oldDpfh.getFranja27());
        newDpfh.setFranja28(oldDpfh.getFranja28());
        newDpfh.setFranja29(oldDpfh.getFranja29());
        newDpfh.setFranja30(oldDpfh.getFranja30());
        newDpfh.setFranja31(oldDpfh.getFranja31());
        newDpfh.setFranja32(oldDpfh.getFranja32());
        newDpfh.setFranja33(oldDpfh.getFranja33());
        newDpfh.setFranja34(oldDpfh.getFranja34());
        newDpfh.setFranja35(oldDpfh.getFranja35());
        newDpfh.setFranja36(oldDpfh.getFranja36());
        newDpfh.setFranja37(oldDpfh.getFranja37());
        newDpfh.setFranja38(oldDpfh.getFranja38());
        newDpfh.setFranja39(oldDpfh.getFranja39());
        newDpfh.setFranja40(oldDpfh.getFranja40());
        newDpfh.setFranja41(oldDpfh.getFranja41());
        newDpfh.setFranja42(oldDpfh.getFranja42());
        newDpfh.setFranja43(oldDpfh.getFranja43());
        newDpfh.setFranja44(oldDpfh.getFranja44());
        newDpfh.setFranja45(oldDpfh.getFranja45());
        newDpfh.setFranja46(oldDpfh.getFranja46());
        newDpfh.setFranja47(oldDpfh.getFranja47());
        newDpfh.setFranja48(oldDpfh.getFranja48());
        newDpfh.setFranja49(oldDpfh.getFranja49());
        newDpfh.setFranja50(oldDpfh.getFranja50());
        newDpfh.setFranja51(oldDpfh.getFranja51());
        newDpfh.setFranja52(oldDpfh.getFranja52());
        newDpfh.setFranja53(oldDpfh.getFranja53());
        newDpfh.setFranja54(oldDpfh.getFranja54());
        newDpfh.setFranja55(oldDpfh.getFranja55());
        newDpfh.setFranja56(oldDpfh.getFranja56());
        newDpfh.setFranja57(oldDpfh.getFranja57());
        newDpfh.setFranja58(oldDpfh.getFranja58());
        newDpfh.setFranja59(oldDpfh.getFranja59());
        newDpfh.setFranja60(oldDpfh.getFranja60());
        newDpfh.setFranja61(oldDpfh.getFranja61());
        newDpfh.setFranja62(oldDpfh.getFranja62());
        newDpfh.setFranja63(oldDpfh.getFranja63());
        newDpfh.setFranja64(oldDpfh.getFranja64	());
        newDpfh.setFranja65(oldDpfh.getFranja65());
        newDpfh.setFranja66(oldDpfh.getFranja66());
        newDpfh.setFranja67(oldDpfh.getFranja67());
        newDpfh.setFranja68(oldDpfh.getFranja68());
        newDpfh.setFranja69(oldDpfh.getFranja69());
        newDpfh.setFranja70(oldDpfh.getFranja70());
        newDpfh.setFranja71(oldDpfh.getFranja71());
        newDpfh.setFranja72(oldDpfh.getFranja72());
        newDpfh.setFranja73(oldDpfh.getFranja73());
        newDpfh.setFranja74(oldDpfh.getFranja74());
        newDpfh.setFranja75(oldDpfh.getFranja75());
        newDpfh.setFranja76(oldDpfh.getFranja76());
        newDpfh.setFranja77(oldDpfh.getFranja77());
        newDpfh.setFranja78(oldDpfh.getFranja78());
        newDpfh.setFranja79(oldDpfh.getFranja79());
        newDpfh.setFranja80(oldDpfh.getFranja80());
        newDpfh.setFranja81(oldDpfh.getFranja81());
        newDpfh.setFranja82(oldDpfh.getFranja82());
        newDpfh.setFranja83(oldDpfh.getFranja83());
        newDpfh.setFranja84(oldDpfh.getFranja84());
        newDpfh.setFranja85(oldDpfh.getFranja85());
        newDpfh.setFranja86(oldDpfh.getFranja86());
        newDpfh.setFranja87(oldDpfh.getFranja87());
        newDpfh.setFranja88(oldDpfh.getFranja88());
        newDpfh.setFranja89(oldDpfh.getFranja89());
        newDpfh.setFranja90(oldDpfh.getFranja90());
        newDpfh.setFranja91(oldDpfh.getFranja91());
        newDpfh.setFranja92(oldDpfh.getFranja92());
        newDpfh.setFranja93(oldDpfh.getFranja93());
        newDpfh.setFranja94(oldDpfh.getFranja94());
        newDpfh.setFranja95(oldDpfh.getFranja95());
        newDpfh.setFranja96(oldDpfh.getFranja96());
        disponibilidadFranjaHoraRepo.save(newDpfh);
        disponibilidadFranjaHoraRepo.deleteById(cmodel.getId());
    }
/*################################################################################################################################
#################################################################################################################################*/

//======================================================================================================

//======================================== DELETERS ==================================================
    @RequestMapping(value = "/deletemenu/{menu}", method = RequestMethod.GET)
    public void deletmenu(@PathVariable String menu) {
        lmrepository.deleteById(menu);
    }

    @RequestMapping(value = "/deletehorariomenusbymenu/{menu}", method = RequestMethod.GET)
    public void deletHorarioMenusByMenu(@PathVariable String menu) {
        hmrepository.deleteByMenu(menu);
    }

    @RequestMapping(value = "/deletedispomenubymenu/{menu}", method = RequestMethod.GET)
    public void deletDispoMenuByMenu(@PathVariable String menu) {
        disponibilidadPorMenuRepo.deleteByMenu(menu);
    }

    @RequestMapping(value = "/deletelistamenusempresabymenu/{menu}", method = RequestMethod.GET)
    public void deletListaMenusEmpresaByMenu(@PathVariable String menu) {
        lmerepository.deleteByMenu(menu);
    }

// ======================================================================================================

    @RequestMapping(value = "/deletehorariomenus/{empresa}", method = RequestMethod.GET)
    public void deletHorarioMenus(@PathVariable String empresa) {
        hmrepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletedispomenu/{empresa}", method = RequestMethod.GET)
    public void deletDispoMenu(@PathVariable String empresa) {
        disponibilidadPorMenuRepo.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deletelistamenusempresa/{empresa}", method = RequestMethod.GET)
    public void deletListaMenusEmpresa(@PathVariable String empresa) {
        lmerepository.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/deleteuser/{empresa}", method = RequestMethod.GET)
    public void deletuser(@PathVariable String empresa) {
        emrepository.deleteById(empresa);
    }

    @RequestMapping(value = "/deletedispodiassitio/{empresa}", method = RequestMethod.GET)
    public void deletDispoDiasSitio(@PathVariable String empresa) {
        ddsitiorepository.deleteById(empresa);
    }

    @RequestMapping(value = "/deletedispofranjah/{empresa}", method = RequestMethod.GET)
    public void deletDispoFranjaH(@PathVariable String empresa) {
        disponibilidadFranjaHoraRepo.deleteByEmpresa(empresa);
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
                emodelnew.setRol(emodelold.getRol());
                emodelnew.setImgnum(emodelold.getImgnum());
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

    @RequestMapping(value = "/getusers/", method = RequestMethod.GET)
    public List<Usuarios> getusers() {
        return emrepository.findNameAndExcludeId();
    }

    @RequestMapping(value = "/getimgnum/{empresa}", method = RequestMethod.GET)
    public List<Usuarios> getimgnum(@PathVariable String empresa) {
        List<Usuarios> returnList = emrepository.findImgnum(empresa);
        returnList.add(emrepository.findImgnumTips().get(0));
        returnList.add(emrepository.findAdminName().get(0));
        return returnList;
    }

    @RequestMapping(value = "/getadminname/", method = RequestMethod.GET)
    public List<Usuarios> getAdminName() {
        return emrepository.findAdminName();
    }

    @RequestMapping(value = "/getusersmobile/", method = RequestMethod.GET)
    public List<Usuarios> getusersmobile() {
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
    public List<DisponibilidadPorMenu> getDisponibilidadRefMenu(@PathVariable String empresa, @PathVariable String menu) {
        return disponibilidadPorMenuRepo.findDispoRefMenus(empresa, menu);
    }

    @RequestMapping(value = "/getdispomenu/{empresa}/{menu}", method = RequestMethod.GET)
    public List<DisponibilidadPorMenu> getDisponibilidadMenu(@PathVariable String empresa, @PathVariable String menu) {
        return disponibilidadPorMenuRepo.findDispoMenus(empresa, menu);
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

    @RequestMapping(value = "/modifydatausers/", method = RequestMethod.POST)
    public void modifydatauser(@Valid @RequestBody Usuarios emodel) {
        Usuarios olduser = emrepository.findById(emodel.getId()).orElse(null);
        Usuarios newuser = new Usuarios();
        newuser.setId(olduser.getId());
        newuser.setNombre(olduser.getNombre());
        newuser.setRol(olduser.getRol());
        newuser.setImgnum(olduser.getImgnum());
        if(!emodel.getCorreo().equals("NULL")){
            if(!emodel.getPassword().equals("NULL")){
                newuser.setPassword(emodel.getPassword());
                newuser.setCorreo(emodel.getCorreo());
            }else{
                newuser.setPassword(olduser.getPassword());
                newuser.setCorreo(emodel.getCorreo());
            }
        }else{
            if(!emodel.getPassword().equals("NULL")){
                newuser.setPassword(emodel.getPassword());
                newuser.setCorreo(olduser.getCorreo());
            }
        }
        emrepository.deleteById(emodel.getId());
        emrepository.save(newuser);
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
        disponibilidadFranjaHoraRepo.deleteByEmpresa(empresa);
    }

    @RequestMapping(value = "/getmail/{empresa}", method = RequestMethod.GET)
    public String getmail(@PathVariable String empresa) {
        return emrepository.findByNombre(empresa).getCorreo();
    }

    @RequestMapping(value = "/aredisponow/{empresa}", method = RequestMethod.GET)
    public boolean areDispoNow(@PathVariable String empresa) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentHour = dateFormat.format(new Date());
        List<MenuTrue> menuList;
        menuList = lmerepository.findListaMenusTrue(empresa);
        List<HorarioMenusReturn> horarioMenu;
        for(int i=0; i<menuList.size();i++){
            horarioMenu = hmrepository.findListaHoras(empresa, menuList.get(i).getMenu());
            String horaIni = horarioMenu.get(0).gethInicioRes().substring(0,2);
            int horaInit = Integer.parseInt(horaIni);
            if(horaInit==12){ horaInit=0; }
            String minutoIni = horarioMenu.get(0).gethInicioRes().substring(3,5);
            int minInit = Integer.parseInt(minutoIni);
            String amIni = horarioMenu.get(0).gethInicioRes().substring(5);
            String horaFin = horarioMenu.get(0).gethFinRes().substring(0,2);
            int horaFi = Integer.parseInt(horaFin);
            String minutoFin = horarioMenu.get(0).gethFinRes().substring(3,5);
            int minFi = Integer.parseInt(minutoFin);
            String amFin = horarioMenu.get(0).gethFinRes().substring(5);

            //CAMBIO A 24
            if(amIni.equals("PM")){ horaInit+=12; }
            if(amFin.equals("PM")){ horaFi+=12; }

            //COMPROBACION BETWEEN
            int curretH = Integer.parseInt(currentHour.substring(0,2));
            int currentmin = Integer.parseInt(currentHour.substring(3));
            if(curretH>horaInit && curretH<horaFi){
                return true;
            }else {
                if(curretH==horaInit){
                    if(currentmin>=minInit){
                        return true;
                    }
                }else {
                    if(curretH==horaFi) {
                        if(currentmin<=minFi){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @RequestMapping(value = "/getmenusnowempresa/{empresa}", method = RequestMethod.GET)
    public List<MenuTrue> getmenusNowEmpresa(@PathVariable String empresa) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentHour = dateFormat.format(new Date());
        List<MenuTrue> menuList;
        List<MenuTrue> menuList2 = new ArrayList<>();
        menuList = lmerepository.findListaMenusTrue(empresa);
        List<HorarioMenusReturn> horarioMenu;
        for(int i=0; i<menuList.size();i++){
            MenuTrue menuAdd = new MenuTrue();
            horarioMenu = hmrepository.findListaHoras(empresa, menuList.get(i).getMenu());
            String horaIni = horarioMenu.get(0).gethInicioRes().substring(0,2);
            int horaInit = Integer.parseInt(horaIni);
            if(horaInit==12){ horaInit=0; }
            String minutoIni = horarioMenu.get(0).gethInicioRes().substring(3,5);
            int minInit = Integer.parseInt(minutoIni);
            String amIni = horarioMenu.get(0).gethInicioRes().substring(5);
            String horaFin = horarioMenu.get(0).gethFinRes().substring(0,2);
            int horaFi = Integer.parseInt(horaFin);
            String minutoFin = horarioMenu.get(0).gethFinRes().substring(3,5);
            int minFi = Integer.parseInt(minutoFin);
            String amFin = horarioMenu.get(0).gethFinRes().substring(5);

            //CAMBIO A 24
            if(amIni.equals("PM")){ horaInit+=12; }
            if(amFin.equals("PM")){ horaFi+=12; }

            //COMPROBACION BETWEEN
            int curretH = Integer.parseInt(currentHour.substring(0,2));
            int currentmin = Integer.parseInt(currentHour.substring(3));
            if(curretH>horaInit && curretH<horaFi){
                menuAdd.setMenu(menuList.get(i).getMenu());
                menuList2.add(menuAdd);
            }else {
                if(curretH==horaInit){
                    if(currentmin>=minInit){
                        menuAdd.setMenu(menuList.get(i).getMenu());
                        menuList2.add(menuAdd);
                    }
                }else {
                    if(curretH==horaFi) {
                        if(currentmin<=minFi){
                            menuAdd.setMenu(menuList.get(i).getMenu());
                            menuList2.add(menuAdd);
                        }
                    }
                }
            }
        }
        return menuList2;
    }

    @RequestMapping(value = "/reserva/save/{provisional}", method = RequestMethod.POST)
    public void createReservationREG(@Valid @RequestBody DataModel dataModel, @PathVariable String provisional) {
        String dia = provisional.replace(dataModel.getEmpresa(),"").toLowerCase();
        String hora = dataModel.getHoraentrega().substring(0,7);
        //=====================================
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
        //==========================================================
        int num = hmap.get(hora);
        DisponibilidadPorFranjaHoraria dpfh = disponibilidadFranjaHoraRepo.findByEmpresaAndDia(dataModel.getEmpresa(),dia);

        switch (num){
            case 1	:	{	dpfh.setFranja1	(	dpfh.getFranja1	()+1);}break;
            case 2	:	{	dpfh.setFranja2	(	dpfh.getFranja2	()+1);}break;
            case 3	:	{	dpfh.setFranja3	(	dpfh.getFranja3	()+1);}break;
            case 4	:	{	dpfh.setFranja4	(	dpfh.getFranja4	()+1);}break;
            case 5	:	{	dpfh.setFranja5	(	dpfh.getFranja5	()+1);}break;
            case 6	:	{	dpfh.setFranja6	(	dpfh.getFranja6	()+1);}break;
            case 7	:	{	dpfh.setFranja7	(	dpfh.getFranja7	()+1);}break;
            case 8	:	{	dpfh.setFranja8	(	dpfh.getFranja8	()+1);}break;
            case 9	:	{	dpfh.setFranja9	(	dpfh.getFranja9	()+1);}break;
            case 10	:	{	dpfh.setFranja10	(	dpfh.getFranja10	()+1);}break;
            case 11	:	{	dpfh.setFranja11	(	dpfh.getFranja11	()+1);}break;
            case 12	:	{	dpfh.setFranja12	(	dpfh.getFranja12	()+1);}break;
            case 13	:	{	dpfh.setFranja13	(	dpfh.getFranja13	()+1);}break;
            case 14	:	{	dpfh.setFranja14	(	dpfh.getFranja14	()+1);}break;
            case 15	:	{	dpfh.setFranja15	(	dpfh.getFranja15	()+1);}break;
            case 16	:	{	dpfh.setFranja16	(	dpfh.getFranja16	()+1);}break;
            case 17	:	{	dpfh.setFranja17	(	dpfh.getFranja17	()+1);}break;
            case 18	:	{	dpfh.setFranja18	(	dpfh.getFranja18	()+1);}break;
            case 19	:	{	dpfh.setFranja19	(	dpfh.getFranja19	()+1);}break;
            case 20	:	{	dpfh.setFranja20	(	dpfh.getFranja20	()+1);}break;
            case 21	:	{	dpfh.setFranja21	(	dpfh.getFranja21	()+1);}break;
            case 22	:	{	dpfh.setFranja22	(	dpfh.getFranja22	()+1);}break;
            case 23	:	{	dpfh.setFranja23	(	dpfh.getFranja23	()+1);}break;
            case 24	:	{	dpfh.setFranja24	(	dpfh.getFranja24	()+1);}break;
            case 25	:	{	dpfh.setFranja25	(	dpfh.getFranja25	()+1);}break;
            case 26	:	{	dpfh.setFranja26	(	dpfh.getFranja26	()+1);}break;
            case 27	:	{	dpfh.setFranja27	(	dpfh.getFranja27	()+1);}break;
            case 28	:	{	dpfh.setFranja28	(	dpfh.getFranja28	()+1);}break;
            case 29	:	{	dpfh.setFranja29	(	dpfh.getFranja29	()+1);}break;
            case 30	:	{	dpfh.setFranja30	(	dpfh.getFranja30	()+1);}break;
            case 31	:	{	dpfh.setFranja31	(	dpfh.getFranja31	()+1);}break;
            case 32	:	{	dpfh.setFranja32	(	dpfh.getFranja32	()+1);}break;
            case 33	:	{	dpfh.setFranja33	(	dpfh.getFranja33	()+1);}break;
            case 34	:	{	dpfh.setFranja34	(	dpfh.getFranja34	()+1);}break;
            case 35	:	{	dpfh.setFranja35	(	dpfh.getFranja35	()+1);}break;
            case 36	:	{	dpfh.setFranja36	(	dpfh.getFranja36	()+1);}break;
            case 37	:	{	dpfh.setFranja37	(	dpfh.getFranja37	()+1);}break;
            case 38	:	{	dpfh.setFranja38	(	dpfh.getFranja38	()+1);}break;
            case 39	:	{	dpfh.setFranja39	(	dpfh.getFranja39	()+1);}break;
            case 40	:	{	dpfh.setFranja40	(	dpfh.getFranja40	()+1);}break;
            case 41	:	{	dpfh.setFranja41	(	dpfh.getFranja41	()+1);}break;
            case 42	:	{	dpfh.setFranja42	(	dpfh.getFranja42	()+1);}break;
            case 43	:	{	dpfh.setFranja43	(	dpfh.getFranja43	()+1);}break;
            case 44	:	{	dpfh.setFranja44	(	dpfh.getFranja44	()+1);}break;
            case 45	:	{	dpfh.setFranja45	(	dpfh.getFranja45	()+1);}break;
            case 46	:	{	dpfh.setFranja46	(	dpfh.getFranja46	()+1);}break;
            case 47	:	{	dpfh.setFranja47	(	dpfh.getFranja47	()+1);}break;
            case 48	:	{	dpfh.setFranja48	(	dpfh.getFranja48	()+1);}break;
            case 49	:	{	dpfh.setFranja49	(	dpfh.getFranja49	()+1);}break;
            case 50	:	{	dpfh.setFranja50	(	dpfh.getFranja50	()+1);}break;
            case 51	:	{	dpfh.setFranja51	(	dpfh.getFranja51	()+1);}break;
            case 52	:	{	dpfh.setFranja52	(	dpfh.getFranja52	()+1);}break;
            case 53	:	{	dpfh.setFranja53	(	dpfh.getFranja53	()+1);}break;
            case 54	:	{	dpfh.setFranja54	(	dpfh.getFranja54	()+1);}break;
            case 55	:	{	dpfh.setFranja55	(	dpfh.getFranja55	()+1);}break;
            case 56	:	{	dpfh.setFranja56	(	dpfh.getFranja56	()+1);}break;
            case 57	:	{	dpfh.setFranja57	(	dpfh.getFranja57	()+1);}break;
            case 58	:	{	dpfh.setFranja58	(	dpfh.getFranja58	()+1);}break;
            case 59	:	{	dpfh.setFranja59	(	dpfh.getFranja59	()+1);}break;
            case 60	:	{	dpfh.setFranja60	(	dpfh.getFranja60	()+1);}break;
            case 61	:	{	dpfh.setFranja61	(	dpfh.getFranja61	()+1);}break;
            case 62	:	{	dpfh.setFranja62	(	dpfh.getFranja62	()+1);}break;
            case 63	:	{	dpfh.setFranja63	(	dpfh.getFranja63	()+1);}break;
            case 64	:	{	dpfh.setFranja64	(	dpfh.getFranja64	()+1);}break;
            case 65	:	{	dpfh.setFranja65	(	dpfh.getFranja65	()+1);}break;
            case 66	:	{	dpfh.setFranja66	(	dpfh.getFranja66	()+1);}break;
            case 67	:	{	dpfh.setFranja67	(	dpfh.getFranja67	()+1);}break;
            case 68	:	{	dpfh.setFranja68	(	dpfh.getFranja68	()+1);}break;
            case 69	:	{	dpfh.setFranja69	(	dpfh.getFranja69	()+1);}break;
            case 70	:	{	dpfh.setFranja70	(	dpfh.getFranja70	()+1);}break;
            case 71	:	{	dpfh.setFranja71	(	dpfh.getFranja71	()+1);}break;
            case 72	:	{	dpfh.setFranja72	(	dpfh.getFranja72	()+1);}break;
            case 73	:	{	dpfh.setFranja73	(	dpfh.getFranja73	()+1);}break;
            case 74	:	{	dpfh.setFranja74	(	dpfh.getFranja74	()+1);}break;
            case 75	:	{	dpfh.setFranja75	(	dpfh.getFranja75	()+1);}break;
            case 76	:	{	dpfh.setFranja76	(	dpfh.getFranja76	()+1);}break;
            case 77	:	{	dpfh.setFranja77	(	dpfh.getFranja77	()+1);}break;
            case 78	:	{	dpfh.setFranja78	(	dpfh.getFranja78	()+1);}break;
            case 79	:	{	dpfh.setFranja79	(	dpfh.getFranja79	()+1);}break;
            case 80	:	{	dpfh.setFranja80	(	dpfh.getFranja80	()+1);}break;
            case 81	:	{	dpfh.setFranja81	(	dpfh.getFranja81	()+1);}break;
            case 82	:	{	dpfh.setFranja82	(	dpfh.getFranja82	()+1);}break;
            case 83	:	{	dpfh.setFranja83	(	dpfh.getFranja83	()+1);}break;
            case 84	:	{	dpfh.setFranja84	(	dpfh.getFranja84	()+1);}break;
            case 85	:	{	dpfh.setFranja85	(	dpfh.getFranja85	()+1);}break;
            case 86	:	{	dpfh.setFranja86	(	dpfh.getFranja86	()+1);}break;
            case 87	:	{	dpfh.setFranja87	(	dpfh.getFranja87	()+1);}break;
            case 88	:	{	dpfh.setFranja88	(	dpfh.getFranja88	()+1);}break;
            case 89	:	{	dpfh.setFranja89	(	dpfh.getFranja89	()+1);}break;
            case 90	:	{	dpfh.setFranja90	(	dpfh.getFranja90	()+1);}break;
            case 91	:	{	dpfh.setFranja91	(	dpfh.getFranja91	()+1);}break;
            case 92	:	{	dpfh.setFranja92	(	dpfh.getFranja92	()+1);}break;
            case 93	:	{	dpfh.setFranja93	(	dpfh.getFranja93	()+1);}break;
            case 94	:	{	dpfh.setFranja94	(	dpfh.getFranja94	()+1);}break;
            case 95	:	{	dpfh.setFranja95	(	dpfh.getFranja95	()+1);}break;
            case 96	:	{	dpfh.setFranja96	(	dpfh.getFranja96	()+1);}break;
        }
        disponibilidadFranjaHoraRepo.save(dpfh);

        DisponibilidadPorMenu dpm = disponibilidadPorMenuRepo.findById(dataModel.getEmpresa()+dataModel.getTipomenu()).orElse(null);

        String contenido = "Hola! \n"+dataModel.getNombre()+" acaba de reservar\n";
        contenido+="Tipo de menú: "+dataModel.getTipomenu()+"\n";
        DateTimeFormatter fmt = DateTimeFormat.forPattern("e");
        LocalDate ld = LocalDate.parse(dataModel.getFecha());
        int hoy = Integer.parseInt(ld.toString(fmt));
        int reservaday=0;
        switch (dia){
            case "lunes":{
                reservaday=1;
                dpm.setLunes(dpm.getLunes()-1);
            }break;
            case "martes":{
                reservaday=2;
                dpm.setMartes(dpm.getMartes()-1);
            }break;
            case "miercoles":{
                reservaday=3;
                dpm.setMiercoles(dpm.getMiercoles()-1);
            }break;
            case "jueves":{
                reservaday=4;
                dpm.setJueves(dpm.getJueves()-1);
            }break;
            case "viernes":{
                reservaday=5;
                dpm.setViernes(dpm.getViernes()-1);
            }break;
            case "sabado":{
                reservaday=6;
                dpm.setSabado(dpm.getSabado()-1);
            }break;
            case "domingo":{
                reservaday=7;
                dpm.setDomingo(dpm.getDomingo()-1);
            }break;
        }
        disponibilidadPorMenuRepo.save(dpm);
        LocalDate ld2 = ld.plusDays(reservaday-hoy);
        DateTimeFormatter fmt2 = DateTimeFormat.forPattern("EEEE dd - MMMM");
        contenido+="Para el día: "+ld2.toString(fmt2).replace("-","de")+"\n";
        contenido+="Tipo de entrega: "+dataModel.getEntrega()+"\n";
        if(!dataModel.getHoraentrega().equals("")){
            contenido+="Hora de entrega: "+dataModel.getHoraentrega()+"\n";
        }
        sendEmail(getmail(dataModel.getEmpresa()),"Tiene una nueva reservacion", contenido);
        reserepository.save(dataModel);
    }

    @RequestMapping(value = "/createhours", method = RequestMethod.POST)
    public void createhours(@Valid @RequestBody DisponibilidadPorFranjaHoraria cmodel) {
        disponibilidadFranjaHoraRepo.save(cmodel);
    }

    @RequestMapping(value = "/gethours/{empresa}/{dia}/{menu}", method = RequestMethod.GET)
    public List<Horas> gethours(@PathVariable String empresa, @PathVariable String dia, @PathVariable String menu) {
        DisponibilidadPorFranjaHoraria dmodel = disponibilidadFranjaHoraRepo.findByEmpresaAndDia(empresa,dia);
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
        String hini = horarios.get(0).gethInicioEnt();
        String hfin = horarios.get(0).gethFinEnt();

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

    /*@Scheduled(cron = "0 0 10 ? * MON", zone = "GMT-5")
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
    }*/
}

