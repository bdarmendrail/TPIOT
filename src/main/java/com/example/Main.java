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
    private static final String EXCHANGE_NAME_ENTRETIEN = "entretien";
    
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
        ArrayList<AttributePatient> lesAttributs = new ArrayList<AttributePatient>();
        lesAttributs.add(new AttributePatient("Diabétique"));
        lesAttributs.add(new AttributePatient("Insufisance respiratoire"));
        lesAttributs.add(new AttributePatient("Cardiaque"));
        lesAttributs.add(new AttributePatient("Asthme"));
        lesAttributs.add(new AttributePatient("Hypertension"));
        lesAttributs.add(new AttributePatient("Aucun"));
        

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
            //lesWebhooks.toString();
        }

        ArrayList<Chambre> lesChambres;
        Type REVIEW_TYPE_CHAMBRE = new TypeToken<ArrayList<Chambre>>() {}.getType();
        lesChambres = new ArrayList<Chambre>();
        File fC = new File("Chambres.json");
        if(fC.exists() && !fC.isDirectory()) { 
            Gson gsonRead = new Gson();
            JsonReader reader = new JsonReader(new FileReader("Chambres.json"));
            lesChambres = gsonRead.fromJson(reader, REVIEW_TYPE_CHAMBRE);
            //lesChambres.toString();
        }
        System.out.println("=== Système de Gestion des Patients IoT ===");
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
        System.out.println("10. Liste des patients enregistrés");
        System.out.println("11. Appuie bouton d'urgence (Simuler une alerte déclenché par un patient)");
        System.out.println("12. Créer une chambre");
        System.out.println("13. Supprimer une chambre");
        System.out.println("14. Afficher les chambres");
        System.out.println("15. Demander un nettoyage d'une chambre");
        System.out.println("16. Monitoring de l'entretien");
        System.out.println("17. Quitter");


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

                            File fW2 = new File("Webhooks.json");
                            if(fW2.exists() && !fW2.isDirectory()) { 
                                Gson gsonRead = new Gson();
                                JsonReader reader = new JsonReader(new FileReader("Webhooks.json"));
                                lesWebhooks = gsonRead.fromJson(reader, REVIEW_TYPE_WEBHOOK);
                                //lesWebhooks.toString();
                            }
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
                                                //System.out.println("Réponse du webhook: " + response.toString());
                                            }
                                        } catch (Exception e) {
                                            lesWebhooks.remove(wh);
                                            System.out.println("Erreur lors de l'envoi au webhook. Le webhook (" + wh.getUrl() + ") n'est pas un endpoint valide. Il ne recevra désormais plus de données.");
                                            try(Writer writer = new FileWriter("Webhooks.json")) {
                                                Gson gsonWriterFile = new GsonBuilder().create();
                                                gsonWriterFile.toJson(lesWebhooks, writer);
                                            } catch (IOException e2) {
                                                e2.printStackTrace();
                                            }
                                            //e.printStackTrace();
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
                                System.out.println("[" + date + "] Donnees de sante recues (" + mesure.getPatient().getNom() + " " + mesure.getPatient().getPrenom() + ") ===");

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
                    if(lesChambres.size() == 0){
                        System.out.println("Aucune chambre enregistrée. Nous ne sommes donc pas en capacité d'accueillir un patient.");
                        break;
                    }else{
                        boolean chambreDisponible = false;
                        for(Chambre c : lesChambres){
                            if(c.getNombrePatient() < c.getNombreMaxPatient()){
                                chambreDisponible = true;
                            }
                        }
                        if(!chambreDisponible){
                            System.out.println("Toutes les chambres sont complètes. Nous ne sommes donc pas en capacité d'accueillir un patient.");
                            break;
                        }
                    }
                    System.out.println("Option 3 sélectionnée : Ajouter un patient");
                    Scanner scanner = new Scanner(System.in);
                    Patient nvPatient = new Patient();
                    System.out.println("Entrer le nom du patient : ");
                    nvPatient.nom = scanner.nextLine();
                    System.out.println("Entrer le prenom du patient : ");
                    nvPatient.prenom = scanner.nextLine();
                    System.out.println("Sélectionner un attribut pour le patient : ");
                    for(int i = 0;i<lesAttributs.size();i++){
                        System.out.println((i+1) + " - "+lesAttributs.get(i).getNomAttribut());    
                    }
                    int choiceAttribut = scanner.nextInt();
                    nvPatient.setAttribut(lesAttributs.get(choiceAttribut-1));
                    int O = 1;
                    while(O==1){
                        System.out.println("Choississez un numéro de chambre : ");
                        for(int i = 0;i<lesChambres.size();i++){
                            if (lesChambres.get(i).getNombreMaxPatient() <= lesChambres.get(i).getNombrePatient()){
                                System.out.println((i+1) + " - "+lesChambres.get(i).getNumeroChambre() + "(COMPLET)");    
                            }else{
                                System.out.println((i+1) + " - "+lesChambres.get(i).getNumeroChambre());
                            }    
                        }
                        int choiceChambre = scanner.nextInt();
                        Chambre chambreChoisie = lesChambres.get(choiceChambre-1);
                        if(chambreChoisie.getNombreMaxPatient() <= chambreChoisie.getNombrePatient()){
                            System.out.println("La chambre est complète. Le patient ne peut pas être ajouté.");
                        }else{
                            //La chambre a de la place !
                            System.out.println("Ajout du patient dans cette chambre.");
                            chambreChoisie.getPatientList().add(nvPatient);
                            O=2;
                        }
                    }


                    
                    lesPatients.add(nvPatient);
                    try(Writer writer = new FileWriter("Patients.json")) {
                        Gson gsonWriterFile = new GsonBuilder().create();
                        gsonWriterFile.toJson(lesPatients, writer);
                        System.out.println("Patient ajouté : " + nvPatient.getNom() + " " + nvPatient.getPrenom() + " avec UUID " + nvPatient.getUuid());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try(Writer writerC = new FileWriter("Chambres.json")) {
                        Gson gsonWriterFileC = new GsonBuilder().create();
                        gsonWriterFileC.toJson(lesChambres, writerC);
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

                    if (lesPatients.isEmpty()) {
                        System.out.println("Aucun patient enregistré.");
                        break;
                    }

                    Scanner sc = new Scanner(System.in);

                    // Sélection patient
                    for (int i = 0; i < lesPatients.size(); i++) {
                        System.out.println((i + 1) + " - " + lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());
                    }
                    int choixPatient = sc.nextInt();
                    sc.nextLine(); // purge

                    Patient ptmodif = lesPatients.get(choixPatient - 1);

                    // Nouvelles infos
                    System.out.print("Nouveau nom (" + ptmodif.getNom() + ") : ");
                    String newNom = sc.nextLine();

                    System.out.print("Nouveau prénom (" + ptmodif.getPrenom() + ") : ");
                    String newPrenom = sc.nextLine();

                    System.out.println("Nouvel attribut :");
                    for (int i = 0; i < lesAttributs.size(); i++) {
                        System.out.println((i + 1) + " - " + lesAttributs.get(i).getNomAttribut());
                    }
                    int choixAttr = sc.nextInt();
                    sc.nextLine();

                    // Recherche chambre actuelle
                    Chambre chambreActuelle = null;
                    for (Chambre c : lesChambres) {
                        if (c.getPatientList().contains(ptmodif)) {
                            chambreActuelle = c;
                            break;
                        }
                    }

                    // Liste des chambres disponibles
                    List<Chambre> chambresDispo = new ArrayList<>();
                    System.out.println("Choisissez une chambre :");

                    for (Chambre c : lesChambres) {
                        if (c.getNombrePatient() < c.getNombreMaxPatient() || c == chambreActuelle) {
                            chambresDispo.add(c);
                            System.out.println(chambresDispo.size() + " - " + c.getNumeroChambre());
                        }
                    }

                    if (chambresDispo.isEmpty()) {
                        System.out.println("Aucune chambre disponible.");
                        break;
                    }

                    int choixChambre = sc.nextInt();
                    sc.nextLine();

                    Chambre nouvelleChambre = chambresDispo.get(choixChambre - 1);

                    // Déplacement patient
                    if (chambreActuelle != null && chambreActuelle != nouvelleChambre) {
                        chambreActuelle.getPatientList().remove(ptmodif);
                    }
                    if (!nouvelleChambre.getPatientList().contains(ptmodif)) {
                        nouvelleChambre.getPatientList().add(ptmodif);
                    }

                    // Mise à jour patient
                    ptmodif.setNom(newNom);
                    ptmodif.setPrenom(newPrenom);
                    ptmodif.setAttribut(lesAttributs.get(choixAttr - 1));

                    // Sauvegardes
                    try (Writer w1 = new FileWriter("Patients.json");
                        Writer w2 = new FileWriter("Chambres.json")) {

                        Gson g = new GsonBuilder().create();
                        g.toJson(lesPatients, w1);
                        g.toJson(lesChambres, w2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Patient modifié avec succès.");
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

                        System.out.println("En attente d'alertes...");

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
                            String patientInfo = wh.isAllPatients() ? "Tous les patients" : wh.getPatient().getNom() + " " + wh.getPatient().getPrenom();
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
                case 10:
                    System.out.println("Option 10 sélectionnée : Liste des patients enregistrés");
                    if(lesPatients.size() == 0){
                        System.out.println("Aucun patient enregistré.");
                        break;
                    }else{
                        System.out.println("Souhaitez vous pouvoir filtrer les patients par attribut? (oui/non)");
                        Scanner scFilter = new Scanner(System.in);
                        String reponseFilter = scFilter.nextLine();
                        if(reponseFilter.equalsIgnoreCase("oui")){
                            System.out.println("Sélectionner un attribut pour filtrer les patients : ");
                            for(int i = 0;i<lesAttributs.size();i++){
                                System.out.println((i+1) + " - "+lesAttributs.get(i).getNomAttribut());    
                            }
                            int choiceAttributFilter = scFilter.nextInt();
                            AttributePatient attributFilter = lesAttributs.get(choiceAttributFilter-1);
                            System.out.println("Patients avec l'attribut '" + attributFilter.getNomAttribut() + "' :");
                            for(int i = 0;i<lesPatients.size();i++){
                                Patient p = lesPatients.get(i);
                                if(p.getAttribut().getNomAttribut().equals(attributFilter.getNomAttribut())){
                                    System.out.println((i+1) + " - Nom: " + p.getNom() + " | Prénom: " + p.getPrenom());
                                }
                            }
                        }else{
                            System.out.println("Liste de tous les patients enregistrés :");
                            for(int i = 0;i<lesPatients.size();i++){
                                Patient p = lesPatients.get(i);
                                System.out.println((i+1) + " - Nom: " + p.getNom() + " | Prénom: " + p.getPrenom());
                            }
                        }
                    }
                    break;
                case 11:
                    System.out.println("Option 11 sélectionnée : Appuie bouton d'urgence (Simuler une alerte déclenché par un patient)");
                    if(lesPatients.size() == 0){
                        System.out.println("Aucun patient enregistré. Veuillez en ajouter un d'abord.");
                        break;
                    }
                    Scanner scP6 = new Scanner(System.in);
                    for(int i = 0;i<lesPatients.size();i++){
                        System.out.println((i+1) + " - "+lesPatients.get(i).getNom() + " " + lesPatients.get(i).getPrenom());    
                    }
                    int choice6 = scP6.nextInt();
                    Patient pt6 = lesPatients.get(choice6-1);
                    String messageAlerte = "!!! ALERTE D'URGENCE POUR LE PATIENT " + pt6.getNom() + " " + pt6.getPrenom() + " !!!\n";
                    messageAlerte += "Le patient a appuyé sur le bouton d'urgence.\n";
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
                        System.out.println("Alerte d'urgence envoyée pour le patient " + pt6.getNom() + " " + pt6.getPrenom() + ".");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 12:
                    System.out.println("Option 12 sélectionnée : Créer une chambre");
                    Scanner scannerChambre = new Scanner(System.in);
                    System.out.println("Entrer le numéro/nom de la chambre : ");
                    String numChambreNew = scannerChambre.nextLine();
                    System.out.println("Entrer le nombre maximum de patients dans la chambre : ");
                    int nbMaxPatient = scannerChambre.nextInt();
                    Chambre newChambre = new Chambre(numChambreNew, nbMaxPatient);
                    lesChambres.add(newChambre);
                    try(Writer writer = new FileWriter("Chambres.json")) {
                        Gson gsonWriterFile = new GsonBuilder().create();
                        gsonWriterFile.toJson(lesChambres, writer);
                        System.out.println("Chambre créée : " + newChambre.getNumeroChambre() + " avec une capacité de " + newChambre.getNombreMaxPatient() + " patients.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 13:
                    System.out.println("Option 13 sélectionnée : Supprimer une chambre");
                    if(lesChambres.size() == 0){
                        System.out.println("Aucune chambre enregistrée. Vous ne pouvez donc pas en supprimer.");
                        break;
                    }else{
                        Scanner scC = new Scanner(System.in);
                        for(int i = 0;i<lesChambres.size();i++){
                            System.out.println((i+1) + " - "+lesChambres.get(i).getNumeroChambre());
                        }
                        int choiceC = scC.nextInt();
                        Chambre chASupprimer = lesChambres.get(choiceC-1);
                        lesChambres.remove(chASupprimer);
                        try(Writer writer = new FileWriter("Chambres.json")) {
                            Gson gsonWriterFile = new GsonBuilder().create();
                            gsonWriterFile.toJson(lesChambres, writer);
                            System.out.println("Chambre supprimée.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 14:
                    System.out.println("Option 14 sélectionnée : Afficher les chambres");
                    if(lesChambres.size() == 0){
                        System.out.println("Aucune chambre enregistrée.");
                        break;
                    }else{
                        System.out.println("Liste des chambres enregistrées :");
                        for(int i = 0;i<lesChambres.size();i++){
                            System.out.println("=======");
                            Chambre ch = lesChambres.get(i);
                            System.out.println((i+1) + " - Numéro/nom: " + ch.getNumeroChambre() + " | Capacité: " + ch.getNombreMaxPatient() + " patients");
                            System.out.println("   Patients dans la chambre : ");
                            ArrayList<Patient> patientsInChambre = ch.getPatientList();
                            for(int j = 0;j<patientsInChambre.size();j++){
                                System.out.println("   - " + patientsInChambre.get(j).getNom() + " " + patientsInChambre.get(j).getPrenom());
                            }
                            System.out.println("=======");
                        }
                    }
                    break;
                case 15:
                    System.out.println("Option 15 sélectionnée : Demander un nettoyage d'une chambre");
                    if(lesChambres.size() == 0){
                        System.out.println("Aucune chambre enregistrée. Vous ne pouvez donc pas en demander le nettoyage.");
                        break;
                    }else{
                        Scanner scC2 = new Scanner(System.in);
                        for(int i = 0;i<lesChambres.size();i++){
                            System.out.println((i+1) + " - "+lesChambres.get(i).getNumeroChambre());
                        }
                        System.out.println("Entrer le numéro de la chambre à nettoyer : ");
                        int choiceC2 = scC2.nextInt();
                        Chambre chANettoyer = lesChambres.get(choiceC2-1);
                        String messageEntretien = "Demande de nettoyage pour la chambre " + chANettoyer.getNumeroChambre() + ".\n";
                        ConnectionFactory factoryEntretien = new ConnectionFactory();
                        factoryEntretien.setHost("localhost");
                        try (Connection connectionE = factoryEntretien.newConnection();    
                            Channel channelE = connectionE.createChannel()) {

                            channelE.exchangeDeclare(EXCHANGE_NAME_ENTRETIEN, "fanout");
                            channelE.basicPublish(
                                EXCHANGE_NAME_ENTRETIEN,
                                "",
                                null,
                                messageEntretien.getBytes("UTF-8")
                            );
                            System.out.println("Demande de nettoyage envoyée pour la chambre " + chANettoyer.getNumeroChambre() + ".");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 16:
                    System.out.println("Option 16 sélectionnée : Monitoring de l'entretien");
                    try {
                        ConnectionFactory factoryE = new ConnectionFactory();
                        factoryE.setHost("localhost");
                        Connection connection = factoryE.newConnection();
                        Channel channel = connection.createChannel();

                        channel.exchangeDeclare(EXCHANGE_NAME_ENTRETIEN, "fanout");

                        String queueName = channel.queueDeclare().getQueue();
                        channel.queueBind(queueName, EXCHANGE_NAME_ENTRETIEN, "");

                        System.out.println("En attente des demandes de nettoyage...");

                        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                            String message_recu = new String(delivery.getBody(), "UTF-8");

                            System.out.println("=== Demande de nettoyage reçue ===");
                            System.out.println(message_recu);
                            System.out.println("===================================");
                        };

                        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 17:
                    System.out.println("Option 17 sélectionnée : Quitter");
                    System.out.println("Fermeture de l'application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Option invalide");
                    break;
            }
    }
}