package com.strawhats.problemrecommendation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

}
