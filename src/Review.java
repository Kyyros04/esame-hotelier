import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Classe che rappresenta le recensioni degli hotel fatte dagli utenti. 
 * @author edoardo
 *
 */
public class Review extends SerializeJson<Review> implements Serializable{
	private static final long serialVersionUID = -6054863478409908569L;
	
	private String userName;
	private String data;
	
	private int pulizia;
	private int qualitàPrezzo;
	private int posizione;
	private int servizio;
	
	public Review(String userName,int pulizia, int qualitàPrezzo, int posizione, int servizio) {
		this.userName = userName;
		this.pulizia = pulizia;
		this.qualitàPrezzo = qualitàPrezzo;
		this.posizione = posizione;
		this.servizio = servizio;
		data=new Date().toString();
	}
	
	/**
	 * 
	 * @return Restituisce l' username dell utente che ha fatto la recensione.
	 */
	public String GetUser(){ return this.userName;}
	
	/**
	 * Prende come parametro l'username dell'utente, stringa, e lo assegna alla recensione.
	 * @param userName Username dell'utente.
	 */
	public void SetUser(String userName) { this.userName=userName; }
	
	/**
	 *
	 * @return Restituisce la data al momento della chiamata del metodo, come Date.
	 * @throws ParseException 
	 */
	public Date GetDate() throws java.text.ParseException{
		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG, Locale.ITALY);
		return format.parse(data);
	}
	
	/**
	 * 
	 * @return Restituisce il punteggio globale, facendo la media tra pulizia, qualitàPrezzo, posizione e servizio.
	 */
	public int GetSintetico() {
		int sum = pulizia + qualitàPrezzo + posizione + servizio;
		return Math.round(sum/4);
	}
	
	/**
	 * 
	 * @return Restituisce l'attributo pulizia.
	 */
	public int GetPulizia() {return pulizia;}
	/**
	 * 
	 * @return Restituisce l'attributo posizione.
	 */
	public int GetPosizione() {return posizione;}
	/**
	 * 
	 * @return Restituisce l'attributo qualitàPrezzo.
	 */
	public int GetQprezzo() {return qualitàPrezzo;}
	/**
	 * 
	 * @return Restituisce l'attributo servizio.
	 */
	public int GetServizio() {return servizio;}
	
	/**
	 * Copia un'altra recensione in questa istanza, attributo per attributo.
	 * @param obj Recensione da copiare.
	 */
	public void Copy(Review obj) {
		userName=obj.userName;
		data=obj.data;
		pulizia=obj.pulizia;
		posizione=obj.posizione;
		qualitàPrezzo=obj.qualitàPrezzo;
		servizio=obj.servizio;
	}
	
	/**
	 * Confronta l'oggetto passato come parametro con questa istanza e verifica che siano uguali.
	 * @param o Istanza della classe Object
	 * @return restituisce true se l'oggetto passato come parametro è uguale all'oggetto istanziato da questa classe, altrimenti restituisce false. Due recensioni sono uguali se hanno lo stesso username.
	 */
	public boolean equals(Object o) {
		if(o==null) return false;
		if (o == this) {
            return true;
        }
        if (!(o instanceof Review)) {
            return false;
        }
        Review c = (Review) o;
        return userName.equals(c.GetUser());
    }
}
