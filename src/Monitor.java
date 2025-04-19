/**
 * Classe astratta, utilizzata per condividere risorse tra thread e gestire situazioni di race condition. Inoltre utilizza metodi per:
 * - controllare l'intervallo di tempo che corre tra le chiamate dei metodi.
 * - Load and Storage in memoria permanente.
 * @author edoardo
 *
 */
public abstract class Monitor {
	
	public static int intervalTime;
	public static long lastTimeUpdate;
	
	/**
	 * Metodo usato per settare il tempo di intervallo e aggiornare lastTimeUpdate con l'attuale tempo.
	 * @param time Intero che rappresenta l'intervallo di tempo in secondi.
	 */
	public void SetIntervalTime(int time) {lastTimeUpdate=System.currentTimeMillis(); intervalTime=time;}
	public static boolean checkTimer() {
		long timeNow = System.currentTimeMillis();
		if((timeNow-lastTimeUpdate)/1000.0<intervalTime) return false;
		return true;
	}
	
	/**
	 * Store in memoria permanente.
	 */
	public abstract void Store();
	
	/**
	 * Load dalla memoria permanente.
	 */
	public abstract void Load();
}
