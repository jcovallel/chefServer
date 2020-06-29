package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class EmpresasModel {
    @Id
    public String nombreid;
    public String nombre;
    public String correo;
    public String password;
    public int rol;

    public String getNombreid() {
        return nombreid;
    }

    public void setNombreid(String nombreid) {
        this.nombreid = nombreid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }
}
