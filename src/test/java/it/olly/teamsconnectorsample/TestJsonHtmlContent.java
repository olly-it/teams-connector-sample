package it.olly.teamsconnectorsample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONObject;

public class TestJsonHtmlContent {

	public static void main(String[] args) throws Exception {
		File file = new File("/Users/Alessio Olivieri/_/template.html");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");

		JSONObject jo = new JSONObject();
		jo.put("HtmlPart", str);

		FileOutputStream fout = new FileOutputStream(new File("/Users/Alessio Olivieri/_/template.json"));
		fout.write(jo.toString().getBytes());
		fout.close();
	}

}
