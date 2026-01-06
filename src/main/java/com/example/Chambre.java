package com.example;
import java.util.ArrayList;

public class Chambre {
    private String numeroChambre;
    private ArrayList<Patient> patientList;
    private int nombreMaxPatient;

    public Chambre(String numeroChambre, int nombreMaxPatient){
        this.numeroChambre = numeroChambre;
        this.patientList = new ArrayList<Patient>();
        this.nombreMaxPatient = nombreMaxPatient;
    }

    public String getNumeroChambre(){
        return this.numeroChambre;
    }

    public int getNombrePatient(){
        return this.patientList.size();
    }

    public ArrayList<Patient> getPatientList(){
        return this.patientList;
    }

    public void setPatientList(ArrayList<Patient> patientList){
        this.patientList = patientList;
    }

    public int getNombreMaxPatient(){
        return this.nombreMaxPatient;
    }
    public void setNombreMaxPatient(int nombreMaxPatient){
        this.nombreMaxPatient = nombreMaxPatient;
    }
}
