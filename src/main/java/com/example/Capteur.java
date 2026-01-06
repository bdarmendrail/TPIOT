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

    public String getMessage(){
        String laChaine = "";
        switch (this.getTypeCapteur()) {
            case "Sucre":
                if (this.getValue() < 0.70) {
                    laChaine =  "Hypoglycémie détectée : " + this.getValue() + " g/L";
                } else if (this.getValue() > 1.30) {
                    laChaine = "Hyperglycémie détectée : " + this.getValue() + " g/L";
                } else {
                    laChaine = "RAS : " + this.getValue() + " g/L";
                }
                break;
            case "Cardiaque":
                if(this.getValue() < 50){
                    laChaine = "Rythme cardiaque basse détectée : " + this.getValue() + " bpm";
                }else if(this.getValue() > 180){
                    laChaine = "Rythme cardiaque élevé détecté : " + this.getValue() + " bpm";
                }else{
                    laChaine = "RAS : " + this.getValue() + " bpm";
                }
                break;
            case "Oxymetre":
                if(this.getValue() < 90){
                    laChaine = "Hypoxémie détectée : " + this.getValue() + " %";
                } else {
                    laChaine = "RAS : " + this.getValue() + " %";
                }
                break;
        }

        return laChaine;
    }


    public String getUnite(){
        String unite = "";
        switch (this.getTypeCapteur()) {
            case "Sucre":
                unite = "g/L";
                break;
            case "Cardiaque":
                unite = "bpm";
                break;
            case "Oxymetre":
                unite = "%SpO2";
                break;
        }
        return unite;
    }
}