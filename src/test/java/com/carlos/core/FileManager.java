package com.carlos.core;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class FileManager {
	
	public String readFile(String path) throws IOException {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		return new String(data, "UTF-8");
	}
	
	public void toFile(String text) throws Exception {
		Random r = new Random();
		File outFile;
		String outPath = System.getProperty("java.io.tmpdir") + r.nextInt(10000) + System.currentTimeMillis() + ".txt";
		try {
			outFile = new File(outPath);
		    PrintWriter out = new PrintWriter(outFile, "UTF-8");
		    out.print(text);
		    out.close();
		} catch (Exception e) {
			throw new Exception("Não foi possível criar o arquivo com o resultados. Verifique com o desenvolvedor.");
		}
	    if(!outFile.exists() || !outFile.isFile()) {
	    	throw new Exception("Não foi possível criar o arquivo com o resultados. Verifique com o desenvolvedor.");
	    }
	    try {
	    	Desktop.getDesktop().open(outFile);
		} catch (Exception e) { }
	    
	}
	
}
