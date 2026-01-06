package com.example;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.lang.reflect.Type;


public class Main {
    private static final String EXCHANGE_NAME = "mesure";
    private static final String EXCHANGE_NAME_ALERT = "alert";
    
    private static int random(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }
    private static double random(double min, double max, int decimals) {
        double value = min + Math.random() * (max - min);
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    
    public static void main(String[] args) throws IOException {
        ArrayList<Patient> lesPatients;
        Type REVIEW_TYPE = new TypeToken<ArrayList<Patient>>() {}.getType();
        lesPatients = new ArrayList<Patient>();
        
        File f = new File("Patients.json");
        if(f.exists() && !f.isDirectory()) { 
            Gson gsonRead = new Gson();
            JsonReader reader = new JsonReader(new FileReader("Patients.json"));
            lesPatients = gsonRead.fromJson(reader, REVIEW_TYPE);
            lesPatients.toString();
        }


        ArrayList<Webhook> lesWebhooks;
        Type REVIEW_TYPE_WEBHOOK = new TypeToken<ArrayList<Webhook>>() {}.getType();
        lesWebhooks = new ArrayList<Webhook>();
        
        File fW = new File("Webhooks.json");
        if(fW.exists() && !fW.isDirectory()) { 
            Gson gsonRead = new Gson();
            JsonReader reader = new JsonReader(new FileReader("Webhooks.json"));
            lesWebhooks = gsonRead.fromJson(reader, REVIEW_TYPE_WEBHOOK);
            lesWebhooks.toString();
        }
        /*
        Patient pat = new Patient();
        pat.nom = "Tata";
        pat.prenom = "Toto";
        //DEBUG - Vu qu'on a pas la persistance encore
        pat.uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        lesPatients.add(pat);*/
        System.out.println("Salut !");
        System.out.println("Fait ton choix :");
        System.out.println("1. Activation du patient");
        System.out.println("2. Visualisation des capteurs du patient");
        System.out.println("3. Ajouter un patient");
        System.out.println("4. Supprimer un patient");
        System.out.println("5. Modifier un patient");
        System.out.println("6. Voir toutes les alertes");
        System.out.println("7. Définir un webhook pour recevoir les données médicales d'un patient");
        System.out.println("8. Définir un webhook pour recevoir toutes les données médicales");
        System.out.println("9. Supprimer un webhook");

        Gson gson = new Gson();
        Scanner scM = new Scanner(System.in);
        int choix = scM.nextInt();
        
        switch(choix){
            case 1:
                System.out.println("Option 1 sélectionnée : Activation du patient");
                //Activation du patient
                if(lesPatients.size() == 0){
                    System.out.println("Aucun patient enregistré. Veuillez en ajouter un d'abord.");
                    break;
                }
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
                    Date now = new Date();
                    if(cardiaque.getValue() < 50 || cardiaque.getValue() > 180 ||
                       sucre.getValue() < 0.70 || sucre.getValue() > 1.30 ||
                       oxymetre.getValue() < 90){
                        String messageAlerte = "!!! [" + now + "]ALERTE DE SANTE POUR LE PATIENT " + pt.getNom() + " " + pt.getPrenom() + " !!!\n";
                        messageAlerte += sucre.getTypeCapteur()+ " : " + sucre.getMessage() + "\n";
                        messageAlerte += cardiaque.getTypeCapteur() + " : " + cardiaque.getMessage() + "\n";
                        messageAlerte += oxymetre.getTypeCapteur() + " : " + oxymetre.getMessage() + "\n";
                        messageAlerte += "=============================================\n";

                        ConnectionFactory factoryAlerte = new ConnectionFactory();
                        factoryAlerte.setHost("localhost");
                        try (Connection connectionA = factoryAlerte.newConnection();
                            Channel channelA = connectionA.createChannel()) {

                            channelA.exchangeDeclare(EXCHANGE_NAME_ALERT, "fanout");
                            channelA.basicPublish(
                                EXCHANGE_NAME_ALERT,
                                "",
                                null,
                                messageAlerte.getBytes("UTF-8")
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

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

                        if(lesWebhooks.size() > 0){
                            for(Webhook wh : lesWebhooks){
                                if(wh.isAllPatients() || wh.getPatient().getUuid().equals(pt.getUuid())){
                                    //Envoyer le message au webhook
                                    try {
                                        java.net.URL url = new java.net.URL(wh.getUrl());
                                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                        conn.setRequestMethod("POST");
                                        conn.setRequestProperty("Content-Type", "application/json; utf-8");
                                        conn.setRequestProperty("Accept", "application/json");
                                        conn.setDoOutput(true);
                                        try(java.io.OutputStream os = conn.getOutputStream()) {
                                            byte[] input = message.getBytes("utf-8");
                                            os.write(input, 0, input.length);           
                                        }
                                        try(java.io.BufferedReader br = new java.io.BufferedReader(
                                          new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                                            StringBuilder response = new StringBuilder();
                                            String responseLine = null;
                                            while ((responseLine = br.readLine()) != null) {
                                                response.append(responseLine.trim());
                                            }
                                            System.out.println("Réponse du webhook: " + response.toString());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        Thread.sleep(5000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    
               }
            case 2:
                System.out.println("Option 2 sélectionnée : Visualisation des données de santé");
                //Récupération et affichage des données de santé
                if(lesPatients.size() == 0){
                    System.out.println("Aucun patient enregistré. Veuillez en ajouter un d'abord.");
                    break;
                }
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
                                    " | Valeur : " + cap.getValue() + " " + cap.getUnite() +
                                    " | Message : " + cap.getMessage()
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
                break;
            case 3:
                System.out.println("Option 3 sélectionnée : Ajouter un patient");
                Scanner scanner = new Scanner(System.in);
                Patient nvPatient = new Patient();
                System.out.println("Entrer le nom du patient : ");
                nvPatient.nom = scanner.nextLine();
                System.out.println("Entrer le prenom du patient : ");
                nvPatient.prenom = scanner.nextLine();
                lesPatients.add(nvPatient);
                try(Writer writer = new FileWriter("Patients.json")) {
                    Gson gsonWriterFile = new GsonBuilder().create();
                    gsonWriterFile.toJson(lesPatients, writer);
                    System.out.println("Patient ajouté : " + nvPatient.getNom() + " " + nvPatient.getPrenom() + " avec UUID " + nvPatient.getUuid());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case 4:
                System.out.println("Option 4 sélectionnée : Supprimer un patient");
                if(lesPatients.size() == 0){
                    System.out.println("Aucun patient enregistré. Vous ne pouvez donc pas en supprimer.");
                    break;
                }else{
                    Scanner scP3 = new Scanner(System.in);
                    for(int i = 0;i<lesPatients.size();i++){
                        System.out.println((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());
                    }
                    int choice3 = scP3.nextInt();
                    Patient pt3 = lesPatients.get(choice3-1);
                    lesPatients.remove(pt3);
                    try(Writer writer = new FileWriter("Patients.json")) {
                        Gson gsonWriterFile = new GsonBuilder().create();
                        gsonWriterFile.toJson(lesPatients, writer);
                        System.out.println("Patient supprimé : " + pt3.getNom() + " " + pt3.getPrenom() + " avec UUID " + pt3.getUuid());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 5:
                System.out.println("Option 5 sélectionnée : Modifier les patients");
                if(lesPatients.size() == 0){
                    System.out.println("Aucun patient enregistré. Vous ne pouvez donc pas en modifier.");
                    break;
                }
                Scanner scP4 = new Scanner(System.in);
                for(int i = 0;i<lesPatients.size();i++){
                    System.out.println((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());    
                }
                int choice4 = scP4.nextInt();
                Patient pt4 = lesPatients.get(choice4-1);
                Scanner scanner4 = new Scanner(System.in);
                System.out.println("Entrer le nouveau nom du patient (actuel : " + pt4.getNom() + ") : ");
                String newNom = scanner4.nextLine();
                System.out.println("Entrer le nouveau prenom du patient (actuel : " + pt4.getPrenom() + ") : ");
                String newPrenom = scanner4.nextLine();
                pt4.setNom(newNom);
                pt4.setPrenom(newPrenom);
                try(Writer writer = new FileWriter("Patients.json")) {
                    Gson gsonWriterFile = new GsonBuilder().create();
                    gsonWriterFile.toJson(lesPatients, writer);
                    System.out.println("Patient modifié : " + pt4.getNom() + " " + pt4.getPrenom() + " avec UUID " + pt4.getUuid());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 6:
                    try {
                    ConnectionFactory factory2 = new ConnectionFactory();
                    factory2.setHost("localhost");
                    Connection connection = factory2.newConnection();
                    Channel channel = connection.createChannel();

                    channel.exchangeDeclare(EXCHANGE_NAME_ALERT, "fanout");

                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_NAME_ALERT, "");

                    System.out.println("En attente de mesures du patient correspondant...");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message_recu = new String(delivery.getBody(), "UTF-8");

                        System.out.println(message_recu);
                    };

                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 7:
                System.out.println("Option 7 sélectionnée : Définir un webhook pour un patient");
                //Définir un webhook pour un patient
                if(lesPatients.size() == 0){
                    System.out.println("Aucun patient enregistré. Veuillez en ajouter un d'abord.");
                    break;
                }
                Scanner scP5 = new Scanner(System.in);
                for(int i = 0;i<lesPatients.size();i++){
                    System.out.println((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());    
                }
                int choice5 = scP5.nextInt();
                Patient pt5 = lesPatients.get(choice5-1);
                Scanner scannerW = new Scanner(System.in);
                System.out.println("Entrer l'URL du webhook : ");
                String urlW = scannerW.nextLine();
                Webhook nvWebhook = new Webhook(urlW, pt5);
                lesWebhooks.add(nvWebhook);
                try(Writer writer = new FileWriter("Webhooks.json")) {
                    Gson gsonWriterFile = new GsonBuilder().create();
                    gsonWriterFile.toJson(lesWebhooks, writer);
                    System.out.println("Webhook ajouté pour le patient " + pt5.getNom() + " " + pt5.getPrenom() + " avec l'URL " + urlW);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 8:
                System.out.println("Option 8 sélectionnée : Définir un webhook pour tous les données médicales");
                //Définir un webhook pour toutes les données médicales
                Scanner scannerWAll = new Scanner(System.in);
                System.out.println("Entrer l'URL du webhook : ");
                String urlWAll = scannerWAll.nextLine();
                Webhook nvWebhookAll = new Webhook(urlWAll, null);
                lesWebhooks.add(nvWebhookAll);
                try(Writer writer = new FileWriter("Webhooks.json")) {
                    Gson gsonWriterFile = new GsonBuilder().create();
                    gsonWriterFile.toJson(lesWebhooks, writer);
                    System.out.println("Webhook ajouté pour toutes les données médicales avec l'URL " + urlWAll);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 9:
                System.out.println("Option 9 sélectionnée : Supprimer un webhook");
                if(lesWebhooks.size() == 0){
                    System.out.println("Aucun webhook enregistré. Vous ne pouvez donc pas en supprimer.");
                    break;
                }else{
                    Scanner scW = new Scanner(System.in);
                    for(int i = 0;i<lesWebhooks.size();i++){
                        Webhook wh = lesWebhooks.get(i);
                        String patientInfo = wh.isAllPatients() ? "Tous les patients" : wh.patient.getNom() + " " + wh.patient.getPrenom();
                        System.out.println((i+1) + " - URL: " + wh.getUrl() + " | Patient: " + patientInfo);
                    }
                    System.out.println("Entrer le numéro du webhook à supprimer : ");
                    int choiceW = scW.nextInt();
                    Webhook webhookASupprimer = lesWebhooks.get(choiceW-1);
                    lesWebhooks.remove(webhookASupprimer);
                    try(Writer writer = new FileWriter("Webhooks.json")) {
                        Gson gsonWriterFile = new GsonBuilder().create();
                        gsonWriterFile.toJson(lesWebhooks, writer);
                        System.out.println("Webhook supprimé.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }   
                }
                break;
            default:
                System.out.println("Option invalide");
                break;
        }
    }
}