package com.example.inboxlistproject;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class HttpHandler {

	private static final String TAG = HttpHandler.class.getSimpleName();

	public HttpHandler() {
	}

	// GET
	public String makeServiceCall(String reqUrl) {
		String response = null;
		try {
			URL url = new URL(reqUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			// read the response
			InputStream in = new BufferedInputStream(conn.getInputStream());
			response = convertStreamToString(in);
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
		} catch (ProtocolException e) {
			Log.e(TAG, "ProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + e.getMessage());
		}
		return response;
	}

	// POST
	public String makePostCall(String reqUrl, HashMap<String, String> params) {
		String response = null;
		try {
			URL url = new URL(reqUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			//conn.setReadTimeout(10000);
			//conn.setConnectTimeout(15000);            
			conn.setDoInput(true);            
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(os, "UTF-8"));
			writer.write(handlePostParams(params));
			writer.flush();
			writer.close();
			os.close();

			// read the response
			InputStream in = new BufferedInputStream(conn.getInputStream());
			response = convertStreamToString(in);
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
		} catch (ProtocolException e) {
			Log.e(TAG, "ProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + e.getMessage());
		}
		return response;
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// handle post params with map
	private String handlePostParams(HashMap<String, String> map) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (HashMap.Entry<String, String> pair : map.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

}

