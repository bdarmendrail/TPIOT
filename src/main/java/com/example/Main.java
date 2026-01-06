package com.example;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;


public class Main {
    private static final String EXCHANGE_NAME = "mesure";
    
    private static int random(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }
    private static double random(double min, double max, int decimals) {
        double value = min + Math.random() * (max - min);
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    
    public static void main(String[] args) {
        ArrayList<Patient> lesPatients;
        lesPatients = new ArrayList<Patient>();
        Patient pat = new Patient();
        pat.nom = "Tata";
        pat.prenom = "Toto";
        //DEBUG - Vu qu'on a pas la persistance encore
        pat.uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        lesPatients.add(pat);
        System.out.println("Salut !");
        System.out.println("Fait ton choix :");
        System.out.println("1. Activation du patient");
        System.out.println("2. Visualisation des capteurs du patient");
        Gson gson = new Gson();
        Scanner scM = new Scanner(System.in);
        int choix = scM.nextInt();
        
        switch(choix){
            case 1:
                System.out.println("Option 1 sélectionnée : Activation du patient");
                //Activation du patient
                Scanner scP = new Scanner(System.in);
                for(int i = 0;i<lesPatients.size();i++){
                    System.out.println((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());    
                }
                int choice = scP.nextInt();
                Patient pt = lesPatients.get(choice-1);
                while(true){
                    Capteur sucre = new Sucre(pt,random(0.70,1.30,2));
                    Capteur cardiaque = new Cardiaque(pt,random(60,100));
                    Capteur oxymetre = new Oxymetre(pt,random(80,100));
                    Mesure mesure_pt = new Mesure();
                    mesure_pt.definePatient(pt);
                    mesure_pt.addCapteur(sucre);
                    mesure_pt.addCapteur(cardiaque);
                    mesure_pt.addCapteur(oxymetre);
                    String message = gson.toJson(mesure_pt);

                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost("localhost");
                    try (Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel()) {

                        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                        channel.basicPublish(
                            EXCHANGE_NAME,
                            "",
                            null,
                            message.getBytes("UTF-8")
                        );

                        System.out.println("Envoi de : '" + message + "'");
                        Thread.sleep(5000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
               }
            case 2:
                System.out.println("Option 2 sélectionnée : Visualisation des données de santé");
                //Récupération et affichage des données de santé

                Scanner scP2 = new Scanner(System.in);
                for(int i = 0;i<lesPatients.size();i++){
                    System.out.println((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());    
                }
                int choice2 = scP2.nextInt();
                Patient pt2 = lesPatients.get(choice2-1);
                try {
                    ConnectionFactory factory2 = new ConnectionFactory();
                    factory2.setHost("localhost");
                    Connection connection = factory2.newConnection();
                    Channel channel = connection.createChannel();

                    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_NAME, "");

                    System.out.println("En attente de mesures du patient correspondant...");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message_recu = new String(delivery.getBody(), "UTF-8");
                        Mesure mesure = gson.fromJson(message_recu, Mesure.class);

                        if (mesure != null &&
                            mesure.getPatient().getUuid().equals(pt2.getUuid())) {

                            Date date = new Date();
                            System.out.println("[" + date + "] Donnees de sante recues ===");

                            for (Capteur cap : mesure.getSensors()) {
                                System.out.println(
                                    "Type de capteur : " + cap.getTypeCapteur() +
                                    " | Valeur : " + cap.getValue()
                                );
                            }
                            System.out.println("\n=========\n");
                        }else if(mesure != null){
                            //Debug mesure?
                        }
                    };

                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }
}