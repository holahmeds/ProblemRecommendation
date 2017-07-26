package com.strawhats.problemrecommendation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class Problem {

	private static final String PROBLEM_INFO_FILE = "ProblemInfo.csv";
	private static final String GET_PROBLEM_FROM_PID_URL = "http://uhunt.felix-halim.net/api/p/id/%d";
	private static final String GET_PROBLEM_FROM_PNUM_URL = "http://uhunt.felix-halim.net/api/p/num/%d";

	int problemNum;
	int problemID;
    String category;
	int level;

	public Problem(int problemNum, int problemID, String category, int level) {
		this.problemNum = problemNum;
		this.problemID = problemID;
		this.category = category;
		this.level = level;
	}

	public String getCategory() {
		return category;
	}

	public int getLevel() {
		return level;
	}

    @Override
    public String toString() {
		return "{Problem Num: " + problemNum + ", Problem ID: " + problemID + ", Category: " + category + ", Level: " + level + "}";
	}

	public static ArrayList<Problem> getProblems() throws FileNotFoundException {
		Scanner fileScanner = new Scanner(new File(PROBLEM_INFO_FILE));
		fileScanner.useDelimiter("\n|,");

		// Ignore first line, it contains file headers
		fileScanner.nextLine();

		ArrayList<Problem> problems = new ArrayList<>();
		while (fileScanner.hasNext()) {
			int problemNum = fileScanner.nextInt();
			int problemID = fileScanner.nextInt();
			String category = fileScanner.next();
			int level = fileScanner.nextInt();

			problems.add(new Problem(problemNum, problemID, category, level));
		}

		return problems;
	}

	public static int getProblemNum(int problemID) throws IOException {
		URL url = null;
		try {
			url = new URL(String.format(GET_PROBLEM_FROM_PID_URL, problemID));
		} catch (MalformedURLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		Scanner responseStream = new Scanner(connection.getInputStream());
		responseStream.useDelimiter("\\A");

		JSONObject response = new JSONObject(responseStream.next());
		return response.getInt("num");
	}

	public static int getProblemID(int problemNum) throws IOException {
		URL url = null;
		try {
			url = new URL(String.format(GET_PROBLEM_FROM_PNUM_URL, problemNum));
		} catch (MalformedURLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		Scanner responseStream = new Scanner(connection.getInputStream());
		responseStream.useDelimiter("\\A");

		JSONObject response = new JSONObject(responseStream.next());
		return response.getInt("pid");
	}

	public static Map<String, Map<Integer, Long>> getProblemStats(ArrayList<Problem> problems) {
		HashMap<String, Map<Integer, Long>> stats = new HashMap<>();

		List<String> categories = problems.stream().map(problem -> problem.category).collect(Collectors.toList());
		for (String cat : categories) {
			List<Problem> categoryProblems = problems.stream().filter(problem -> problem.category.equals(cat)).collect(Collectors.toList());
			Map<Integer, Long> levelGroup = categoryProblems.stream().collect(Collectors.groupingBy(Problem::getLevel, Collectors.counting()));

			stats.put(cat, levelGroup);
		}

		return stats;
	}

	public static Map<String, Long> getCountPerCategory(ArrayList<Problem> problems) {
		return problems.stream().collect(Collectors.groupingBy(Problem::getCategory, Collectors.counting()));
	}
    
}
