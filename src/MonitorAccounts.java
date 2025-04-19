
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Estensione di Monitor. Utilizzato per la gestione degli utenti e condiviso tra i thread. Permette storage e load degli utenti in memoria permanente e aggiornarli periodicamente in base all'intervallo di tempo settato.
 * @author edoardo
 *
 */
public class MonitorAccounts extends Monitor{

	final static String filePath="UsersFile";
	LinkedList<UserInfo> users=new LinkedList<UserInfo>();

	/**
	 * Prende come parametro il nuovo account dell'utente e ne effettua la registrazione.
	 * @param user Il nuovo account da registrare.
	 * @return Restituisce true se la registrazione è andata a buon fine, altrimenti false.
	 */
	public synchronized boolean AddNewRegistration(UserInfo user) {
		if(users.contains(user)) {
			return false;
		}
		users.add(user);
		return true;
	}
	
	/**
	 * Verifica che l'account sia esistente.
	 * @param user Account dell'utente. 
	 * @return Restituisce true se l'account passato come parametro risulta valido, altrimenti false
	 */
	public boolean SearchAccount(UserInfo user) {
		for(UserInfo us : users) {
			if(us.equals(user)) {
				return us.Authenticate(user);
			}
		}
		return false;
	}
	
	/**
	 * Override della classe astratta Monitor. Carica dalla memoria permanente la lista degli account registrati. 
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

			Type typeList = new TypeToken<LinkedList<UserInfo>>() {}.getType();

			users = gson.fromJson(textForJson, typeList);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Override della classe astratta Monitor. Carica nella memoria permanente la lista degli account registrati.
	 */
	public void Store(){
		try {
			
			FileOutputStream fileOutput = new FileOutputStream(filePath);
			BufferedOutputStream fileBuffered = new BufferedOutputStream(fileOutput);

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			String jsonString = gson.toJson(users);

			fileBuffered.write(jsonString.getBytes(ServiceMethods.ENCODE));

			fileBuffered.close();
			fileOutput.close();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Classe degli account loggati.
	 * @author edoardo
	 *
	 */
	public class AccountLoggati{
		public SocketAddress address;
		public String userName;
		private AccountLoggati(String userName, SocketAddress address) {
			this.address = address;
			this.userName=userName;
		}
		/**
		 * Confronta l'oggetto passato come parametro con questa istanza e verifica che siano uguali.
		 * @param o Istanza della classe Objects.
		 * @return restituisce true se l'oggetto passato come parametro è uguale all'oggetto istanziato da questa classe, altrimenti restituisce false. Due Account loggati sono uguali se hanno lo stesso username.
		 */
		public boolean equals(Object o) {
			if(o==null) return false;
			if (o == this) {
				return true;
			}
			if (!(o instanceof AccountLoggati)) {
				return false;
			}
			AccountLoggati c = (AccountLoggati) o;
			return userName.equals(c.userName);
		}
	}

	private List<AccountLoggati> accounts;

	public MonitorAccounts(){
		accounts=new LinkedList<AccountLoggati>();
	}
	
	/**
	 * Restituisce il numero di account loggati.
	 * @return Restituisce il numero di account loggati, come numero intero.
	 */
	public int GetNAccounts() {return accounts.size();}
	
	/**
	 * Effettua il login dell' utente e lo aggiunge ad una lista di questa istanza.
	 * @param nameUser Nome utente che deve effettuare il login.
	 * @param address SocketAddress, contenente l'indirizzo remoto della connessione tcp con l'utente. 
	 * @return Restituisce true se l'account non era ancora loggato, altrimenti false perchè risulta già loggato.
	 */
	public synchronized boolean AddAccount(String nameUser, SocketAddress address) {
		AccountLoggati newAccount = new AccountLoggati(nameUser, address);
		if(accounts.contains(newAccount))
			return false;
		else {
			accounts.add(newAccount);
			return true;
		}
	}
	
	/**
	 * Cerca l'utente nella lista degli account loggati confrontando gli indirizzi remoti.
	 * @param channel SocketChannel usato per ricavare l'indirizzo remoto dell'utente.
	 * @return Restituisce il nome utente, come stringa, corrispondente all'indirizzo remoto ottenuto dal channel passato come parametro.
	 * @throws Exception
	 */
	public String CompareAddress(SocketChannel channel) throws Exception{
		SocketAddress address = channel.getRemoteAddress();
		for(AccountLoggati acc : accounts) {
			if(acc.address==address) return acc.userName;
		}
		return null;
	}
	 /**
	  * Rimuove l'utente dagli account loggati (usato per il logout). Metodo sincronizzato per affrontare problemi di race condition.
	  * @param nameUser Nome utente per cui viene effettuato il logout.
	  * @return Restituisce true se l'account è stato rimosso correttamente dalla lista degli account loggati, false se non era presente nella lista.
	  */
	public synchronized boolean RemAccount(String nameUser) {
		AccountLoggati account = new AccountLoggati(nameUser, null); 
		if(!accounts.contains(account)) return false;
		else {
			accounts.remove(account);
			return true;
		}
	}
}
