package com.example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class Mesure{
    private UUID idMesure;
    private Patient pt;
    private ArrayList<Capteur> sensors;

    public Mesure(){
        this.sensors = new ArrayList<Capteur>();
    }
    public void definePatient(Patient pat){
        this.pt = pat;
    }
    public void addCapteur(Capteur cap){
        this.sensors.add(cap);
    }

    public Patient getPatient(){
        return this.pt;
    }

    public ArrayList<Capteur> getSensors(){
        return this.sensors;
    }
}