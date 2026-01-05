package com.example;
import java.util;
import java.util.Scanner;

private static final String EXCHANGE_NAME = "mesure";

public class Main {
    public static void main(String[] args) {
        switch(choix){
            case 1:
                //Activation du patient
                Scanner scP = new Scanner(System.in);
                for(int i = 0;i<count(lesPatients);i++){
                    print((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients[i].getPrenom());    
                }
                int choice = scP.next();
                Patient pt = lesPatients.get(choice-1);
                Capteur sucre = new Sucre(pt);
                Capteur cardique = new Cardiaque(pt);
                Capteur oxymetre = new Cardiaque(pt);
                Mesure mesure_pt = new Mesure();
                mesure_pt.definePatient(pt);
                mesure_pt.addCapteur(sucre);
                mesure_pt.addCapteur(cardiaque);
                mesure_pt.addCapteur(oxymetre);
                String message = mesure_pt.toJson();

                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                try (Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()) {
                    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
                    System.out.println("Envoi de : '" + message + "'");
                }
                break;
        }
    }
}