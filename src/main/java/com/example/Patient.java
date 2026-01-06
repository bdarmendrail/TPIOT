package com.example;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Attr;
public class Patient{
    public UUID uuid;
    public String nom;
    public String prenom;
    public AttributePatient attribut;

    public Patient(){
        this.uuid = UUID.randomUUID();
    }

    public String getPrenom(){
        return this.prenom;
    }
    public String getNom(){
        return this.nom;
    }
    public UUID getUuid(){
        return this.uuid;
    }
    public void setNom(String nom){
        this.nom = nom;
    }
    public void setPrenom(String prenom){
        this.prenom = prenom;
    }
    public void setUuid(UUID uuid){
        this.uuid = uuid;
    }

    public AttributePatient getAttribut(){
        return this.attribut;
    }
    public void setAttribut(AttributePatient attribut){
        this.attribut = attribut;
    }
}