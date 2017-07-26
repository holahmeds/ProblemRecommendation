package com.strawhats.problemrecommendation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class User {

	private static final String GET_UID_URL = "http://uhunt.felix-halim.net/api/uname2uid/%s";
	private static final String GET_USER_SUBMISSIONS_URL = "http://uhunt.felix-halim.net/api/subs-user/%d";

	public static int getUID(String username) throws IOException {
		URL url = null;
		try {
			url = new URL(String.format(GET_UID_URL, username));
		} catch (MalformedURLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextInt();
	}

	public static ArrayList<Problem> getAccepted(String username) throws IOException {
		int uid = getUID(username);

		URL url = null;
		try {
			url = new URL(String.format(GET_USER_SUBMISSIONS_URL, uid));
		} catch (MalformedURLException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		Scanner responseStream = new Scanner(connection.getInputStream());
		responseStream.useDelimiter("\\A");

		JSONObject response = new JSONObject(responseStream.next());
		JSONArray submissions = response.getJSONArray("subs");

		HashSet<Integer> acceptedPIDs = new HashSet<>();
		for (int i = 0; i < submissions.length(); i++) {
			JSONArray sub = submissions.getJSONArray(i);
			if (sub.getInt(2) == 90) { // verdict is accepted
				acceptedPIDs.add(sub.getInt(1));
			}
		}

		ArrayList<Problem> problems = Problem.getProblems();
		ArrayList<Problem> acceptedProblems = problems.stream().filter(problem -> acceptedPIDs.contains(problem.problemID)).collect(Collectors.toCollection(ArrayList::new));

		return acceptedProblems;
	}

	public static Map<String, Double> getAcceptedRatioPerCategory(String username) throws IOException {
		Map<String, Long> totalProblems = Problem.getCountPerCategory(Problem.getProblems());
		Map<String, Long> userACProblems = Problem.getCountPerCategory(User.getAccepted(username));

		Map<String, Double> ACRatio = new HashMap<>();
		for (String Category : totalProblems.keySet()) {
			ACRatio.put(Category, userACProblems.get(Category).doubleValue() / totalProblems.get(Category));
		}

		return ACRatio;
	}

	public static Map<String, Map<Integer, Double>> getAcceptedRatioPerLevelPerCategory(String username) throws IOException {
		Map<String, Map<Integer, Long>> totalProblems = Problem.getProblemStats(Problem.getProblems());
		Map<String, Map<Integer, Long>> userACProblems = Problem.getProblemStats(User.getAccepted(username));

		Map<String, Map<Integer, Double>> ACRatio = new HashMap<>();
		for (String category : totalProblems.keySet()) {
			Map<Integer, Long> totalcountPerLevel = totalProblems.get(category);
			Map<Integer, Long> userCountPerLevel = userACProblems.getOrDefault(category, Collections.EMPTY_MAP);

			Map<Integer, Double> levelGroup = new HashMap<>();
			for (Integer level : totalcountPerLevel.keySet()) {
				levelGroup.put(level, userCountPerLevel.getOrDefault(level, Long.valueOf(0)).doubleValue() / totalcountPerLevel.get(level));
			}
			ACRatio.put(category, levelGroup);
		}

		return ACRatio;
	}

}
