/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package it.olly.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * base idea taken from https://github.com/eclipse-vertx/vert.x/tree/master/src/main/java/io/vertx/core/json
 * 
 * TODO complete & move to core // evaluate to import maybe: org.json.JSONObject -> very light library with only JSON support 
 * 
 * @author alessio olivieri
 *
 */
public class JsonObject implements Iterable<Map.Entry<String, Object>>{
	private static ObjectMapper om = new ObjectMapper();
	static {
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private Map<String, Object> map;

	@SuppressWarnings("unchecked")
	public JsonObject(String json) throws JsonMappingException, JsonProcessingException {
		if (json == null) {
			throw new NullPointerException();
		}
		map = om.readValue(json, Map.class);
	}

	public JsonObject() {
		map = new LinkedHashMap<>();
	}

	public JsonObject(Map<String, Object> map) {
		if (map == null) {
			throw new NullPointerException();
		}
		this.map = map;
	}
	
	public String getString(String key) {
		Object ret = map.get(key);
		if (ret==null) return null;
		return ret.toString();
	}
	
	@SuppressWarnings("unchecked")
	public JsonObject getJsonObject(String key) {
		Object ret = map.get(key);
		if (ret==null) return null;
		return new JsonObject((Map<String, Object>)map.get(key));
	}
	
	@SuppressWarnings("unchecked")
	public JsonArray getJsonArray(String key) {
		Object ret = map.get(key);
		if (ret==null) return null;
		return new JsonArray((List<Object>)map.get(key));
	}
	
	public Map<String, Object> map() {
		return map;
	}

	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return map.entrySet().iterator();
	}
	
	public int size() {
		if (map==null) return 0;
		return map.size();
	}
	
	@Override
	public String toString() {
		if (map==null) return null;
		return map.toString();
	}
	
	public Object get(String key) {
		return map.get(key);
	}
}