package com.carlos.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
	private static Properties prop = null;
	
	private static void readProperties() {
		try (InputStream input = new FileInputStream("config.properties")) {
	        prop = new Properties();
	        prop.load(input);
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
	
	public static String getProperty(String name) {
		return getProp().getProperty(name);
	}

	private static Properties getProp() {
		if(prop == null) {
			readProperties();
		}
		return prop;
	}
}