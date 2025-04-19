import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Estende ServiceMethods. Comunica al server i servizi richiesti dall'utente.
 * @author edoardo
 *
 */
public class RequestServiceToServer extends ServiceMethods{

	public RequestServiceToServer(Object obj,SocketChannel channel) {
		super(obj,channel);
	}
	
	protected boolean DoRegister(){
		try {
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.register.toInt());
			UserInfo newAccount = GetCredentialsFromUser();
			String newAccountSerialized = newAccount.Serialization();
			ByteBuffer accountBuffer = ByteBuffer.wrap(newAccountSerialized.getBytes(ENCODE));
			ByteBuffer buffer = ByteBuffer.allocate(accountBuffer.capacity()+nServiceBuffer.capacity());
			buffer.clear();
			buffer.put(nServiceBuffer);
			buffer.put(accountBuffer);
			buffer.flip();
			channel.write(buffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoLogin() {
		try {
			if( ((User) obj).isLogged ) {
				System.out.println("Sei già loggato, prima effettua un logout se vuoi loggare con un altro account\n");
				return false;
			}
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.login.toInt());
			UserInfo account = GetCredentialsFromUser();
			String accountSerialized = account.Serialization();
			ByteBuffer accountBuffer = ByteBuffer.wrap(accountSerialized.getBytes(ENCODE));
			ByteBuffer buffer = ByteBuffer.allocate(accountBuffer.capacity()+nServiceBuffer.capacity());
			buffer.clear();
			buffer.put(nServiceBuffer);
			buffer.put(accountBuffer);
			buffer.flip();
			channel.write(buffer);
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoLogout() {
		try {
			System.out.println(((User)obj).isLogged);
			if( ! ((User)obj).isLogged) {
				System.out.println("Non sei autenticato, quindi non puoi effettuare il logout\n");
				return false;
			}
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.logout.toInt());
			channel.write(nServiceBuffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoSearchHotel() {
		try {
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.searchHotel.toInt());
			
			String hotelString = GetStringFromUser("Inserisci il nome dell'hotel da ricercare", true);
			String cittàString = GetStringFromUser("Inserisci la città", false);
			
			ByteBuffer hotelBuffer = ByteBuffer.wrap(hotelString.getBytes(ENCODE));
			ByteBuffer cittàBuffer = ByteBuffer.wrap(cittàString.getBytes(ENCODE));
			
			ByteBuffer result = ByteBuffer.allocate(nServiceBuffer.capacity()+hotelBuffer.capacity()+cittàBuffer.capacity());
			
			result.put(nServiceBuffer);
			result.put(hotelBuffer);
			result.put(cittàBuffer);
			result.flip();
			channel.write(result);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoSearchAllHotels() {
		try {
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.searchAllHotels.toInt());
			
			String cittàString = GetStringFromUser("Inserisci la città", false);
			ByteBuffer cittàBuffer = ByteBuffer.wrap(cittàString.getBytes(ENCODE));
			
			ByteBuffer result = ByteBuffer.allocate(nServiceBuffer.capacity()+cittàBuffer.capacity());
			
			result.put(nServiceBuffer);
			result.put(cittàBuffer);
			result.flip();
			channel.write(result);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoInsertReview() {
		try {
			if( !((User) obj).isLogged ) {
				System.out.println("devi essere loggato per poter fare recensioni\n");
				return false;
			}
			
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.insertReview.toInt());
			String hotelString = GetStringFromUser("Inserisci il nome dell'hotel da recensire", true);
			String cittàString = GetStringFromUser("Inserisci la città", true);
			Review review = GetReviewFromUser();
			
			String reviewString = review.Serialization();
			
			ByteBuffer hotelBuffer = ByteBuffer.wrap(hotelString.getBytes(ENCODE));
			ByteBuffer cittàBuffer = ByteBuffer.wrap(cittàString.getBytes(ENCODE));
			ByteBuffer reviewBuffer = ByteBuffer.wrap(reviewString.getBytes(ENCODE));
			
			ByteBuffer result = ByteBuffer.allocate(nServiceBuffer.capacity()+hotelBuffer.capacity()+cittàBuffer.capacity()+reviewBuffer.capacity());
			
			result.put(nServiceBuffer);
			result.put(hotelBuffer);
			result.put(cittàBuffer);
			result.put(reviewBuffer);
			result.flip();
			channel.write(result);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean DoShowMyBadges() {
		try {
			if( !((User) obj).isLogged ) {
				System.out.println("devi essere loggato per poter visualizzare il distintivo\n");
				return false;
			}
			
			ByteBuffer nServiceBuffer = PrepareService(channel, Service.showMyBadges.toInt());
			channel.write(nServiceBuffer);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private ByteBuffer PrepareService(SocketChannel channel, int service) throws Exception{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(service);
		buffer.flip();
		return buffer;
	}
	
	private UserInfo GetCredentialsFromUser() throws Exception{
		UserInfo account=null;
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		String userName=null;
		String password=null;
		do{
			System.out.println("Inserire nome utente");
			userName = scanner.nextLine();
		}while(userName==null||userName.length()<=0||userName.length()>=20);
		
		do{
			System.out.println("Inserire password utente");
			password = scanner.nextLine();
		}while(password==null||password.length()<=0||password.length()>=20);
		
		account = new UserInfo(userName,password);
		return account;
	}
	
	private String GetStringFromUser(String description, boolean separator) throws Exception{
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		String text=null;
		do {
			System.out.println(description);
			text = scanner.nextLine();
		}while(text==null||text.length()<=0||text.length()>=20);
		return separator ? text+SEPARATOR : text;
	}
	
	private int GetSingleReview(String title) {
		String temp=null;
		int n=-1;
		do {
			System.out.printf("Inserisci una recensione da 1 a 5 per: %s\n", title);
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			temp = scanner.nextLine();
			try {
				n=Integer.parseInt(temp);
				if(n<1||n>5) throw new Exception("error format");
			}catch(Exception e) {n=-1;System.out.println("deve essere un numero da 1 a 5\n");}
		}while(n==-1);
		return n;
	}
	
	private Review GetReviewFromUser() {
		
		int PULIZIA=GetSingleReview("PULIZIA");
		int QPREZZO=GetSingleReview("QUALITA'/PREZZO");
		int POSIZIONE=GetSingleReview("POSIZIONE");
		int SERVIZIO=GetSingleReview("SERVIZIO");
		
		return new Review("",PULIZIA,QPREZZO,POSIZIONE,SERVIZIO);
	}
}
