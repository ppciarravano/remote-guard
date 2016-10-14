package ppc.remoteguard.util;

import java.util.Properties;

import ppc.remoteguard.log.Logger;

/**
 * Classe per la gestione delle costanti. Legge le costanti in un file di properties e le popola in modo finale, 
 * attraverso il codice presente in un blocco statico.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ConstantManager {

    private static Properties constantProps = null;

    //Costanti dell'applicazione caricate dal file /constant.properties
    public static final String SERVER_PASSWORD_GUARD;
    public static final String SERVER_PASSWORD_MD5_GUARD;
    public static final String SERVER_PASSWORD_CONTROLLER;
    public static final String SERVER_PASSWORD_MD5_CONTROLLER;
    public static final int SERVER_PORT;
    public static final String SERVER_HOST;
    public static final String VIDEO_MEDIA_LOCATOR;
    public static final String AUDIO_MEDIA_LOCATOR;
    public static final String VERSION;
    public static final int MAX_ATTEMPT_CLIENT_CONNECTION;
    public static final long DELAY_RETRY_CONNECTION;
    public static final long AUDIO_BUFFER_LENGTH;
    public static final long VIDEO_BUFFER_LENGTH;
    public static final long TIMEOUT_RTP_CONNECTION;
    public static final int JPEG_QUALITY;
    public static final int VIDEO_FORMAT;
    public static final int AUDIO_FORMAT;
    
	static {
		
		
		// Caricamento dei valori da file
		try {
			Logger.log.info("Loading Constants...");
			constantProps = PropFileLoader.loadProperties("/constant.properties");
			
		} catch (Exception ie) {
			Logger.log.error("Impossibile procedere al caricamento delle proprieta'", ie);
		}
		
		SERVER_PASSWORD_GUARD = constantProps.getProperty("SERVER_PASSWORD_GUARD");
		SERVER_PASSWORD_MD5_GUARD = constantProps.getProperty("SERVER_PASSWORD_MD5_GUARD");
		SERVER_PASSWORD_CONTROLLER = constantProps.getProperty("SERVER_PASSWORD_CONTROLLER");
		SERVER_PASSWORD_MD5_CONTROLLER = constantProps.getProperty("SERVER_PASSWORD_MD5_CONTROLLER");
		SERVER_PORT = Integer.parseInt(constantProps.getProperty("SERVER_PORT"));
		SERVER_HOST = constantProps.getProperty("SERVER_HOST");
		VIDEO_MEDIA_LOCATOR = constantProps.getProperty("VIDEO_MEDIA_LOCATOR");
		AUDIO_MEDIA_LOCATOR = constantProps.getProperty("AUDIO_MEDIA_LOCATOR");
		VERSION = constantProps.getProperty("VERSION");
		MAX_ATTEMPT_CLIENT_CONNECTION = Integer.parseInt(constantProps.getProperty("MAX_ATTEMPT_CLIENT_CONNECTION"));
		DELAY_RETRY_CONNECTION = Long.parseLong(constantProps.getProperty("DELAY_RETRY_CONNECTION"));
		AUDIO_BUFFER_LENGTH = Long.parseLong(constantProps.getProperty("AUDIO_BUFFER_LENGTH"));
		VIDEO_BUFFER_LENGTH = Long.parseLong(constantProps.getProperty("VIDEO_BUFFER_LENGTH"));
		TIMEOUT_RTP_CONNECTION = Long.parseLong(constantProps.getProperty("TIMEOUT_RTP_CONNECTION"));
		JPEG_QUALITY = Integer.parseInt(constantProps.getProperty("JPEG_QUALITY"));
		VIDEO_FORMAT = Integer.parseInt(constantProps.getProperty("VIDEO_FORMAT"));
		AUDIO_FORMAT = Integer.parseInt(constantProps.getProperty("AUDIO_FORMAT"));
		
		
	}

}
