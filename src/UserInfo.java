import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe per rappresentare gli account degli utenti. 
 * @author edoardo
 *
 */
public class UserInfo extends SerializeJson<UserInfo> implements Serializable{
	private static final long serialVersionUID = 3852857577226122383L;

	public String userName;
	private String password;
	
	public List<Review> recensioni=new LinkedList<Review>();
	
	public UserInfo(String userName, String password){
		this.userName = userName;
		this.password = password;
	}
	
	/**
	 * In base al numero di recensioni fatte dall'utente, crea un distintivo.
	 * @param nRecensioni Numero delle recensioni.
	 * @return Restituisce il distintivo dell'utente come stringa.
	 */
	public int GetRate() { return recensioni.size(); }
	
	public String GetDistintivo() {
		int rate = GetRate();
		if(rate>200) return "Contribuente super";
		if(rate>150) return "Contribuente esperto";
		if(rate>100) return "Contribuente";
		if(rate>50) return "Recensore esperto";
		return "Recensore";
	}
	
	/**
	 * Verifica che le credenziali dell'utente passato come parametro siano valide, ovvero siano già state registrate.
	 * @param o
	 * @return restituisce true se le credenziali esistono e sono corrette, altrimenti restituisce false.
	 */
	public boolean Authenticate(UserInfo o) {
		if(o==null) return false;
		if (o == this) {
            return true;
        }
        return userName.equals(o.userName) && password.equals(o.password);
	}
	
	/**
	 * Colleziona la recensione dell'utente fatta all'hotel.
	 * @param rev Recensione fatta dall'utente.
	 */
	public void InsertReview(Review rev) { recensioni.add(rev); }
	
	/**
	 * Copia l'username e la password dell'utente.
	 * @param obj
	 */
	public void Copy(UserInfo obj) {
		obj.userName=this.userName;
		obj.password=this.userName;
	}
	
	/**
	 * Confronta l'oggetto passato come parametro con questa istanza e verifica che siano uguali.
	 * @param o Istanza della classe Object
	 * @return restituisce true se l'oggetto passato come parametro è uguale all'oggetto istanziato da questa classe, altrimenti restituisce false. Due utenti sono uguali se hanno lo stesso username.
	 */
	public boolean equals(Object o) {
		if(o==null) return false;
		if (o == this) {
            return true;
        }
        if (!(o instanceof UserInfo)) {
            return false;
        }
        UserInfo c = (UserInfo) o;
        return userName.equals(c.userName);
    }
}
