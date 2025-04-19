import java.io.Serializable;

/**
 * Classe per rappresentare un hotel. 
 * @author edoardo
 *
 */
public class Hotel extends SerializeJson<Hotel> implements Serializable{
	private static final long serialVersionUID = -6474246856690460149L;
	
	public int id;
	public String name="";
	public String description;
	public String city="";
	public String phone="";
	public String[] services;
	
	public int rate=0;
	
	private class Ratings{
		int cleaning = 0;
		int position = 0;
		int services = 0;
		int quality = 0;
		public int GetGlobalRating() {
			int sum = cleaning + position + services + quality;
			return Math.round(sum/4);
		}
	}
	
	Ratings ratings = new Ratings();
	
	public Hotel(String nameHotel, String  city) {
		this.name=nameHotel;
		this.city=city;
	}
	
	public Hotel(int id) {
		this.id=id;
	}
	
	/**
	 * Inserisce una recensione all'hotel.
	 * @param rev Recensione fatta dall'utente.
	 */
	public void InsertReview(Review rev) {
		this.rate++;
		ratings.cleaning = (ratings.cleaning + rev.GetPulizia()) / rate;
		ratings.position = (ratings.position + rev.GetPosizione()) / rate;
		ratings.quality = (ratings.quality + rev.GetQprezzo()) / rate;
		ratings.services = (ratings.services + rev.GetServizio()) / rate;
	}
	
	/**
	 * Classe interna ad Hotel usata per il calcolo del ranking locale.
	 * @author edoardo
	 */
	public static class Rank{
		String nameHotel;
		
		float Scores;
		
		/**
		 * Algoritmo per calcolare il ranking locale di un hotel. il risultato è un intero, calcolato in base alle quantità delle recensioni fatte e la qualità.  
		 * @param h Hotel su cui viene calcolato il ranking locale.
		 * @return restituisce una istanza del Rank dell'hotel passato come parametro.
		 */
		public static Rank CalculateScores(Hotel h) {
			
			if(h.rate<1) return null;
			
			Rank rank = new Rank();
			rank.nameHotel = h.name;
			
			rank.Scores = h.ratings.GetGlobalRating()*2 + GetValueScaglione(h.rate) + (float) h.rate/(h.rate+1);
			
			return rank; 
		}
		
		/**
		 * Prende il numero recensioni e in base a quale scaglione appartiene, viene restituito un punteggio.
 		 * @param n Numero recensioni
		 * @return Restituisce un punteggio, un intero. 
		 */
		public static int GetValueScaglione(int n) 
		{
			if(n<100) return 1;
			if(n<500) return 2;
			if(n<1500) return 3;
			return 4;
		}
		
		public Rank CompareRanking(Rank other) {
			if(other==null) return this;
			return this.Scores>other.Scores ? this : other; 
		}
	}
	
	/**
	 * Crea una stringa per rappresentare i dettagli dell'hotel.
	 * @return restituisce una stringa contenente i dettagli dell'hotel.
	 */
	public String toString() {
		String servicesString ="\n";
		for(String serv : services) {
			servicesString+=serv+"\n";
		}
		return String.format("Nome hotel: %s\nDescrizione: %s\ncittà: %s\nTelefono: %s\nServizi: %s\nNumero recensioni: %d\n\nPunteggi hotel:\n\npulizia: %d\nqualità/prezzo: %d\nposizione: %d\nservizio: %d\nglobale: %d\n\n",name, description, city, phone, servicesString, rate, ratings.cleaning, ratings.quality, ratings.position, ratings.services, ratings.GetGlobalRating());
	}
	
	/**
	 * Confronta l'oggetto passato come parametro con questa istanza e verifica che siano uguali.
	 * @param o Istanza della classe Object
	 * @return restituisce true se l'oggetto passato come parametro è uguale all'oggetto istanziato da questa classe, altrimenti restituisce false. Due hotel sono uguali se hanno lo stesso nome e stessa città oppure lo stesso id.
	 */
	public boolean equals(Object o) {
		if(o==null) return false;
		if (o == this) {
            return true;
        }
        if (!(o instanceof Hotel)) {
            return false;
        }
        Hotel c = (Hotel) o;
        return id==c.id || (name.equals(c.name) && city.equals(c.city));
    }
}
