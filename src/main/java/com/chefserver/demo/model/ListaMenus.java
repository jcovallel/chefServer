package com.chefserver.demo.model;
import org.springframework.data.annotation.Id;

public class ListaMenus {
    @Id
    String id;
    String menu;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getMenu() { return menu; }

    public void setMenu(String menu) { this.menu = menu; }
}
