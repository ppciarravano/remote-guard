package ppc.remoteguard.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe di utilita' per output in formato esadecimale.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class Hexdump {
    private static final int BYTES_PER_LINE = 16;
    private static final int PADDING = 4;

    /**
     *   returns a char corresponding to the hex digit of the
     *   supplied integer.
     */
    private static char charFromHexDigit(int digit){
	if((digit >= 0) && (digit <= 9))
	    return (char)(digit + '0');
	else
	    return (char)(digit - 10 + 'a');
    }
    
    public static String dumpBytes(byte[] datas){
    	return dumpBytes(datas, 0, datas.length);
    }
    
    public static String dumpBytes(byte[] datas, int offset,int length){
    	ByteArrayInputStream bais = new ByteArrayInputStream(datas,offset,length);
    	//System.out.println("LENGTH: " + length + " ----------------------------------");
    	return dumpFile(bais);
    	//System.out.println("-------------------------------------------------");
    }
    
    /**
     *  hexdumps the contents of a given open input file to stdout
     */    
    public static String dumpFile(InputStream in){
	
    	StringBuffer result = new StringBuffer();
    	try {
		
		
	    byte[]       inBuf = new byte[BYTES_PER_LINE]; 
	    int          count;
	    int          runningCount=0;
	    StringBuffer line = new StringBuffer();
	    
	    while((count = in.read(inBuf)) >0){
		// print running offset
		for(int d=0; d < 8; d++){
		    int digitValue = (int)(runningCount >> (4*(7-d)))%16;
		    line.append (charFromHexDigit((digitValue>=0)?digitValue
						  :(16+digitValue)));
		}
		runningCount += count;
		line.append("  ");

		// print hexbytes
		for(int x=0; x<count; x++){

		    int i = (inBuf[x]>=0)?inBuf[x]:(256+inBuf[x]);

		    line.append(charFromHexDigit(i/16));
		    line.append(charFromHexDigit(i%16));
		    line.append(' ');
		}

		// print padding between hexbytes and ascii
		for(int x=0; x < PADDING + ((BYTES_PER_LINE-count)*3); x++){
		    line.append(' ');
		}
		      		
		// print ascii
		for(int x=0; x<count; x++){
		    char v = (char)inBuf[x];
		    if((v >= ' ') && (v < (char)0x7f)){
			line.append(v);
		    }
		    else {
			line.append('.');
		    }
		}
		
		line.append('\n');
	    } // while

	    result.append(line);
	}
	catch (IOException e){
	    System.out.println("error reading file");
	}
		return result.toString();
    }
    
    public static String getHexBytesString(byte[] inBuf)
    {
    	StringBuffer result = new StringBuffer();
		for(int x=0; x<inBuf.length; x++)
		{

		    int i = (inBuf[x]>=0)?inBuf[x]:(256+inBuf[x]);

		    result.append(charFromHexDigit(i/16));
		    result.append(charFromHexDigit(i%16));
		    
		}
		return result.toString();
    }
    
    
}

