import java.nio.channels.SocketChannel;

/**
 * Estende ServiceMethods. Legge le risposte del server riguardo i servizi richiesti in precedenza.
 * @author edoardo
 *
 */
public class ResponseServiceFromServer extends ServiceMethods{
	
	public ResponseServiceFromServer(Object obj, SocketChannel channel) {
		super(obj, channel);
	}

	protected boolean DoRegister() {
		try {
			String response = ReadMessage(channel);
			System.out.println(response+"\n");
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoLogin() {
		try {
			String response = ReadMessage(channel);
			String[] args = response.split(SEPARATOR);
			if(args.length!=2) {
				System.out.println(response);
				return false;
			}
			System.out.println(args[1]+"\n");
			if(!args[0].equals("y")) return false;
			User user = (User) obj;
			user.isLogged=true;
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoLogout() {
		try {
			String response = ReadMessage(channel);
			String[] args = response.split(SEPARATOR);
			if(args.length!=2) {
				System.out.println(response);
				return false;
			}
			System.out.println(args[1]+"\n");
			if(!args[0].equals("y")) return false;
			
			User user = (User) obj;
			user.isLogged=false;
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoSearchHotel() {
		String response = ReadMessage(channel);
		System.out.println(response+"\n");
		return true;
	}


	protected boolean DoSearchAllHotels() {
		String response = ReadMessage(channel);
		System.out.println(response+"\n");
		return true;
	}

	protected boolean DoInsertReview() {
		String response = ReadMessage(channel);
		System.out.println(response+"\n");
		return true;
	}

	protected boolean DoShowMyBadges() {
		String response = ReadMessage(channel);
		
		String[] args = response.split(SEPARATOR);
		if(args.length!=2) {
			System.out.println(response);
			return false;
		}
		
		System.out.println("hai effettuato: "+args[0]+" recensioni\nil tuo distintivo è: "+args[1]+"\n");
		return true;
	}
}
