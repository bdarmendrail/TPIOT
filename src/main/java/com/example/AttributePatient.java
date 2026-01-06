package com.example;

public class AttributePatient {
    private String nomAttribut;

    public AttributePatient(String nomAttribut){
        this.nomAttribut = nomAttribut;
    }
    public String getNomAttribut(){
        return this.nomAttribut;
    }
    public void setNomAttribut(String nomAttribut){
        this.nomAttribut = nomAttribut;
    }
}
