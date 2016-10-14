package ppc.remoteguard;

import java.io.IOException;

import ppc.remoteguard.log.Logger;
import ppc.remoteguard.util.Utility;

/**
 * Classe per la gestione del tunnel di tutti i pacchetti provenienti da due diversi client.
 * I pacchetti di un client estratti dalla coda dei canali RTP dall'1 al 4 vengono reindirizzati all'altro client e viceversa.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class UDPPacketTunnel extends Thread 
{
	private UDPMultiplexer udpClient1;
	private int idClient1;
	private UDPMultiplexer udpClient2;
	private int idClient2;
	
	private boolean active;	
	
	public UDPPacketTunnel(UDPMultiplexer udpClient1, int idClient1, UDPMultiplexer udpClient2, int idClient2)
	{
		this.udpClient1 = udpClient1;
		this.idClient1 = idClient1;
		this.udpClient2 = udpClient2;
		this.idClient2 = idClient2;
			
		active=true;
		start();
	}
				
	public void run()
	{
		Logger.log.info("UDPPacketTunnel START RUN!!");
		while (active)
		{
			try
			{
				//TODO:ulteriore controllo da implementare ma che non pregiudica il funzionamento dell'applicazione:
				//Se entro un certo tempo non manda in tunnel nessun packetto allora imposta active=false
				
				
				for (int idChannel = 1; idChannel <= 4; idChannel++)
				{
					if (udpClient1.getQueueSize(idClient1, idChannel)>0)
					{
						UDPPacket packet1 = udpClient1.getUDPPacketFromQueue(idClient1, idChannel);
						packet1.setIdClient(idClient2);
						udpClient2.sendUDPPacket(packet1);
					}
					
					if (udpClient2.getQueueSize(idClient2, idChannel)>0)
					{
						UDPPacket packet2 = udpClient2.getUDPPacketFromQueue(idClient2, idChannel);
						packet2.setIdClient(idClient1);
						udpClient1.sendUDPPacket(packet2);
					}
				}
			}
			catch (IOException ioe) {
				Logger.log.error(Utility.exceptionToString(ioe));
			}
			
		}
		Logger.log.info("UDPPacketTunnel END RUN!!");
	}
	
	public void setActive(boolean value)
	{
		this.active = value;
	}
	
	public boolean isActive()
	{
		return active;
	}
}
