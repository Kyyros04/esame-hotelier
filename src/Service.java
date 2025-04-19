/**
 * Enum che indica il servizio richiesto dall'utente.
 * @author edoardo
 *
 */
public enum Service {
	 register(1), login(2), logout(3), searchHotel(4), searchAllHotels(5), 
	 insertReview(6), showMyBadges(7);
    final int code;
    private Service(int code) {
        this.code = code;
    }
    /**
     * Intero che fa da indice ai servizi.
     * @return Restituisce un intero che fa da indice ai servizi.
     */
    public int toInt() {
    	return code;
    }
    
    /**
     * Rappresentazione byte dell'indice del servizio.
     * @return Restituisce la rappresentazione in byte dell'indice del servizio.
     */
    public byte getByte() {
    	return (byte) code;
    }
    
    /**
     * Prende un intero e se è valido, restituisce il servizio corrispondente.
     * @param code Indice del servizio visto come intero.
     * @return Restituisce il Service corrispondente all'indice passato come parametro.
     * @throws Exception Viene lanciata una eccezione se l'intero è <1 e >7.
     */
    public static Service Compare(int code) throws Exception{
        switch(code){ 
        case 1 : return Service.register;
        case 2 : return Service.login; 
        case 3 : return Service.logout;
        case 4 : return Service.searchHotel;
        case 5 : return Service.searchAllHotels;
        case 6 : return Service.insertReview;
        case 7 : return Service.showMyBadges;
        default: throw new Exception(String.format("int outrange, code: %d not accepted",code));
        }
    }
    public final static int MAXNSERVICE = 7;
}
