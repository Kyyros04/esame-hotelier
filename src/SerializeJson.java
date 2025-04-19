import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Classe astratta. Permette ad una classe che la estende di essere serializzata - deserializzata per il formato json.
 * @author edoardo
 *
 * @param <T> La classe che deve essere serializzata.
 */
public abstract class SerializeJson<T>{
	/**
	 * Serializza la classe estesa nel formato json.
	 * @return Restituisce la stringa in formato json.
	 */
	public String Serialization() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(this);
	}
	
	/**
	 * Serializza l'oggetto che estende SerializeJson<T>
	 * @param <T> il tipo della classe da serializzare.
	 * @param objToSerialize l'istanza della classe da serializzare.
	 * @return Restituisce la stringa in formato json.
	 */
	public static <T> String Serialization(SerializeJson<T> objToSerialize) {
		return objToSerialize.Serialization();
	}
	
	/**
	 * Deserializza l'oggetto che estende SerializeJson<T>
	 * @param <T> il tipo della classe da deserializzare.
	 * @param jsonString Stringa json da deserializzare
	 * @param cls tipo della classe che deve essere deserializzato.
	 * @return Restituisce L'oggetto deserializzato.
	 */
	public static <T> T Deserialization(String jsonString, Class<T> cls)  {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.fromJson(jsonString, cls);
	}
}
