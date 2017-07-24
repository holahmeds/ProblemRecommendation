package com.strawhats.problemrecommendation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Problem {

	private static final String PROBLEM_INFO_FILE = "ProblemInfo.csv";

	int problemNum;
    String category;
	int level;

	public Problem(int problemNum, String category, int level) {
		this.problemNum = problemNum;
		this.category = category;
		this.level = level;
	}

    @Override
    public String toString() {
		return "{Problem Num: " + problemNum + ", Category: " + category + ", Level: " + level + "}";
	}

	public static ArrayList<Problem> getProblems() throws FileNotFoundException {
		Scanner fileScanner = new Scanner(new File(PROBLEM_INFO_FILE));
		fileScanner.useDelimiter("\n|,");

		// Ignore first line, it contains file headers
		fileScanner.nextLine();

		ArrayList<Problem> problems = new ArrayList<>();
		while (fileScanner.hasNext()) {
			int problemID = fileScanner.nextInt();
			String category = fileScanner.next();
			int level = fileScanner.nextInt();

			problems.add(new Problem(problemID, category, level));
		}

		return problems;
	}
    
}
