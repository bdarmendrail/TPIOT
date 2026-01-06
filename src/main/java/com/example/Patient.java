package com.example;
import java.util.UUID;
public class Patient{
    public UUID uuid;
    public String nom;
    public String prenom;

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
}