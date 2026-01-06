package com.example;
public class Capteur{
    private Patient patient;
    private double value;
    private String typeCapteur;

    public Capteur(Patient pt){
        this.patient = pt;
    }
    public void setValue(double val){
        this.value = val;
    }
    public double getValue(){
        return this.value;
    }
    public void setTypeCapteur(String type){
        this.typeCapteur = type;
    }
    public String getTypeCapteur(){
        return this.typeCapteur;
    }
}