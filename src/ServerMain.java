import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Classe main server side. Si mette in ascolto su una porta specificata dal parametro passato come input e accetta connessioni dai client. 
 * Inizializza:
 * - un selector per gestire le richieste client.
 * - una blocking queue condivisa per i thread.
 * - una classe condivisa MonitorAccounts per gestire, in modo sincronizzato, gli utenti tra i threads.
 * - una classe condivisa MonitorHotels per gestire, in modo sincronizzato, gli hotels tra i threads.
 * @author edoardo
 *
 */
public class ServerMain {
	static int PORT = 2000;
	static int PORT_UDP = 13;
	static String IP_GROUP= "230.0.0.0";
	static int PORT_GROUP = 6000;
	static int INTERVALLO_RANK_STORE=8;
	//args : numeri di porta, indirizzi, intervallo per aggiornamento dei ranking, ecc..
	public static void main(String[] args){
		try(ServerSocketChannel server = ServerSocketChannel.open()){
			if(args.length !=0 && args.length!=5)throw new Exception("non mettere parametri per valori preimpostati, altrimenti inserire 5 parametri:\nporta del server\nporta udp del server\nip del gruppo del multicast\nporta del gruppo del multicast\nintervallo per aggiornamento dei ranking e store in memoria permanente\n");

			if(args.length!=0) {
				PORT = Integer.parseInt(args[0]);
				PORT_UDP = Integer.parseInt(args[1]);
				IP_GROUP = args[2];
				PORT_GROUP = Integer.parseInt(args[3]);
				INTERVALLO_RANK_STORE = Integer.parseInt(args[4]);
			}
			DatagramSocket socket = new DatagramSocket(PORT_UDP);

			server.bind(new InetSocketAddress(PORT));
			server.configureBlocking(false);

			Selector selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.printf("In ascolto sulla porta: %d\n", PORT);
			BlockingQueue<SelectionKey> blockingQueue = new LinkedBlockingQueue<SelectionKey>();
			MonitorAccounts accounts = new MonitorAccounts();
			accounts.SetIntervalTime(INTERVALLO_RANK_STORE);
			accounts.Load();
			MonitorHotels hotels = new MonitorHotels(socket, IP_GROUP,PORT_GROUP);
			hotels.SetIntervalTime(INTERVALLO_RANK_STORE);
			hotels.Load();
			ExecutorService threadPool =  Executors.newFixedThreadPool(4);

			for(int i=0; i<4; i++) {
				threadPool.execute(new ManagerSelect(blockingQueue, selector, accounts, hotels));
			}

			long ultimoAggiornamento = System.currentTimeMillis();

			while(true) {
			    long tempoTrascorso = System.currentTimeMillis() - ultimoAggiornamento;
			    long tempoRimasto = (INTERVALLO_RANK_STORE * 1000) - tempoTrascorso;
			    
			    if (tempoRimasto <= 0) tempoRimasto = 1; 
			    int n = selector.select(tempoRimasto); 
			
			    // Gestione del Timer (Salvataggio e Ranking) se il tempo è scaduto
			    if (System.currentTimeMillis() - ultimoAggiornamento >= (INTERVALLO_RANK_STORE * 1000)) {
			        accounts.Store();
			        hotels.Store();
			        ultimoAggiornamento = System.currentTimeMillis();
			        System.out.println("Backup periodico e aggiornamento ranking completato.");
			    }
			
			    if (n == 0) continue; // Se si era svegliato solo per il timeout, torna a inizio ciclo
			
			    Set<SelectionKey> keys = selector.selectedKeys();
			    Iterator<SelectionKey> iter = keys.iterator();
			
			    while(iter.hasNext()) {
			        SelectionKey key = iter.next();
			        iter.remove();
			
			        if (key.isValid()) {
			            // Prima di passare la chiave al thread pool, disattiviamo temporaneamente il suo interesse
			            // in modo che i prossimi select() non la vedano finché il thread slave non ha finito di lavorarla.
			            int interestOps = key.interestOps();
			            key.interestOps(0); 
			            // Passiamo la chiave alla coda (il ManagerSelect dovrà poi fare key.interestOps(interestOps) alla fine)
			            blockingQueue.add(key);
			        }
			    }
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
