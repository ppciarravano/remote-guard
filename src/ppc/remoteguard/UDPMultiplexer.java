package ppc.remoteguard;

import java.io.IOException;

/**
 * Interfaccia che descrive i metodi per inviare i pacchetti UDPPacket.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public interface UDPMultiplexer
{
	public int sendUDPPacket(UDPPacket udpPacket) throws IOException;
	
	public UDPPacket getUDPPacketFromQueue(int idClient, int idChannel);	
	
	public int getQueueSize(int idClient, int idChannel);
	
}
