package it.olly.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * base idea taken from https://github.com/eclipse-vertx/vert.x/tree/master/src/main/java/io/vertx/core/json
 * 
 * TODO complete & move to core
 * 
 * @author alessio olivieri
 *
 */
public class JsonArray implements Iterable<Object>{
	private static ObjectMapper om = new ObjectMapper();
	static {
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	private List<Object> list;

	@SuppressWarnings("unchecked")
	public JsonArray(String json) throws JsonMappingException, JsonProcessingException {
		if (json == null) {
			throw new NullPointerException();
		}
		list = om.readValue(json, List.class);
	}

	public JsonArray() {
		list = new ArrayList<>();
	}

	public JsonArray(List<Object> list) {
		if (list == null) { throw new NullPointerException(); }
		this.list = list;
	}
	
	public String getString(int pos) {
		Object ret = list.get(pos);
		if (ret==null) return null;
		return ret.toString();
	}
	
	@SuppressWarnings("unchecked")
	public JsonObject getJsonObject(int pos) {
		Object ret = list.get(pos);
		if (ret==null) return null;
		return new JsonObject((Map<String,Object>)list.get(pos));
	}
	
	@SuppressWarnings("unchecked")
	public JsonArray getJsonArray(int pos) {
		Object ret = list.get(pos);
		if (ret==null) return null;
		return new JsonArray((List<Object>)list.get(pos));
	}

	public List<Object> list() {
		return list;
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}
	
	public int size() {
		if (list==null) return 0;
		return list.size();
	}

	@Override
	public String toString() {
		if (list==null) return null;
		try {
			return om.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			return list.toString();
		}
	}
}