package com.carlos.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessData {
	private boolean ignoreRampDown;
	private FileManager fileManager = new FileManager();
	
	public void processGroups(boolean ignoreRampDown, JSONArray filteredData, List<Integer> selectedGroups, String label) throws Exception {
		this.ignoreRampDown = ignoreRampDown;
		Map<Integer, Integer> averageTime = processAverageTime(filteredData, selectedGroups);
		Map<Integer, Double> errorRate = processAverageErrorRate(filteredData, selectedGroups);
		fileManager.toFile(resultsToText(label, averageTime, errorRate));
	}
	
	public void processAverage(JSONArray data, String granularity, boolean showVu, String selectedLabel) throws Exception {
		List<String> moment = new ArrayList<String>();
		List<Integer> time = new ArrayList<Integer>();
		List<Integer> vu = new ArrayList<Integer>();
		int gran = Integer.parseInt(granularity)*1000;
		Long current = data.getJSONObject(0).getLong("ts");
		int count = 0;
		int sumVU = 0;
		int sumTime = 0;
		Date start = new SimpleDateFormat("HH:mm").parse("00:00");
		
		for(int i=0; i<data.length(); i++) {
			JSONObject item = data.getJSONObject(i);
			if(current+gran<item.getLong("ts")) {
				time.add((int)(sumTime/(double)count));
				if(showVu) {
					vu.add((int)(sumVU/(double)count));
					sumVU = item.getInt("na");
				}
				moment.add(new SimpleDateFormat("HH:mm").format(start));
				start = addTime(start, gran/1000);
				count = 1;
				current = item.getLong("ts");
				sumTime = item.getInt("t");
			} else {
				sumTime+=item.getInt("t");
				if(showVu) {
					sumVU+=item.getInt("na");
				}
				count++;
			}
			
		}
		fileManager.toFile(resultsToText(time, vu, selectedLabel, showVu, moment));
	}  

	private Date addTime(Date start, int gran){ 
		  Calendar cal = Calendar.getInstance();
		  cal.setTime(start);
		  cal.add(Calendar.SECOND, gran);
		  start = cal.getTime();
		  return start;
	}
	
	private String resultsToText(List<Integer> time, List<Integer> vu, String label, boolean showVu, List<String> moment) {
		StringBuilder result = new StringBuilder();
		result.append("Resultados obtidos para a label '" + label + "':\r\n\r\n");
		result.append("Tempo de resposta médio por quantidade de usuários simultâneos(v.u.)\r\n\r\n");
		if(showVu) {			
			result.append("**O conteúdo das linhas abaixos estão separadas por tabs (\\t), assim você pode copiar e colar com falicade no Excel ou similares.**\r\n\r\n");
			result.append("Momento\tTempo médio\tUsuários simultâneos\r\n");
		} else {
			result.append("**Copie e cole o conteúdo abaixo no Excel ou similares.**\r\n\r\n");
			result.append("Momento\tTempo médio\r\n");
		}
		
		for(int i=0;i<time.size();i++) {
			result.append(moment.get(i)+"\t"+time.get(i));
			if(showVu) {
				result.append("\t"+vu.get(i)+"\r\n");
			} else {
				result.append("\r\n");
			}
		}
		return result.toString();
	}

	private String resultsToText(String label, Map<Integer, Integer> averageTime, Map<Integer, Double> errorRate) {
		StringBuilder result = new StringBuilder();
		if (label != null) {
			if (label.equals("[todos]")) {
				result.append("Resultados obtidos para todas as transações:\r\n\r\n");	
			} else {
				result.append("Resultados obtidos para a label '" + label + "':\r\n\r\n");				
			}
		} else {
			result.append("Resultados obtidos:\r\n\r\n");
		}
		result.append("**O conteúdo das linhas abaixos estão separadas por tabs (\\t), assim você pode copiar e colar com facilidade no Excel ou similares.**\r\n\r\n");
		result.append("Tempo de resposta médio por quantidade de usuários simultâneos(v.u.)\r\n\r\n");
		result.append("Qtd. usuários simultâneos\tTempo médio(ms)\r\n");
		for (Integer key : averageTime.keySet()) {
			result.append(key + " v.u.\t" + averageTime.get(key) + "\r\n");
		}
		result.append("\r\n");
		result.append("Taxa de erro das transações em porcentagem por quantidade de usuários simultâneos:\r\n\r\n");
		result.append("Qtd. usuários simultâneos\tTaxa de erro em %\r\n");
		for (Integer key : errorRate.keySet()) {
			result.append(key + " v.u.\t" + errorRate.get(key) + "\r\n");
		}
		return result.toString();
	}

	private Map<Integer, Integer> processAverageTime(JSONArray filteredData, List<Integer> selectedGroups) {
		Collections.sort(selectedGroups);
		int sum[] = new int[selectedGroups.size()];
		int qtd[] = new int[selectedGroups.size()];
		int max = 0;
		for (int i = 0; i < filteredData.length(); i++) {
			JSONObject item = filteredData.getJSONObject(i);
			int ng = item.getInt("na");
			if (ignoreRampDown) {
				if (ng < max) {
					break;
				}
				max = ng;
			}
			if (selectedGroups.contains(ng)) {
				int index = selectedGroups.indexOf(ng);
				sum[index] = sum[index] + item.getInt("t");
				qtd[index] = qtd[index] + 1;
			}
		}
		Map<Integer, Integer> averageTime = new TreeMap<Integer, Integer>();
		for (Integer group : selectedGroups) {
			int index = selectedGroups.indexOf(group);
			averageTime.put(group, (int)(sum[index] / (double) qtd[index]));
		}
		return averageTime;
	}

	private Map<Integer, Double> processAverageErrorRate(JSONArray filteredData, List<Integer> selectedGroups) {
		Collections.sort(selectedGroups);
		int errors[] = new int[selectedGroups.size()];
		int samples[] = new int[selectedGroups.size()];
		int max = 0;
		for (int i = 0; i < filteredData.length(); i++) {
			JSONObject item = filteredData.getJSONObject(i);
			int ng = item.getInt("na");
			if (ignoreRampDown) {
				if (ng < max) {
					break;
				}
				max = ng;
			}
			if (selectedGroups.contains(ng)) {
				int index = selectedGroups.indexOf(ng);
				if (!item.getBoolean("s")) {
					errors[index] = errors[index] + 1;
				}
				samples[index] = samples[index] + 1;
			}
		}
		Map<Integer, Double> averageTime = new TreeMap<Integer, Double>();
		for (Integer group : selectedGroups) {
			int index = selectedGroups.indexOf(group);
			averageTime.put(group, 100 * errors[index] / (double) samples[index]);
		}
		return averageTime;
	}
}