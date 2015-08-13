package at.micsti.mymusic;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class DbConnector {
	
	private static String driverName = "org.sqlite.JDBC";
	private static String jdbc = "jdbc:sqlite";
	
	// database file path
	private String path;
	
	// database url
	private String dbUrl;
	
	// database connection
	private Connection connection;
	
	// played id
	private int playedId;
	
	// modified time
	private double modifiedTime;
	
	// SQL queries
	private static String SONGS_QUERY = "SELECT * FROM Songs WHERE FileModified >= ";
	private static String PLAYEDS_QUERY = "SELECT * FROM Played WHERE IDPlayed > ";

	public DbConnector() {
		
	}
	
	public boolean init(String dbPath) {
		path = dbPath;
		
		// build database url
		dbUrl = jdbc + ":" + path;
		
		return createConnection();
	}
	
	public List<Song> getSongs() {
		List<Song> songs = new ArrayList<Song>();
		
		String songQuery = SONGS_QUERY + String.valueOf(modifiedTime);
		
		try {
			Statement stmt = connection.createStatement();
			
			try {
				ResultSet result = stmt.executeQuery(songQuery);
				
				try {
					while (result.next()) {
						int id = result.getInt("ID");
						String name = result.getString("SongTitle");
						String artistName = result.getString("Artist");
						int discNo = result.getInt("DiscNumber");
						int trackNo = result.getInt("TrackNumber");
						int rating = result.getInt("Rating");
						int bitrate = result.getInt("Bitrate");
						String dateAdded = result.getString("DateAdded");
						long length = result.getInt("SongLength");
						
						String recordName = result.getString("Album");
						
						// if record name is empty, try to get it from AlbumArtist
						if (recordName.isEmpty()) {
							recordName = result.getString("AlbumArtist");
						}
						
						// add attributes to song
						Song song = new Song();
						
						song.setId(id);
						song.setName(name);
						song.setArtistName(artistName);
						song.setDiscNo(discNo);
						song.setTrackNo(trackNo);
						song.setRating(rating);
						song.setBitrate(bitrate);
						song.setDateAdded(dateAdded);
						song.setLength(length);
						song.setRecordName(recordName);
					
						// add song to list
						songs.add(song);
					}
				} finally {
					try { result.close(); } catch (Exception e) { e.printStackTrace(); }
				}
			} finally {
				try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return songs;
	}
	
	public List<Played> getPlayeds() {
		List<Played> playeds = new ArrayList<Played>();

		String playedQuery = PLAYEDS_QUERY + playedId;
		
		try {
			Statement stmt = connection.createStatement();
			
			try {
				ResultSet result = stmt.executeQuery(playedQuery);
				
				try {
					while (result.next()) {
						int id = result.getInt("IDPlayed");
						int mmid = result.getInt("IDSong");
						
						double playDate = result.getDouble("PlayDate");
						double utcOffset = result.getDouble("UTCOffset");
						
						double timestamp = playDate + utcOffset;
						
						// add attributes to played
						Played played = new Played();
						
						played.setId(id);
						played.setMmId(mmid);
						played.setTimestamp(timestamp);
					
						// add played to list
						playeds.add(played);
					}
				} finally {
					try { result.close(); } catch (Exception e) { e.printStackTrace(); }
				}
			} finally {
				try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return playeds;
	}
	
	private boolean createConnection() {
		try {
			connection = DriverManager.getConnection(dbUrl);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public int getPlayedId() {
		return playedId;
	}

	public void setPlayedId(int playedId) {
		this.playedId = playedId;
	}

	public double getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(double modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	
}
