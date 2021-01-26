package com.carlos.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class FileParser {
	private FileManager fileManager = new FileManager();
	private JSONArray dataList;
	private JSONObject data;
	
	
	public void parseFile(String path) throws Exception {
		String text = fileManager.readFile(path);
		if(text.startsWith("<?xml")) text = text.replace("sample", "httpSample");
		boolean isXML = false;
		try {
			data = XML.toJSONObject(text);		
		} catch (JSONException e) {}
		isXML = data.keySet().size()>0;
		if(isXML) {
			try {
				if(data.getJSONObject("testResults").getJSONArray("httpSample").length()==0) {
					throw new Exception("Não há dados no arquivo inserido.");
				}
				JSONObject firstObject = data.getJSONObject("testResults").getJSONArray("httpSample").getJSONObject(0);
				if(!firstObject.has("s")) {
					throw new Exception("Não foi encontrado o atributo 'Success' no arquivo informado. Ele deve estar selecionado na configuração do Simple Data Writer.");
				}
				if(!firstObject.has("t")) {
					throw new Exception("Não foi encontrado o atributo 'Elapsed time' no arquivo informado. Ele deve estar selecionado na configuração do Simple Data Writer.");
				}
				if(!firstObject.has("na")) {
					throw new Exception("Não foi encontrado o atributo 'Active Thread Count' no arquivo informado. Ele deve estar selecionado na configuração do Simple Data Writer.");
				}
				if(!firstObject.has("ts")) {
					throw new Exception("Não foi encontrado o atributo 'Time Stamp' no arquivo informado. Ele deve estar selecionado na configuração do Simple Data Writer.");
				}
				dataList = data.getJSONObject("testResults").getJSONArray("httpSample");
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("O arquivo informado não possui o formato esperado.");
			}
		} else {
			String lines[] = text.split("\n");
			String separator = ",";
			List<String> titles = Arrays.asList(lines[0].split(separator));
			dataList = new JSONArray();
			if(!titles.contains("timeStamp") ||
					!titles.contains("elapsed") ||
					!titles.contains("success") ||
					!titles.contains("allThreads")
					) {
				throw new Exception("Não foi encontrado as colunas esperadas.");
			}
			String expectedTitles[] = {"timeStamp", "elapsed", "success", "allThreads", "label"};
			Map<String, Integer> mapTitle = new HashMap<String, Integer>();
			for(int i = 0; i<expectedTitles.length; i++) {
				int index = titles.indexOf(expectedTitles[i]);
				if(index>=0) {
					mapTitle.put(expectedTitles[i], index);
				}
			}
			List<String> keys = new ArrayList<String>(mapTitle.keySet());
			for(int i=1; i<lines.length; i++) {
				String values[];
				if(lines[i].startsWith("\"")) {
					values = getValues(lines[i]);
				} else {
					values = treatLine(lines[i]).split(separator);
				}
				JSONObject item = new JSONObject();
				for(int j=0; j<keys.size(); j++) {
					String key = keys.get(j);
					if(key.equals("timeStamp")) {
						item.put(titleConverter(key), toMilliseconds(values[mapTitle.get(key)]));
					} else if(key.equals("allThreads") || key.equals("elapsed")) {
						item.put(titleConverter(key), Integer.parseInt(values[mapTitle.get(key)]));
					} else if(key.equals("success") ) {
						item.put(titleConverter(key), Boolean.parseBoolean(values[mapTitle.get(key)]));
					} else {
						item.put(titleConverter(key), values[mapTitle.get(key)]);
					}
				}
				dataList.put(item);
			}
		}
	}
	
	private String treatLine(String line) {
		if(!line.contains("\"")) return line;
		do {
			int quoteIndex = line.indexOf("\"");
			for(int i = quoteIndex+1; i< line.length(); i++) {
				if(line.charAt(i) == ',') {
					line = line.substring(0, i) + line.substring(i+1);
				} else if(line.charAt(i)=='"') {
					line = line.replaceFirst("\"", "");
					line = line.replaceFirst("\"", "");
					break;
				}
			}
		} while (line.contains("\""));
		return line;
	}
	
	private String[] getValues(String text) {
		int c = 0;
		int index = 1;
		String currentValue = "";
		List<String> valuesList = new ArrayList<String>();
		while(c != 3) {
			if(c == 0) {
				if(text.charAt(index)==',') {
					valuesList.add(currentValue);
					currentValue = "";
				} else if(text.charAt(index)=='\"') {
					currentValue = "";
					index++;
					c = 1;
				} else {
					currentValue = currentValue + text.charAt(index);
				}
			} else if(c == 1) {
				if(text.charAt(index)=='\"') {
					valuesList.add(currentValue);
					index+=2;
					c = 2;
					currentValue = "";
				} else {
					currentValue = currentValue + text.charAt(index);
				}
			} else if(c ==2) {
				if(text.charAt(index)==',') {
					valuesList.add(currentValue);
					currentValue = "";
				} else if(text.charAt(index)=='\"') {
					valuesList.add(currentValue);
					c = 3;
				} else {
					currentValue = currentValue + text.charAt(index);
				}
			}
			index++;
		}
		return valuesList.stream().toArray(String[]::new);
	}
	
	private String titleConverter(String title) throws Exception {
		switch (title) {
		case "timeStamp":
			return "ts";
		case "elapsed":
			return "t";
		case "label":
			return "lb";
		case "success":
			return "s";
		case "allThreads":
			return "na";
		}
		throw new Exception("Title não encontrado.");
	}
	
	private Long toMilliseconds(String myDate) throws ParseException {
		String format = PropertiesReader.getProperty("jmeter.save.saveservice.timestamp_format");
		if(format.equals("ms")) {
			return Long.parseLong(myDate);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = sdf.parse(myDate);
		return date.getTime();
	}
	
	public JSONObject getData() {
		return data;
	}

	public JSONArray getDataList() {
		return dataList;
	}
}