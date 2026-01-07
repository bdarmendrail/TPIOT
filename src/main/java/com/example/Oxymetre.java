package com.example;
public class Oxymetre extends Capteur {
    

    public Oxymetre(Patient pt,double saturationOxygene) {
        super(pt);
        this.setValue(saturationOxygene);
        this.setTypeCapteur("Oxymetre");
        //this.saturationOxygene = saturationOxygene;
    }

    public double getSaturationOxygene() {
        return this.getValue();
    }

    public void setSaturationOxygene(double saturationOxygene) {
        this.setValue(saturationOxygene);
        //this.saturationOxygene = saturationOxygene;
    }
}
