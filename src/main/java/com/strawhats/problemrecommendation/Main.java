/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strawhats.problemrecommendation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author ahmed
 */
public class Main {
    private static final String PROBLEM_CATEGORIES_FILE_NAME = "Problem Categories.csv";
    private static final String URL_FORMAT = "http://uhunt.felix-halim.net/api/p/num/%d";
    
    public static void main(String[] args) {
        try {
            Scanner fileScanner = new Scanner(new File(PROBLEM_CATEGORIES_FILE_NAME));
            fileScanner.useDelimiter("\n|,");
            
            // Ignore first line, it contains file headers
            fileScanner.nextLine();
            
            while (fileScanner.hasNext()) {
                int problemID = fileScanner.nextInt();
                String problemCategory = fileScanner.next();
                
                try {
                    Problem p = getProblem(problemID);
                    p.category = problemCategory;
                    
                    System.out.println(p);
                } catch (IOException ex) {
                    System.out.println("Error getting data for problem " + problemID + ": " + ex.getMessage());
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not found: " + PROBLEM_CATEGORIES_FILE_NAME);
        }
    }
    
    private static Problem getProblem(int problemID) throws IOException {
        try {
            URL url = new URL(String.format(URL_FORMAT, problemID));
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            Scanner responseStream = new Scanner(connection.getInputStream());
            responseStream.useDelimiter("\\A");
            
            JSONObject response = new JSONObject(responseStream.next());
            String[] verdicts = { "sube" , "noj" , "inq" , "ce" , "rf" , "re" , "ole" , "tle" , "mle" , "wa" , "pe" , "ac" };
            
            Problem p = new Problem();
            p.problemID = problemID;
            p.accepted = response.getInt("ac");
            // no field for total submissions
            // so use sum of all the verdict fields
            for (String ver : verdicts) {
                p.sumbmissions += response.getInt(ver);
            }
            
            return p;
        } catch (MalformedURLException ex) {
            System.err.println("Main.URL_FORMAT creates incorrect url.");
            System.exit(-1);
        }
        
        return null;
    }
}
