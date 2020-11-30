package com.chefserver.demo.model;
import org.springframework.data.annotation.Id;

public class ListaMenusEmpresas {
    @Id
    String id;
    String empresa;
    String menu;
    Boolean check;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getEmpresa() { return empresa; }

    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getMenu() { return menu; }

    public void setMenu(String menu) { this.menu = menu; }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }
}
