package com.strawhats.problemrecommendation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class User {

	private static final String GET_UID_URL = "http://uhunt.felix-halim.net/api/uname2uid/%s";
	private static final String GET_USER_SUBMISSIONS_URL = "http://uhunt.felix-halim.net/api/subs-user/%d";
	private static final String GET_PROBLEM_FROM_PID_URL = "http://uhunt.felix-halim.net/api/p/id/%d";

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

	public static int[] getAccepted(String username) throws IOException {
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

		HashSet<Integer> set = new HashSet<>();
		for (int i = 0; i < submissions.length(); i++) {
			JSONArray sub = submissions.getJSONArray(i);
			if (sub.getInt(2) == 90) {
				set.add(sub.getInt(1));
			}
		}

		int[] arr = new int[set.size()];
		int i = 0;
		for (Integer pid : set) {
			arr[i] = getProblemNum(pid);
			i++;
		}

		return arr;
	}

	private static int getProblemNum(int problemID) throws IOException {
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

}
