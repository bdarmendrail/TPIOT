package com.example;

public class Webhook {
    private String url;
    private Patient patient; //si null, webhook pour tous les patients

    public Webhook(String url, Patient patient){
        this.url = url;
        this.patient = patient;
    }

    public String getUrl(){
        return this.url;
    }

    public boolean isAllPatients(){
        return this.patient == null;
    }

    public Patient getPatient(){
        return this.patient;
    }

    public String getURL(){
        return this.url;
    }
}
