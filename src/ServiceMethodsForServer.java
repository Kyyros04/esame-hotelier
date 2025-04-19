import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Estende Service Methods. Affronta e risponde ai servizi richiesti dagli utenti.
 * @author edoardo
 *
 */
public class ServiceMethodsForServer extends ServiceMethods{
	MonitorAccounts accounts;
	MonitorHotels hotels;
	/**
	 * 
	 * @param obj
	 * @param key
	 * @param accounts
	 * @param hotels
	 */
	public ServiceMethodsForServer(Object obj,SelectionKey key, MonitorAccounts accounts, MonitorHotels hotels) {
		super(obj,key);
		this.accounts=accounts;
		this.hotels=hotels;
	}

	protected boolean DoRegister() {
		try {
			String bodyRequest = ReadMessage();
			UserInfo account = SerializeJson.Deserialization(bodyRequest, UserInfo.class);

			String message="";
			if(!accounts.AddNewRegistration(account)){
				message="nome utente già preso";
			}{
				message="registrato";
			}

			ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(ENCODE));
			((SocketChannel) key.channel()).write(buffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoLogin(){
		try {
			String messageFromClient = ReadMessage();
			UserInfo accountToLogin = SerializeJson.Deserialization(messageFromClient, UserInfo.class);
			SocketChannel channel=(SocketChannel) key.channel();
			String nameUser = accounts.CompareAddress(channel);
			String messageFromServer;
			if(nameUser!=null) 
				messageFromServer="n"+SEPARATOR+"Per poter fare un nuovo login prima devi sloggare";
			else {
				if(accounts.SearchAccount(accountToLogin)){
					messageFromServer="y"+SEPARATOR+"Utente loggato";
					accounts.AddAccount(accountToLogin.userName, channel.getRemoteAddress());
				}else
				{
					messageFromServer="n"+SEPARATOR+"Credenziali errate";
				}
			}
			ByteBuffer buffer = ByteBuffer.wrap(messageFromServer.getBytes(ENCODE));
			channel.write(buffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoLogout(){
		try {
			SocketChannel channel = (SocketChannel) key.channel();
			String acc = accounts.CompareAddress(channel);
			if(acc!=null) {
				accounts.RemAccount(acc);
				acc="y"+SEPARATOR+"Utente sloggato";
			}else
				acc="n"+SEPARATOR+"Per sloggare dovevi essere già loggato";

			ByteBuffer buffer = ByteBuffer.wrap(acc.getBytes(ENCODE));
			channel.write(buffer);
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoSearchHotel() {
		try {
			String messageFromClient = ReadMessage();

			String[] argsClient = messageFromClient.split(SEPARATOR);
			System.out.println(messageFromClient);

			String messageFromServer;

			if(argsClient.length!=2) {
				messageFromServer="Numero argomenti non validi. Servono il nome dell'hotel e della città esistenti";
			}
			else
				messageFromServer = SearchHotel(argsClient[0], argsClient[1]);

			WriteMessage((SocketChannel) key.channel(), messageFromServer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoSearchAllHotels() {
		try {
			String messageFromClient = ReadMessage();

			String messageFromServer = SearchAllHotels(messageFromClient);

			WriteMessage((SocketChannel) key.channel(), messageFromServer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoInsertReview() {
		try {
			SocketChannel channel=(SocketChannel) key.channel();

			String messageFromClient = ReadMessage();

			String[] argsClient = messageFromClient.split(SEPARATOR);

			String messageFromServer;

			if(argsClient.length!=3) {
				messageFromServer="Argomenti non validi. Devono essere inseriti nome hotel, città e la recensione\n";
			}
			else
				if(IsUserLoged(channel)){
					String userName = accounts.CompareAddress(channel);
					messageFromServer=InsertReview(argsClient[0],argsClient[1],argsClient[2], userName);
				}
				else 
				{
					messageFromServer="L'utente deve essere loggato per poter inserire le recensioni";
				}

			ByteBuffer buffer = ByteBuffer.wrap(messageFromServer.getBytes(ENCODE));
			channel.write(buffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoShowMyBadges() {
		try {
			SocketChannel channel=(SocketChannel) key.channel();
			String acc = accounts.CompareAddress(channel);

			String messageFromServer;

			if(acc!=null) {
				var index = accounts.users.indexOf( new UserInfo(acc, "")  );
				var user = accounts.users.get(index);
				messageFromServer = String.valueOf(user.GetRate())+SEPARATOR+user.GetDistintivo();
			}
			else {
				messageFromServer = "account non trovato";
			}
			ByteBuffer buffer = ByteBuffer.wrap(messageFromServer.getBytes(ENCODE));
			channel.write(buffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Verifica che l'utente sia loggato, controllando la lista degli account loggati dalla risorsa condivisa MonitorAccounts.
	 * @param channel SocketChannel della connessione con l'utente.
	 * @return Restituisce true se l'utente è loggato, false altrimenti.
	 * @throws Exception
	 */
	private boolean IsUserLoged(SocketChannel channel) throws Exception{
		String acc = accounts.CompareAddress(channel);
		if(acc!=null) 
			return true;
		else
			return false;
	}

	/**
	 * Override. Prende il canale dalla key.channel() passato nel costruttore.
	 * Legge il messaggio dal canale e lo restituisce come stringa.
	 * @return Restituisce il messaggio in forma di stringa, dopo averlo letto dal canale.
	 */
	protected String ReadMessage(){
		SocketChannel channel =(SocketChannel) key.channel();
		try {
			
			ByteBuffer buffer = ByteBuffer.allocate(256);
			buffer.clear();
			channel.read(buffer);
			String bodyMessage = new String(buffer.array(),ENCODE).trim();
			return bodyMessage;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Cerca l'hotel richiesto dal cliente, considerando come parametri il nome dell'hotel e la città dove è situato.
	 * @param nomeHotel nome dell'hotel.
	 * @param città città dove è situato l'hotel.
	 * @return Restituisce i dettagli dell'hotel richiesto.
	 */
	private String SearchHotel(String nomeHotel, String città){
		Hotel hotel = new Hotel(nomeHotel, città);
		for(Hotel h : hotels.hotels) {
			if(h.equals(hotel)) {
				return h.toString();
			}
		}
		return "Hotel non trovato, ricontrollare i dati inseriti";
	}
	
	/**
	 * Cerca gli hotel richiesti dal cliente, considerando come parametro la città dove sono situati. Li ordina per punteggio di ranking locale decrescente.
	 * @param città città dove sono situati gli hotel.
	 * @return Restituisce gli hotels ordinati per ranking locale come stringa.
	 */
	private String SearchAllHotels(String città){
		String text="Hotels di "+ città+":\n\n";
		boolean find=false;
		
		LinkedList<Hotel> ranked = new LinkedList<Hotel>();
		
		for(Hotel h : hotels.hotels) {
			if(h.city.equals(città)) {
				ranked.add(h);
				find=true;
			}
		}
		ranked.sort(new Comparator<Hotel>() {
			public int compare(Hotel o1,Hotel o2) {
				Hotel.Rank rank1 = Hotel.Rank.CalculateScores(o1);
				Hotel.Rank rank2 = Hotel.Rank.CalculateScores(o2);
				
				if(rank1==null && rank2==null) return 0;
				if(rank1==null) return 1;
				if(rank2==null) return -1; 
				
				Hotel.Rank bestRank = rank1.CompareRanking(rank2);
				
				return rank1.nameHotel.equals(bestRank.nameHotel) ? -1 : 1 ;
			}
		});
		
		for(Hotel h : ranked) {
			text+=h.toString();
		}
		
		return find ? text : "città non trovata, ricontrollare i dati inseriti";
	}

	/**
	 * Inserisce la recensione passata come parametro, all'hotel specificato e nella collezione delle recensioni dell'utente.
	 * @param hotel Hotel scelto per la recensione.
	 * @param città Città dove si trova l'hotel. 
	 * @param reviewJson Recensione fatta dall'utente dell'hotel specificato.
	 * @param userName Username dell'utente che ha fatto la recensione.
	 * @return Restituisce una stringa contenente il messaggio.
	 * @throws Exception
	 */
	private String InsertReview(String hotel, String città, String reviewJson, String userName) throws Exception{
		Review rev = SerializeJson.Deserialization(reviewJson, Review.class);
		rev.SetUser(userName);
		int index = accounts.users.indexOf( new UserInfo(userName, "") );
		accounts.users.get(index).InsertReview(rev);
		return hotels.AddReview(hotel, città, rev);
	}
}
