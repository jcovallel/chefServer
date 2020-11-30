package com.chefserver.demo.model;
import org.springframework.data.annotation.Id;

public class HorariosMenus {
    @Id
    String id;
    String empresa;
    String menu;
    String hInicioRes;
    String hFinRes;
    String hInicioEnt;
    String hFinEnt;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getMenu() { return menu; }

    public void setMenu(String menu) { this.menu = menu; }

    public String gethInicioRes() {
        return hInicioRes;
    }

    public void sethInicioRes(String hInicioRes) {
        this.hInicioRes = hInicioRes;
    }

    public String gethFinRes() {
        return hFinRes;
    }

    public void sethFinRes(String hFinRes) {
        this.hFinRes = hFinRes;
    }

    public String gethInicioEnt() {
        return hInicioEnt;
    }

    public void sethInicioEnt(String hInicioEnt) {
        this.hInicioEnt = hInicioEnt;
    }

    public String gethFinEnt() {
        return hFinEnt;
    }

    public void sethFinEnt(String hFinEnt) {
        this.hFinEnt = hFinEnt;
    }
}
