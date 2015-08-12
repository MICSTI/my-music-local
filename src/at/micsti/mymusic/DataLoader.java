package at.micsti.mymusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class DataLoader {
	
	// config file name
	private static String CONFIG_FILE = "my-music-config.txt";
	
	// database connector
	private DbConnector dbConnector;
	
	// MediaMonkey database file path
	private String dbPath;
	
	// modification timestamp
	private long modification;
	
	// last imported played id
	private int playedId;
	
	// upload url
	private String uploadUrl;
	
	// songs list
	private List<Song> songs;
	
	// playeds list
	private List<Played> playeds;
	
	// result file path
	private String resultFilePath; 
	
	// xml document
	private Document xmlDocument;
	
	public DataLoader() {
		
	}
	
	public boolean performUpdate() {
		boolean success = true;
		
		// init values
		initValues();
		
		// read config file
		readConfigFile();
		
		// get database file modification time
		modification = getFileModification(dbPath);
		modification /= 1000;
		
		// retrieve values from database
		getValuesFromDatabase();
		
		// write xml
		writeXml();
		
		// write config file
		writeConfigFile();
		
		return success;
	}
	
	private void readConfigFile() {
		try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
		    String line;
		    int count = 1;
		    
		    while ((line = br.readLine()) != null) {
		       if (count == 1) {
		    	   // first line is MediaMonkey database path
		    	   dbPath = line.trim();
		       } else if (count == 2) {
		    	   // second line is result file path
		    	   resultFilePath = line.trim();
		       } else if (count == 3) {
		    	   // third line is the upload url
		    	   uploadUrl = line.trim();
		       } else if (count == 4) {
		    	   // fourth line is modification timestamp
		    	   modification = Long.valueOf(line.trim());
		       } else if (count == 5) {
		    	   // fifth line is last imported played id
		    	   playedId = Integer.valueOf(line.trim());
		       }
		       
		       count++;
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getValuesFromDatabase() {
		// init database connector
		dbConnector = new DbConnector();
		dbConnector.init(dbPath);
		
		// get songs
		songs = dbConnector.getSongs();
		
		// get playeds
		playeds = dbConnector.getPlayeds();
		
		// set last played id
		if (playeds.size() > 0) {
			int lastPlayedId = playeds.get(playeds.size() - 1).getId();
			
			if (lastPlayedId > playedId)
				playedId = lastPlayedId;
		}
	}
	
	private void writeXml() {
		XmlWriter xmlWriter = new XmlWriter();
		
		xmlWriter.setDbModification(modification);
		xmlWriter.setSongs(songs);
		xmlWriter.setPlayeds(playeds);
		
		xmlDocument = xmlWriter.getXmlDocument();
		
		// write document to file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			
			DOMSource source = new DOMSource(xmlDocument);
			StreamResult streamResult = new StreamResult(new File(resultFilePath));
			
			transformer.transform(source, streamResult);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean uploadFile() {
		boolean success = true;
		
		int b_read = 0;
		char[] buffer = new char[1024 * 10];
		
		try {
			FileReader fr = new FileReader(xmlDocument.toString());
			
			URL url = new URL(uploadUrl);
			
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		
			// set request headers
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Content-Type", "text/xml");
			httpConnection.setRequestProperty("CACHE-CONTROL", "no-cache");
			
			// add xml data
			OutputStreamWriter osWriter = new OutputStreamWriter(httpConnection.getOutputStream());
			
			while ((b_read = fr.read(buffer)) != -1) {
				osWriter.write(buffer, 0, b_read);
			}
			
			// close reader and writer
			osWriter.flush();
			osWriter.close();
			fr.close();
			
			// get response properties
			int statusCode = httpConnection.getResponseCode();
			
			System.out.println(statusCode);
			
			// close connection
			httpConnection.disconnect();
		} catch (MalformedURLException e) {
			success = false;
			
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			success = false;
			
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			
			e.printStackTrace();
		}
		
		return success;
	}
	
	private long getFileModification(String path) {
		File file = new File(path);
		
		return file.lastModified();
	}
	
	private void writeConfigFile() {
		BufferedWriter out = null;
		
		try {
			FileWriter fstream = new FileWriter(CONFIG_FILE);
			out = new BufferedWriter(fstream);
			
			// MediaMonkey database path
			out.write(dbPath);
			
			// new line
			out.write(System.lineSeparator());
			
			// result file path
			out.write(resultFilePath);
			
			// new line
			out.write(System.lineSeparator());
			
			// upload url
			out.write(uploadUrl);
			
			// new line
			out.write(System.lineSeparator());
			
			// modification timestamp
			out.write(String.valueOf(modification));
			
			// new line
			out.write(System.lineSeparator());
			
			// played id
			out.write(String.valueOf(playedId));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void initValues() {
		modification = 0;
		playedId = 0;
	}
}
