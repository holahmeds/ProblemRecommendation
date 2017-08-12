package com.strawhats.problemrecommendation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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

	public static Map<String, List<Integer>> getRecommendedLevelsPerCategory(String username) throws IOException {
		Map<String, Map<Integer, Double>> ACRatios = getAcceptedRatioPerLevelPerCategory(username);

		Map<String, List<Integer>> recommendation = new HashMap<>();
		for (String category : ACRatios.keySet()) {
			Map<Integer, Double> levelRatios = ACRatios.get(category);

			// get lowest level for category
			int recLevel = levelRatios.keySet().stream().min((i1, i2) -> Integer.compare(i1, i2)).get();
			double recLevelRatio = 0;

			for (Integer l : levelRatios.keySet()) {
				if (levelRatios.get(l) > recLevelRatio) {
					recLevel = l;
					recLevelRatio = Math.min(levelRatios.get(l), 0.70);

					if (levelRatios.get(l) > 0.70) {
						recLevel = l + 1;
						recLevelRatio = Math.min(levelRatios.get(l + 1), 0.70);
					}
				}
			}

			recommendation.put(category, new ArrayList<>());
			recommendation.get(category).add(recLevel);
			if (levelRatios.get(recLevel) > 0.40 && levelRatios.get(recLevel) < 0.70) {
				recommendation.get(category).add(recLevel + 1);
			}
		}

		return recommendation;
	}

	public static List<Problem> getRecommendedProblems(String username) throws IOException {
		List<Problem> allProblems = Problem.getProblems();
		List<Problem> userSolvedProblems = getAccepted(username);
		Map<String, List<Integer>> recommendedLevels = getRecommendedLevelsPerCategory(username);

		// filter out solved problems and problems of other levels
		List<Problem> filteredProblems = allProblems.stream()
				.filter(problem -> !userSolvedProblems.contains(problem))
				.filter(problem -> recommendedLevels.get(problem.category).contains(problem.level))
				.collect(Collectors.toList());
		Collections.shuffle(allProblems);

		List<Problem> recommendedProblems = new ArrayList<>();

		// get 2 problems from each problem
		List<String> categories = allProblems.stream().map(problem -> problem.category).distinct().collect(Collectors.toList());
		for (String cat : categories) {
			recommendedProblems.addAll(filteredProblems.stream().filter(problem -> problem.category.equals(cat)).limit(2).collect(Collectors.toList()));
		}

		return recommendedProblems;
	}

}
