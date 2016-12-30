package KRS_Downloader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Woj on 2016-12-30.
 */
public class Engine {
    private static Engine ENGINE;
    private static List<String> NAMES_LIST = new LinkedList<>();
    private static File OUTPUT_FILE;

    private Engine() {
    }


    public static void start(String path) {
        ENGINE = new Engine();

        fillNames_List(path);
        for (int i = 0; i < NAMES_LIST.size(); i++) {
            //punkty2 3 i 4
            workOnName(NAMES_LIST.get(i)); //przyjmuje id lub nazwe
        }

    }

    private static void createFile(String fileName) {
        //tworzy plik w tym samym folderze co wejsciowy o nazwie nazwawejsciowegoOutput
        Scanner scanner = new Scanner(System.in);
        String name = "OUTPUT" + fileName;
        File tmpOutputFile = null;
        while (true) {
            tmpOutputFile = new File(name);
            try {
                if (!tmpOutputFile.exists()) {
                    if (tmpOutputFile.createNewFile()) ;
                    OUTPUT_FILE = tmpOutputFile;
                    System.out.println("-Successfully created output file: " + name);
                    break;
                } else {
                    System.out.println("-ERROR- file with name " + name + " already exists. Type new file name");
                    name = scanner.nextLine();
                }
            } catch (IOException e) {
                System.out.println("-ERROR- Can not create output file");
            }
        }

    }

    private static void fillNames_List(String path) {
        File file = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line != "" && line != "\t" && line != System.lineSeparator() && line != " ") { //nie wczytywac gowien!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    NAMES_LIST.add(line);
                    System.out.println("Added company " + line + " from file " + file.getName());
                }
            }
            createFile(file.getName());
        } catch (FileNotFoundException e) {
            System.out.println("-ERROR- File does not exist");
        } catch (IOException e) {
            System.out.println("-ERROR- Could not load file");
        }

        //wypelnij statyczna liste stringow nazwami z pliku

    }

    private static void workOnName(String name) {// lub string name

        List<JsonObject> tempList = createTempList(name);
        chooseCompany(tempList);

    }

    private static List<JsonObject> createTempList(String name) {
        //dla danej nazwy znajduje firmy i tworzy z nich liste do wyboru
        List<JsonObject> result = new LinkedList<>();
        Gson GSON = new Gson();
        JsonObject JSON = null;
        try {
            JSON = GSON.fromJson(readUrl("https://api-v3.mojepanstwo.pl/dane/krs_podmioty.json?conditions[q]=" + name), JsonObject.class);
        } catch (Exception e) {
            System.out.println("-ERROR- Could not create JSON object from link https://api-v3.mojepanstwo.pl/dane/krs_podmioty.json?conditions[q]=" + name + " . Please check if name " + name + " is correct.");
        }

        //getJSON(companyName);
        if (JSON.getAsJsonArray("Dataobject").size() > 0) {
            System.out.println("-Successfully loaded companies containing \"" + name + "\" in their names");
            int size = JSON.getAsJsonArray("Dataobject").size();
            System.out.println("Loaded companies: ");
            for (int i = 0; i < size; i++) {
                JsonObject tmpCompany = GSON.fromJson(JSON.getAsJsonArray("Dataobject").get(i), JsonObject.class);
                result.add(tmpCompany);
                String tmpCompanyName = String.valueOf(tmpCompany.getAsJsonObject("data").get("krs_podmioty.firma"));
                String tmpCompanyKRS = String.valueOf(tmpCompany.getAsJsonObject("data").get("krs_podmioty.krs"));
                System.out.println(i + 1 + ". " + tmpCompanyName + " o KRS: " + tmpCompanyKRS);
            }

        } else {
            System.out.println("-ERROR- Company " + name + " does not exist in mojepanstwo.pl database");
        }
        return result;
    }

    private static void chooseCompany(List<JsonObject> jsonObjectList) {
        //uzytkownik wybiera firme tworzy sie nowa klasa Company(w konstruktorze przyjmuje nazwe krs i id a konstruktor tworzy graph) dla tej firmy a tymczasowa lista jest usuwana(jest wewnatrz metody)
        //zawiera metode 4
        //wyswietla dane z jsonow i kaze wybrac jeden z nich potem tworzy klase Company1
        List<JsonObject> innerJsonObjectList = jsonObjectList;
        Scanner scanner = new Scanner(System.in);
        System.out.print("-Type number of company you want to load: ");
        int choosen = 0;
        while (true) {
            if (scanner.hasNextInt()) {
                choosen = scanner.nextInt();
                if (choosen > 0 && choosen <= innerJsonObjectList.size()) break;
                else {
                    System.out.println("-ERROR- Wrong input- type correct number: ");
                }
            } else {
                System.out.println("-ERROR- Wrong input- type correct number: ");
                scanner.next();
            }
        }
        JsonObject jsonCompany = innerJsonObjectList.get(choosen - 1);
        String tmpCompanyName = String.valueOf(jsonCompany.getAsJsonObject("data").get("krs_podmioty.firma"));
        String tmpCompanyKRS = String.valueOf(jsonCompany.getAsJsonObject("data").get("krs_podmioty.krs"));
        System.out.println("-You checked company " + choosen + " : " + tmpCompanyName + " with KRS: " + tmpCompanyKRS);
        System.out.println("-All other companies from above list will not be loaded");

        writeToFile(jsonCompany, tmpCompanyName, tmpCompanyKRS);

    }

    private static void writeToFile(JsonObject jsonComapny, String name, String krs) {//ew moze przekazywac obiekt
        PrintWriter writer = null;
        String output, jsonComapnyId, tmpString;
        //tworzy nowy writer i pisze do pliku
        try {
            writer = new PrintWriter(new FileWriter(OUTPUT_FILE, true));

            tmpString = String.valueOf(jsonComapny.get("id"));
            jsonComapnyId = tmpString.substring(1, tmpString.length() - 1);
            System.out.println(jsonComapnyId);
            output = readUrl("https://api-v3.mojepanstwo.pl/dane/krs_podmioty/" + jsonComapnyId + ".json?layers[]=wspolnicy&layers[]=reprezentacja&layers[]=graph");
            System.out.println("https://api-v3.mojepanstwo.pl/dane/krs_podmioty/" + jsonComapnyId + ".json?layers[]=wspolnicy&layers[]=reprezentacja&layers[]=graph");
            System.out.println(readUrl("https://api-v3.mojepanstwo.pl/dane/krs_podmioty/" + jsonComapnyId + ".json?layers[]=wspolnicy&layers[]=reprezentacja&layers[]=graph"));
            writer.print("\n" + name + " krs: " + krs + " " + output + "\n"); // zerobic zeby dzialaly entery
            writer.println(" \n");


        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception in wypisz do pliu");
        } finally {
            writer.flush();
            writer.close();
        }


    }


    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }


}
