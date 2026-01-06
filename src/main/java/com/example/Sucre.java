package com.example;
public class Sucre extends Capteur{
    
    private double Sucre;

    public Sucre(Patient pt,double Sucre) {
        super(pt);
        this.setValue(Sucre);
        this.setTypeCapteur("Sucre");
        //this.Sucre = Sucre;
    }

    public double getSucre() {
        return this.getValue();
    }

    public void setSucre(double Sucre) {
        this.setValue(Sucre);
        //this.Sucre = Sucre;
    }
}