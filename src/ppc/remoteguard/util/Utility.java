package ppc.remoteguard.util;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/**
 * Classe di utilities varie.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class Utility
{
	public static String stringToMD5(String password)
	{
		byte[] defaultBytes = password.getBytes();
		String result = "";
		try
		{
			
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();
		     
			/*
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++)
			{
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			*/
			result = Hexdump.getHexBytesString(messageDigest);
		}
		catch(NoSuchAlgorithmException nsae)
		{
			nsae.printStackTrace();
		}
		return result;

	}
	
	
	
	public static String datagramPacketToString(DatagramPacket p)
	{
		//String result = "" + p.getAddress() + ":" + 
		//p.getPort() + " - " + p.getOffset() + ","+
		//p.getLength()+" DATA:\n"+Hexdump.dumpBytes(p.getData(),p.getOffset(),p.getLength()); 
		
		String result = "" + p.getAddress() + ":" + 
		p.getPort() + " - " + p.getOffset() + ","+
		p.getLength()+" CHECKSUM:"+Utility.getChecksum(p.getData(),p.getOffset(),p.getLength()); 
		
		return result;
				
	}
	
	public static long getChecksum(byte[] buffer, int offset, int length)
	{
		long result = 0;
		try {

			ByteArrayInputStream bais = new ByteArrayInputStream(buffer,
					offset, length);
			CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
			byte readBuffer[] = new byte[length];
			cis.read(readBuffer, 0, length);
			result = cis.getChecksum().getValue();
			
			//byte readBuffer[] = new byte[5];
			//while (cis.read(readBuffer) >= 0) {
			//	long value = cis.getChecksum().getValue();
			//	System.out.println("The value of checksum is " + value);
			//}
		} catch (Exception e) {

			System.out.println("Exception has been caught" + e);
		}
		return result;
		
	}
	
	
	public static String exceptionToString(Exception ex)
	{
	    java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
	    java.io.PrintWriter pw = new java.io.PrintWriter(cw, true);
	    ex.printStackTrace(pw);
	    return "EXCEPTION --- MESSAGE: " + ex.getMessage() + "\n StackTrace:\n" + cw.toString();
	}
	
	/*
	public static void main(String[] args) {
		System.out.println(stringToMD5("controllerpw"));
	}
	*/


	
}

