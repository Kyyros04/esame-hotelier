import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Utilizzato come task dei thread per la gestione delle richieste di connessione e degli utenti. Il main thread deve aver definito un selector che inserisce in una coda condivisa le selection key. Questa classe prende dal costruttore il selector, la coda e risorse condivise. Il thread periodicamente consuma le selection key
 * @author edoardo
 *
 */
public class ManagerSelect implements Runnable{
	BlockingQueue<SelectionKey> blockingQueue;
	Selector selector;
	MonitorAccounts accounts; 
	MonitorHotels hotels;
	
	/**
	 * Costruttore della classe
	 * @param blockingQueue Coda condivisa, gestisce il race condition.
	 * @param selector Selector per consumare le selection key.
	 * @param accounts MonitorAccounts Risorsa condivisa dai thread per gestire gli utenti.
	 * @param hotels MonitorHotels Risorsa condivisa dai thread per gestire gli hotel.
	 */
	public ManagerSelect(BlockingQueue<SelectionKey> blockingQueue, Selector selector, MonitorAccounts accounts, MonitorHotels hotels) {
		this.blockingQueue = blockingQueue;
		this.selector = selector;
		this.accounts=accounts;
		this.hotels=hotels;
	}
	/**
	 * Override di run dell'interfaccia runnable. Istanzia ed esegue il thread e che a sua volta eseguirà il metodo, (visto come task).
	 */
	public void run(){
		while(true) {
			SelectionKey key=null;
			try {
				key=blockingQueue.take();
				System.out.printf("Thread %s ha preso una key\n",Thread.currentThread().getName());
			}catch(Exception e ) {e.printStackTrace();}
			if(!key.isValid()) {
				System.out.println("key is not valid");
				return;
			}
			if(key.isAcceptable()) {
				AcceptSelect(key);
			}
			else
			if(key.isReadable()) {
				ReadSelect(key);
			}

		}
	}
	
	/**
	 * Metodo che si occupa di gestire un nuovo socket connection quando il canale è pronto, ovvero il campo readyOps() restituisce true e OP_ACCEPT!=0.
	 * @param key Selection key pronto per l'accettazione.
	 */
	private void AcceptSelect(SelectionKey key) {
		try {
			ServerSocketChannel clientToAccept = (ServerSocketChannel) key.channel();
			SocketChannel client = clientToAccept.accept();
			if(client==null)return;
			client.configureBlocking(false);
			ByteBuffer bufferClient = ByteBuffer.allocate(256);
			client.register(selector, SelectionKey.OP_READ);
			System.out.println("accettata richiesta di connessione");
			bufferClient.clear();
			bufferClient = ByteBuffer.wrap("---\nBenvenuto su Hotelier\n\n---\n".getBytes());
			client.write(bufferClient);
		}catch(Exception e) {e.printStackTrace();}
	}
	
	/**
	 * Metodo che si occupa di gestire la lettura del canale quando è pronto, ovvero il campo readyOps() restituisce true e OP_READ!=0 
	 * @param key Selection key pronto per la lettura.
	 */
	private void ReadSelect(SelectionKey key) {
		try {
			//READ SERVICE
			Service service = ReadServiceFromClient(key);
			
			ServiceMethodsForServer methods = new ServiceMethodsForServer(null ,key, accounts, hotels);
			methods.DoService(service);
			
		}catch(Exception e) { key.cancel(); e.printStackTrace();}
	}
	
	/**
	 * Legge dal canale il servizio richiesto dall'utente e lo restituisce come Service.
	 * @param key Selection key da cui si prende il canale pronto per la lettura.
	 * @return Restituisce Service, enum del servizio richiesto dall'utente. 
	 * @throws Exception Viene lanciata una eccezione nel caso in cui la connessione sia chiusa o il servizio richiesto non sia ben formato.
	 * @throws IllegalArgumentException se la capacità del buffer è un intero negativo.
	 * @throws NotYetConnectedException se il channel non è ancora connesso.
	 * @throws IOException se occorre un errore di I/O.
 	 */
	private Service ReadServiceFromClient(SelectionKey key) throws Exception{
		System.out.println("key da leggere");
		ByteBuffer bufferClient = ByteBuffer.allocate(4);
		SocketChannel channel = (SocketChannel) key.channel();
		bufferClient.clear();
		int n = channel.read(bufferClient);
		if(n<=0) {
			String acc = accounts.CompareAddress(channel);
			if(acc != null) accounts.RemAccount(acc);
			System.out.println("connessione chiusa");
			throw new Exception("connessione chiusa");
		}
		bufferClient.flip();
		IntBuffer viewInt = bufferClient.asIntBuffer();
		int nService = viewInt.get(0);
		return Service.Compare(nService);
	}
}

