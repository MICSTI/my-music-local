package at.micsti.mymusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
		    	   // second line is modification timestamp
		    	   modification = Long.valueOf(line.trim());
		       } else if (count == 3) {
		    	   // third line is last imported played id
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
		List<Song> songs = dbConnector.getSongs();
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
