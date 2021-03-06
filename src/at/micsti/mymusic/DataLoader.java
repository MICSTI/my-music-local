package at.micsti.mymusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.json.JSONException;
import org.json.JSONObject;
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
	
	// xml writer
	private XmlWriter xmlWriter;
	
	// MediaMonkey reference date
	private final static DateTimeZone AUSTRIA = DateTimeZone.forID("Europe/Vienna");
	private final static DateTime mmRefDate = new DateTime(1900, 1, 1, 0, 0, 0, AUSTRIA);
	
	public DataLoader() {
		
	}
	
	public boolean performUpdate() {
		boolean success = true;
		
		// init values
		initValues();
		
		// read config file
		readConfigFile();
		
		// retrieve values from database
		getValuesFromDatabase();
		
		// get database file modification time
		modification = getFileModification(dbPath);
		modification /= 1000;
		
		// write xml
		writeXml();
		
		// upload file
		if (!uploadFile())
			success = false;
		
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
		
		// set values
		dbConnector.setModifiedTime(convert2MMDate(modification));
		
		dbConnector.setPlayedId(playedId);
		
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
		xmlWriter = new XmlWriter();
		
		xmlWriter.setDbModification(modification);
		xmlWriter.setSongs(songs);
		xmlWriter.setPlayeds(playeds);
		xmlWriter.setPlayedId(playedId);
		
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
		
		try {
			String xmlString = xmlWriter.getXmlString();
			
			// url post parameters
			String urlParameters = "xmldata=" + URLEncoder.encode(xmlString, "UTF-8");
			
			URL url = new URL(uploadUrl);
			
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
		
			// set request headers
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("CACHE-CONTROL", "no-cache");
			httpConnection.setRequestProperty("Content-Length", "" + 
		               Integer.toString(urlParameters.getBytes().length));
			httpConnection.setRequestProperty("Accept", "application/json");
			
			// add xml data
			DataOutputStream dos = new DataOutputStream(httpConnection.getOutputStream());
			dos.writeBytes(urlParameters);
			dos.flush();
			dos.close();
						
			// get insput stream
			BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			
			String inLine;
			
			while ((inLine = br.readLine()) != null) {
				sb.append(inLine);
			}
			
			// get JSON response body
			JSONObject jsonResponse = new JSONObject(sb.toString());
			
			// get message from JSON
			String status = jsonResponse.getString("status");
			String message = jsonResponse.getString("message");
			
			// get response properties
			int statusCode = httpConnection.getResponseCode();
			
			if (statusCode != 200 || !status.equals("success")) {
				success = false;
				System.out.println(message);
			}
			
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
		} catch (JSONException e) {
			// TODO Auto-generated catch block
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
	
	private long convert2Timestamp(double mmDate) {
		int fullDays = (int)Math.floor(mmDate);
		
		double dayPortion = mmDate - fullDays;
		
		double hours = dayPortion * 24;
		double minutes = (hours - Math.floor(hours)) * 60;
		double seconds = (minutes - Math.floor(minutes)) * 60;
		
		DateTime result = mmRefDate.plusDays(fullDays)
								   .minusDays(2)
								   .plusHours((int)Math.floor(hours))
								   .plusMinutes((int)Math.floor(minutes))
								   .plusSeconds((int)Math.floor(seconds));
		
		long timestamp = result.getMillis() / 1000L;
		
		return timestamp;
	}
	
	private double convert2MMDate(long timestamp) {
		DateTime compare = new DateTime(timestamp * 1000L);
		
		Days dayInterval = Days.daysBetween(mmRefDate, compare);
		
		DateTime dayPortion = compare.minusDays(dayInterval.getDays());
		
		int hours = dayPortion.getHourOfDay();
		int minutes = dayPortion.getMinuteOfHour();
		int seconds = dayPortion.getSecondOfMinute();
		
		double mmDate = dayInterval.getDays() + 2;
		
		mmDate += (hours / 24d);
		mmDate += (minutes / (60d * 24d));
		mmDate += (seconds / (60d * 60d * 24d));
		
		return mmDate;
	}
}
