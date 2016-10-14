package ppc.remoteguard.log;

/**
 * Classe per referenziare il log di Log4j.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class Logger {

	public static final org.apache.log4j.Logger log;
	
	static
    {
        //System.out.println("Init Logger");
        log = org.apache.log4j.Logger.getRootLogger();
        //log = org.apache.log4j.Logger.getLogger("application");
        
        
    }
}
