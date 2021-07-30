package it.olly.teamsconnectorsample;

import org.json.JSONException;
import org.json.JSONObject;

public class TestJson {

	public static void main(String[] args) throws JSONException {
		org.json.JSONObject jo = new JSONObject();
		jo.put("text", "ciao \" zio!");
		String json = jo.toString();
		System.out.println(json);

		jo = new JSONObject(json);
		String txt = jo.getString("text");
		System.out.println("TXT = " + txt);

		JSONObject json2 = new JSONObject().put("body", new JSONObject().put("content", "text example"));
		System.out.println("2: " + json2);
	}

}
