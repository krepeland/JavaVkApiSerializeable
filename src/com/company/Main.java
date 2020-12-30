package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;


public class Main {
    public static void main(String[] args) {
        try {
            System.out.print("Введите id группы: ");
            Scanner scanner = new Scanner(System.in);
            String groupID = scanner.next();

            // получаем список городов из API:
            ArrayList<CityData> sortedCities = getCitiesFromAPI(groupID);

            // сериализация:
            writeToFile(sortedCities, groupID);

            // десериализация:
            ArrayList<CityData> citiesFromFile = readFromFile(groupID);

            //вывод на консоль:
            //*
            System.out.println("\n\n" + groupID);
            for (CityData city : citiesFromFile) {
                System.out.println(city.toString());
            }
            //*/

        } catch (Exception e) {
            System.out.println("EXCEPTION - " + e);
        }
    }

    private static ArrayList<CityData> getCitiesFromAPI(String groupID) throws IOException, JSONException, InterruptedException {
        Map<String, CityData> citiesMap = new HashMap<String, CityData>();
        int offset = 0;
        while (true) {
            try {
                URL url = new URL(String.format(
                        "https://api.vk.com/method/groups.getMembers?group_id=%s&fields=city&offset=%d&access_token=f5493d1ef5493d1ef5493d1e03f53ddc23ff549f5493d1eaae58a467485251565e27d2a&v=5.126",
                        groupID,
                        offset));
                URLConnection yc = url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                yc.getInputStream()));
                String inputLine = readAll(in);
                in.close();

                JSONObject json = new JSONObject(inputLine);
                JSONArray members = (JSONArray) json.getJSONObject("response").get("items");

                if (members.length() <= 0) {
                    break;
                }

                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = (JSONObject) members.get(i);
                    try {
                        String city = ((JSONObject) member.get("city")).get("title").toString();
                        if (citiesMap.containsKey(city)) {
                            citiesMap.get(city).addToCount(1);
                        } else {
                            citiesMap.put(city, new CityData(city, 1));
                        }
                    } catch (Exception e) {

                    }
                }

                System.out.println("Counted: " + (offset + members.length()));
                offset += 1000;
                if (offset % 5000 == 0) {
                    System.out.println("800ms waiting");
                    Thread.sleep(800);
                }
            }catch (Exception e){
                System.out.println("EXCEPTION - " + e);
            }
        }
        // Сортировка городов по количеству:
        ArrayList<CityData> sortedCities = new ArrayList<>();
        for (Map.Entry<String, CityData> entry : citiesMap.entrySet()) {
            sortedCities.add(entry.getValue());
        }
        sortedCities.sort(new Comparator<CityData>() {
            @Override
            public int compare(CityData o1, CityData o2) {
                return o2.getCount() - o1.getCount();
            }
        });
        return sortedCities;
    }

    private static void writeToFile(ArrayList<CityData> sortedCities, String groupID) {
        // Сериализация:
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(String.format("Cities-%s.dat", groupID)))) {
            for (CityData city : sortedCities) {
                oos.writeObject(city);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static ArrayList<CityData> readFromFile(String groupID) {
        // Десериализация:
        ArrayList<CityData> result = new ArrayList<CityData>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(String.format("Cities-%s.dat", groupID)))) {
            while (true) {
                CityData city = (CityData) ois.readObject();
                if (city == null)
                    break;
                result.add(city);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
