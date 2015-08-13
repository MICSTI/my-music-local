package at.micsti.mymusic;

public class MyMusicLocalApp {
	
	public static void main(String[] args) {
		DataLoader dataLoader = new DataLoader();
		
		boolean success = dataLoader.performUpdate();
		
		if (success)
			System.out.println("Update successful");
		else
			System.out.println("Update was not successful");
	}

}
