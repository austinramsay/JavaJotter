package com.austinramsay.javajotter;

import java.io.*;
import java.util.*;

public class ConfigManager {

	private final String configFilename = ".javajotter";
	private File appConfig;
	private FileInputStream propsInput;
	private FileOutputStream propsOutput;
	private Properties prop;
	private Writer inputStream;

	public ConfigManager() {
		appConfig = new File(configFilename);
		prop = new Properties();

		/*
		try {
			//inputStream = new FileWriter(appConfig);
			// prop.load(propsInput);
		} catch (IOException e) {
			System.out.println("There was an error initializing the ConfigManager.");
			e.printStackTrace();
			System.exit(1);
		}*/
	}

	public boolean loadConfig() {
		try {
			propsInput = new FileInputStream(configFilename);
			prop.load(propsInput);
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (Exception e) {
			System.out.println("Failed to load props file.");
			e.printStackTrace();
			return false;
		}
	}

	public String readServerAddress() {
		return prop.getProperty("serverAddress");
	}

	public String getSystemOS() {
		return System.getProperty("os.name");
	}
}