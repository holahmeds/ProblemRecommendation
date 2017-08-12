/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strawhats.problemrecommendation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author ahmed
 */
public class Main {

	public static void main(String[] args) {
            Scanner input = new Scanner(System.in);
            
            while (input.hasNext()) {
                String user = input.next();
                
                try {
                    List<Problem> recProblems = User.getAccepted(user).isEmpty() ? newUserSuggestions() : User.getRecommendedProblems(user);
                    
                    System.out.println(user);
                    for (Problem p : recProblems) {
                        System.out.println(p);
                    }
                    System.out.println();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
	}

	private static List<Problem> newUserSuggestions() {
		ArrayList<Problem> problems = null;
		try {
			problems = Problem.getProblems();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(-1);
		}

		return problems.stream().filter(problem -> problem.level == 0).collect(Collectors.toList());
	}

}
