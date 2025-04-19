import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Classe astratta che definisce metodi per gestire i servizi richiesti dall'utente.
 * @author edoardo
 *
 */
public abstract class ServiceMethods{
	
	/**
	 * Usato per suddividere gli argomenti testuali in una stringa.
	 */
	public final static String SEPARATOR = "%%";
	
	/**
	 * usato per codificare/decoficare le stringhe.
	 */
	public final static String ENCODE = "UTF-8";
	
	public SelectionKey key;
	public SocketChannel channel;
	
	protected Object obj; 
	
	public ServiceMethods(Object obj, SelectionKey key) {
		this.obj=obj;
		this.key= key;
	}
	
	public ServiceMethods(Object obj, SocketChannel channel) {
		this.obj=obj;
		this.channel = channel;
	}
	
	/**
	 * Si occupa della registrazione.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoRegister();
	/**
	 * Si occupa del login.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoLogin();
	/**
	 * si occupa del logout.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoLogout();
	/**
	 * si occupa di ricercare un hotel specifico.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoSearchHotel();
	/**
	 * si occupa di ricercare tutti gli hotel di una certa città.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoSearchAllHotels();
	/**
	 * si occupa di inserire una recensione fatta dall'utente.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoInsertReview();
	/**
	 * si occupa di calcolare il distintivo dell'utente registrato che lo ha richiesto.
	 * @return true se il metodo viene eseguito correttamente.
	 */
	protected abstract boolean DoShowMyBadges();
	
	/**
	 * Legge il servizio passato come parametro e chiama il metodo appropriato per gestirlo.
	 * @param service Service contenente il servizio richiesto dall'utente.
	 * @return Restituisce true se il metodo è stato eseguito correttamente.
	 * @throws Exception viene lanciato un errore di tipo generico Exception se il servizio passato come parametro non è ben formato.
	 */
	public boolean DoService(Service service) throws Exception{
		switch(service) {
			case register: return DoRegister();
			case login: return DoLogin();
			case logout: return DoLogout();
			case searchHotel: return DoSearchHotel();
			case searchAllHotels: return DoSearchAllHotels();
			case insertReview: return DoInsertReview();
			case showMyBadges: return DoShowMyBadges();
			default: throw new Exception(String.format("int outrange, service.code : %d not accepted", service.toInt()));
		}
	}
	
	/**
	 * Legge dal canale della connessione il messaggio e lo restituisce come stringa.
	 * @return Restituisce come stringa il messaggio del canale.
	 */
	public static String ReadMessage(SocketChannel channel){
		try {
			ByteBuffer buffer = ByteBuffer.allocate(256);
			buffer.clear();
			String bodyMessage="";
			channel.configureBlocking(true);
			@SuppressWarnings("unused")
			int a;
			while((a=channel.read(buffer))>0) {
				buffer.flip();
				while(buffer.hasRemaining()) {
					bodyMessage+=(char) (buffer.get() & 0xFF);
				}
				buffer.clear();
				channel.configureBlocking(false);
			}
			return bodyMessage;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected static void WriteMessage(SocketChannel channel, String message) throws Exception{
		ByteBuffer buffer = ByteBuffer.allocate(256);
		
		char[] messageArr = message.toCharArray(); 
		int indexChar=0;
		while(indexChar<messageArr.length) {
			buffer.clear();
			while(buffer.position()<buffer.capacity()) {
				buffer.put((byte)messageArr[indexChar]);
				indexChar++;
				if(indexChar>=messageArr.length) break;
			}
			buffer.flip();
			channel.write(buffer);
		}
	}
}
