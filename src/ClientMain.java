import java.nio.channels.*;
import java.util.Scanner;
import java.io.IOException;
import java.net.*; 

/**
 * Classe main client side. 
 * @author edoardo
 *
 */
public class ClientMain {

	static String IP="0.0.0.0";
	static int PORT=2000;
	static int PORT_UDP=6000;
	static String IP_GROUP="230.0.0.0";

	/**
	 * Classe usata per unirsi al gruppo multicast per ricevere notifiche da parte del server sugli aggiornamenti del ranking degli hotels.
	 * @author edoardo
	 *
	 */
	static public class ThreadListenerUdp extends Thread{

		@SuppressWarnings("deprecation")
		public void run() {
			try {
				@SuppressWarnings("resource")
				MulticastSocket ms = new MulticastSocket(PORT_UDP);
				InetAddress group = InetAddress.getByName(IP_GROUP);
				ms.joinGroup(group);
				while(true) {


					DatagramPacket dp = new DatagramPacket (new byte[256],256);

					ms.receive(dp);

					System.out.println(new String(dp.getData(),ServiceMethods.ENCODE));
				}
			}
			catch (IOException e) {e.printStackTrace(); }
		}
	}

	public static void main(String[] args){
		try {
			if(args.length !=0 && args.length!=4)throw new Exception("non mettere parametri per valori preimpostati, altrimenti inserire 4 parametri:\nip del server\nporta del server\nporta udp del server\nip del gruppo del multicast\n");

			if(args.length!=0) {
				IP=args[0];
				PORT = Integer.parseInt(args[1]);
				PORT_UDP = Integer.parseInt(args[3]);
				IP_GROUP = args[2];
			}
		}catch(Exception e) { System.err.println(e.getMessage());}
		
		User user = new User();
		try(SocketChannel channel=SocketChannel.open(new InetSocketAddress(IP,PORT))) {
			if(channel.isConnected())System.out.printf("connesso a %s : %d\n", IP, PORT);
			else {
				channel.close();
				throw new Exception("connessione non andata a buon fine");
			}

			System.out.println(ResponseServiceFromServer.ReadMessage(channel));

			Thread listenerUdp = new ThreadListenerUdp();
			listenerUdp.start();
			while(true) {

				Service service = AskServiceToClient();
				try {
					RequestServiceToServer requestService = new RequestServiceToServer(user, channel);
					if(!requestService.DoService(service))continue;

					ResponseServiceFromServer responseService = new ResponseServiceFromServer(user, channel);
					responseService.DoService(service);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prende in input dall'utente il servizio che vuole richiedere.
	 * @return Restituisce un Service, un enum che rappresenta il servizio scelto dal cliente.
	 * @throws Exception
	 */
	private static Service AskServiceToClient() throws Exception{
		int service;
		do {
			System.out.println("quale funzionalità vuoi richiedere al server?\n\n1)registrati\n2)login\n3)logout\n4)cerca un hotel\n5)cerca gli hotel di una città\n6)inserisci una recensione\n7)visualizza il tuo distintivo\n(digita un numero intero)\n");
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			String text = scanner.nextLine();
			try {
				service = Integer.parseInt(text);
			}catch(NumberFormatException e) {System.out.println("input errato"); service=-1;}
		}while(service<1||service>Service.MAXNSERVICE);

		return Service.Compare(service);
	}
}
