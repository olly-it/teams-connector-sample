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
	}

}
