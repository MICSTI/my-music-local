package at.micsti.mymusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataLoader {
	
	// config file name
	private static String CONFIG_FILE = "my-music-config.txt";
	
	// modification timestamp
	private long modification;
	
	// last imported played id
	private int playedId;
	
	public DataLoader() {
		// init values
		initValues();
	}
	
	public void readConfigFile() {
		try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
		    String line;
		    int count = 1;
		    
		    while ((line = br.readLine()) != null) {
		       if (count == 1) {
		    	   // first line is modification timestamp
		    	   modification = Long.valueOf(line.trim());
		       } else if (count == 2) {
		    	   // second line is last imported played id
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
	
	public void writeConfigFile() {
		BufferedWriter out = null;
		
		try {
			FileWriter fstream = new FileWriter(CONFIG_FILE);
			out = new BufferedWriter(fstream);
			
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

	public long getModification() {
		return modification;
	}

	public void setModification(long modification) {
		this.modification = modification;
	}

	public int getPlayedId() {
		return playedId;
	}

	public void setPlayedId(int playedId) {
		this.playedId = playedId;
	}

}
