package at.micsti.mymusic;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlWriter {
	
	private Document xmlDocument;
	private List<Song> songs;
	private List<Played> playeds;
	private int playedId;
	private long dbModification;
	
	public XmlWriter() {
		
	}
	
	public Document getXmlDocument() {
		buildXmlDocument();
		
		return xmlDocument;
	}
	
	public String getXmlString() {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
			
			return writer.getBuffer().toString().replaceAll("\n|\r", "");
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void buildXmlDocument() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		
		try {
			docBuilder = docFactory.newDocumentBuilder();
			
			// create document
			Document doc = docBuilder.newDocument();
			
			// root element
			Element rootElement = doc.createElement("desktop");
			doc.appendChild(rootElement);
			
			// song parent element
			Element songElement = doc.createElement("songs");
			rootElement.appendChild(songElement);
			
			// song elements
			for (Song s : songs) {
				Element song = doc.createElement("song"); 
				
				// name
				Element name = doc.createElement("name");
				name.appendChild(doc.createTextNode(s.getName()));
				song.appendChild(name);
				
				// artist
				Element artist = doc.createElement("artist");
				artist.appendChild(doc.createTextNode(s.getArtistName()));
				song.appendChild(artist);
				
				// record
				Element record = doc.createElement("record");
				record.appendChild(doc.createTextNode(s.getRecordName()));
				song.appendChild(record);
				
				// length
				Element length = doc.createElement("length");
				length.appendChild(doc.createTextNode(String.valueOf(s.getLength())));
				song.appendChild(length);
				
				// bitrate
				Element bitrate = doc.createElement("bitrate");
				bitrate.appendChild(doc.createTextNode(String.valueOf(s.getBitrate())));
				song.appendChild(bitrate);
				
				// mmid
				Element mmid = doc.createElement("mmid");
				mmid.appendChild(doc.createTextNode(String.valueOf(s.getId())));
				song.appendChild(mmid);
				
				// track no
				Element trackno = doc.createElement("trackno");
				trackno.appendChild(doc.createTextNode(String.valueOf(s.getTrackNo())));
				song.appendChild(trackno);
				
				// disc no
				Element discno = doc.createElement("discno");
				discno.appendChild(doc.createTextNode(String.valueOf(s.getDiscNo())));
				song.appendChild(discno);
				
				// rating
				Element rating = doc.createElement("rating");
				rating.appendChild(doc.createTextNode(String.valueOf(s.getRating())));
				song.appendChild(rating);
				
				// date added
				Element dateAdded = doc.createElement("added");
				dateAdded.appendChild(doc.createTextNode(s.getDateAdded()));
				song.appendChild(dateAdded);
				
				// append song to songs
				songElement.appendChild(song);
			}
			
			// played parent element
			Element playedElement = doc.createElement("playeds");
			rootElement.appendChild(playedElement);
			
			// played elements
			for (Played p : playeds) {
				Element played = doc.createElement("played");
				
				// played id
				Element pldid = doc.createElement("pldid");
				pldid.appendChild(doc.createTextNode(String.valueOf(p.getId())));
				played.appendChild(pldid);
				
				// MediaMonkey id
				Element mmid = doc.createElement("mmid");
				mmid.appendChild(doc.createTextNode(String.valueOf(p.getMmId())));
				played.appendChild(mmid);
				
				// timestamp
				Element timestamp = doc.createElement("timestamp");
				timestamp.appendChild(doc.createTextNode(String.valueOf(p.getTimestamp())));
				played.appendChild(timestamp);
				
				// append played to playeds
				playedElement.appendChild(played);
			}
			
			// config values
			Element configElement = doc.createElement("config");
			rootElement.appendChild(configElement);
			
			// MediaMonkey database modification time
			Element dbModificationTimestamp = doc.createElement("mm_db_modification");
			dbModificationTimestamp.appendChild(doc.createTextNode(String.valueOf(dbModification)));
			configElement.appendChild(dbModificationTimestamp);
			
			// last played id
			int last_played_id = -1;
			
			if (playeds.size() > 0) {
				last_played_id = playeds.get(playeds.size() - 1).getId();
			}
			
			Element lastPlayedId = doc.createElement("last_imported_played_id");
			lastPlayedId.appendChild(doc.createTextNode(String.valueOf(last_played_id)));
			configElement.appendChild(lastPlayedId);
			
			xmlDocument = doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Song> getSongs() {
		return songs;
	}

	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}

	public List<Played> getPlayeds() {
		return playeds;
	}

	public void setPlayeds(List<Played> playeds) {
		this.playeds = playeds;
	}

	public int getPlayedId() {
		return playedId;
	}

	public void setPlayedId(int playedId) {
		this.playedId = playedId;
	}

	public long getDbModification() {
		return dbModification;
	}

	public void setDbModification(long dbModification) {
		this.dbModification = dbModification;
	}
	
}
