package at.micsti.mymusic;

public class MyMusicLocalApp {
	
	
	public static void main(String[] args) {
		DataLoader dataLoader = new DataLoader();
		
		dataLoader.readConfigFile();
		dataLoader.writeConfigFile();
		
	}

}
