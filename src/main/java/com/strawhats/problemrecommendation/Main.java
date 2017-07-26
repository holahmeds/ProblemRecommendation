/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strawhats.problemrecommendation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author ahmed
 */
public class Main {

	private static final String[] CATEGORIES = {
		"Introduction", "Data Structures and Libraries", "Problem Solving Paradigms",
		"Graph", "Mathematics", "String Processing", "(Computational) Geometry"
	};

	public static void main(String[] args) {
		/*
		HashMap<Integer, Problem> problemNumMap = new HashMap<>();
		for (Problem p : problems) {
			problemNumMap.put(p.problemNum, p);
		}

		ArrayList<HashMap<String, ArrayList<Integer>>> info = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			HashMap<String, ArrayList<Integer>> map = new HashMap<>();
			for (String cat : CATEGORIES) {
				map.put(cat, new ArrayList<>());
			}
			info.add(map);
		}
		for (Problem p : problems) {
			info.get(p.level).get(p.category).add(p.problemNum);
		}

		ArrayList<Problem> suggestions = new ArrayList<>();
		HashMap<String, ArrayList<Integer>> level0Probs = info.get(0);
		for (String cat : CATEGORIES) {
			for (int i = 0; i < 2 && i < level0Probs.get(cat).size(); i++) {
				suggestions.add(problemNumMap.get(level0Probs.get(cat).get(i)));
			}
		}
		 */

		System.out.println(newUserSuggestions());
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
