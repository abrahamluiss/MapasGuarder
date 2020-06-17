package com.guarderiashyo.guarderiashyo.models;

public class Guarderia {
    String id;
    String name;
    String email;
    String ruc;
    String trabajadores;

    public Guarderia(String id, String name, String email, String ruc, String trabajadores) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.ruc = ruc;
        this.trabajadores = trabajadores;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTrabajadores() {
        return trabajadores;
    }

    public void setTrabajadores(String trabajadores) {
        this.trabajadores = trabajadores;
    }
}
