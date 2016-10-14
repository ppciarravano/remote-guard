package ppc.remoteguard.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe per caricare un file di properties dal classpath.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public abstract class PropFileLoader
{
    /**
     * Loads the property file.
     *
     * @param propFileName - the name of the property file
     * @return The properties loaded from the file.
     * @throws java.io.IOException - when the file is not found, etc.
     */
    public static Properties loadProperties(String propFileName) throws IOException
    {
        // find the jdo.properties in the class path
        InputStream ioStream = PropFileLoader.class.getResourceAsStream(propFileName);
        if(ioStream == null)
        {
            throw new IOException("File not found: " + propFileName);
        }

        // load the properties
        Properties retv = new Properties();
        retv.load(ioStream);

        // close the stream
        ioStream.close();

        // return the properties
        return retv;
    }

}
