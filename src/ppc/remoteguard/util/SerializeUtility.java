package ppc.remoteguard.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import ppc.remoteguard.log.Logger;

/**
 * Classe di utilita' per la serializzazione degli oggetti.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class SerializeUtility
{
    private static final int BUFFER_SIZE_FOR_ZIP = 1024;

    public static byte[] serializeObj(Object obj)
    {
        byte[] result = null;
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            result = baos.toByteArray();
            baos.close();

        }
        catch(Exception ex)
        {
            Logger.log.error("serializeObj Exception:\n" + Utility.exceptionToString(ex));
        }
        return result;
    }

    public static Object unserializeObj(byte[] source)
    {
        Object result = null;
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(source);
            ObjectInputStream ois = new ObjectInputStream(bais);
            result = ois.readObject();
            ois.close();
            bais.close();
        }
        catch(Exception ex)
        {
        	Logger.log.error("unserializeObj Exception:\n" + Utility.exceptionToString(ex));
        }
        return result;
    }
    
    public static byte[] zip(byte[] source)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater();
        deflater.setInput(source);
        deflater.finish();

        byte[] buffer = new byte[BUFFER_SIZE_FOR_ZIP];
        int writeByte = 0;
        //int tot = 0;
        while ((writeByte = deflater.deflate(buffer))!=0)
        {
            //tot += writeByte;
            //System.out.println(writeByte + " -> " + tot);
            baos.write(buffer, 0, writeByte);
        }
        deflater.end();
        return baos.toByteArray();
    }

    public static byte[] unzip(byte[] source)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();
        inflater.setInput(source);

        byte[] buffer = new byte[BUFFER_SIZE_FOR_ZIP];
        int writeByte = 0;
        //int tot = 0;
        try
        {
            while((writeByte = inflater.inflate(buffer)) != 0)
            {
                //tot += writeByte;
                //System.out.println(writeByte + " -> " + tot);
                baos.write(buffer, 0, writeByte);
            }
        }
        catch(DataFormatException ex)
        {
        	Logger.log.error("unzip Exception:\n" + Utility.exceptionToString(ex));
        }
        inflater.end();
        return baos.toByteArray();
    }

    
    public static byte[] serializeObjZip(Object obj)
    {
    	byte[] sourceSerialized = serializeObj(obj);
        byte[] sourceZipped = zip(sourceSerialized);
        return sourceZipped;
    }
    
    public static Object unserializeObjZip(byte[] source)
    {
    	byte[] sourceUnzipped = unzip(source);
        Object sourceObject = unserializeObj(sourceUnzipped);
        return sourceObject;
    }
    
}
