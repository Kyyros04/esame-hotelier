
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Estensione di Monitor. Utilizzato per la gestione degli hotel e condiviso tra i thread. Permette storage e load degli hotel in memoria permanente e aggiornare periodicamente in base all'intervallo di tempo settato.
 * @author edoardo
 *
 */
public class MonitorHotels extends Monitor{

	final static String filePath="Hotels.json";

	LinkedList<Hotel> hotels = new LinkedList<Hotel>();

	DatagramSocket socket;
	
	String ipGroup;
	int portGroup;

	Hotel.Rank bestRank;

	public MonitorHotels(DatagramSocket socket,String ipGroup, int portGroup) {
		this.socket=socket;
		this.ipGroup=ipGroup;
		this.portGroup=portGroup;
	}

	/**
	 * Override della classe astratta Monitor. Carica dalla memoria permanente la lista degli hotel di Hotelier. 
	 */
	public void Load(){
		try {
			FileInputStream fileInput = new FileInputStream(filePath);
			BufferedInputStream in = new BufferedInputStream(fileInput);

			String textForJson = "";
			byte[] nbyte;

			while((nbyte = in.readNBytes(256)).length>0) {
				String temp = new String(nbyte,ServiceMethods.ENCODE);
				textForJson+=temp;
			}
			in.close();
			fileInput.close();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			Type typeListHotels = new TypeToken<LinkedList<Hotel>>() {}.getType();

			hotels = gson.fromJson(textForJson, typeListHotels);

			//PrintHotels();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Override della classe astratta Monitor. Carica nella memoria permanente la lista degli hotel di Hotelier. 
	 */
	public void Store(){
		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			String JsonText = gson.toJson(hotels);

			FileOutputStream fileOutput = new FileOutputStream(filePath, false);
			BufferedOutputStream fileBuffered = new BufferedOutputStream(fileOutput);

			fileBuffered.write(JsonText.getBytes(ServiceMethods.ENCODE));

			Hotel.Rank tempRank = GetRankHotels();
			if((tempRank!=null && bestRank==null) || (tempRank!=null && !tempRank.nameHotel.equals(bestRank.nameHotel))) {
				bestRank=tempRank;
				String message = String.format("L'hotel con il rank più alto è stato aggiornato, adesso è: %s", bestRank.nameHotel);
				PublishMessage(message);
			}

			fileBuffered.close();
			fileOutput.close();

		}catch(Exception e) {e.printStackTrace();}
	}
	/**
	 * Aggiunge una recensione all'hotel
	 * @param nameHotel Nome hotel
	 * @param city Nome della città
	 * @param rev Recensione 
	 * @return Restituisce il messaggio 
	 */
	public synchronized String AddReview(String nameHotel, String city, Review rev) {
		for(Hotel h : hotels) {
			if(h.name.equals(nameHotel) && h.city.equals(city)) {
				h.InsertReview(rev);
				return "recensione pubblicata";
			}
		}
		return "l'hotel da recensire non è stato trovato";
	}
	/**
	 * Calcola il ranking di tutti gli hotel memorizzati nel sistema e restituisce quello più alto.
	 * @return Restituisce il rank dell'hotel più alto.
	 */
	private Hotel.Rank GetRankHotels() {

		Hotel.Rank bestRank=null;

		for( Hotel h : hotels) {
			Hotel.Rank tempRank = Hotel.Rank.CalculateScores(h);
			if(tempRank==null) continue;
			bestRank = tempRank.CompareRanking(bestRank);
		}

		return bestRank;
	}
	/**
	 * Calcola il ranking di tutti gli hotel situati nella città, passato come parametro, e restituisce quello più alto.
	 * @param città Il nome della città dove risiedono gli hotel su cui calcolare il ranking locale.
	 * @return Restituisce il rank dell'hotel più alto.
	 */
	private Hotel.Rank GetRankHotels(String città) {

		Hotel.Rank bestRank=null;

		for( Hotel h : hotels) {
			if(!h.city.equals(città)) continue;
			Hotel.Rank tempRank = Hotel.Rank.CalculateScores(h);
			if(tempRank==null) continue;
			bestRank = tempRank.CompareRanking(bestRank);
		}

		return bestRank;
	}

	/**
	 * Notifica tutti i clienti attraverso connessione UDP, multicast
	 * @param message stringa che rappresenta il contenuto da inviare ai clienti.
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IOException
	 */
	public void PublishMessage(String message) throws IllegalArgumentException,UnsupportedEncodingException,SecurityException,IOException{
		byte[] data = message.getBytes(ServiceMethods.ENCODE);
		InetSocketAddress group = new InetSocketAddress(ipGroup,portGroup);
		DatagramPacket response = new DatagramPacket(data, data.length, group);
		socket.send(response);
	}
}
