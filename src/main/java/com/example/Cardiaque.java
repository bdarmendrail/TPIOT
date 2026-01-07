package com.example;
public class Cardiaque extends Capteur {
    

    public Cardiaque(Patient pt,double frequenceCardiaque) {
        super(pt);
       this.setValue(frequenceCardiaque);
       this.setTypeCapteur("Cardiaque");
       //this.frequenceCardiaque = frequenceCardiaque;
    }

    public double getFrequenceCardiaque() {
        return this.getValue();
    }

    public void setFrequenceCardiaque(double frequenceCardiaque) {
        this.setValue(frequenceCardiaque);
    }
}
