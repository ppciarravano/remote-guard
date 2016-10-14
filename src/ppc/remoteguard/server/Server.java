package ppc.remoteguard.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import ppc.remoteguard.UDPMultiplexerServer;
import ppc.remoteguard.UDPPacketTunnel;
import ppc.remoteguard.log.Logger;
import ppc.remoteguard.log.LoggerWindow;
import ppc.remoteguard.util.ConstantManager;
import ppc.remoteguard.util.Utility;

/**
 * Classe che gestisce tutta la logica della Server.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class Server //extends Thread //Thread per test
{
	private UDPMultiplexerServer server = null;
	private ServerTray serverTray = null;
	private Hashtable<Integer, ServerClient> guardsHash;
	private ServerClient controller = null;
	
	private ServerClient clientOfLastRequestView = null;
	private UDPPacketTunnel udpPacketTunnel = null;
	
	public Server()
	{
		try
		{
			serverTray = new ServerTray();
			LoggerWindow.init("RemoteGuard (Server) Log Output", serverTray.getImageIcon());
			//LoggerWindow.show();
			
			guardsHash = new Hashtable<Integer, ServerClient>();
			server = new UDPMultiplexerServer(ConstantManager.SERVER_PORT, this);
			serverTray.displayMessageINFO("Server attivo!");
			
			//For test
			//this.start();
		}
		catch (IOException ioe)
		{
			serverTray.displayMessageERROR("Errore: Impossibile attivare il server!!");
			Logger.log.error("Server new:" + Utility.exceptionToString(ioe));
		}
		
	}
	
	/*
	//Usato per test
	public void run()
	{
		while (true)
		{
			try
			{
				sleep(5000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}		
			
			Logger.log.info("SERVER HASH:"+this.guardsHash.size() + " Controller:" + this.controller);
			
		}
		
	}
	*/
	
	public boolean searchClientAndsendViewRequest(int idClient)
	{
		boolean result = false;
		clientOfLastRequestView = null;
		
		//Ciclo la Hashtable cercando il client corrispondente
		Enumeration<Integer> guardsClient = guardsHash.keys();
		while(guardsClient.hasMoreElements())
		{
			Integer idClientInHash = guardsClient.nextElement();
			ServerClient guardInHash = guardsHash.get(idClientInHash);
			if (idClientInHash.intValue() == idClient)
			{
				result = true;
				clientOfLastRequestView = guardInHash;
				//Invio la richiesta al client corrispondente
				guardInHash.sendViewRequest();
				//Interrompo il ciclo
				break;
			}
		}
		
		return result;
	}
	
	public boolean sendAckViewToController()
	{
		//Finalizzo un eventuale tunnel ancora in esecuzione
		udpPacketTunnelFinalize();
		
		if (controller!=null)
		{
			//Inizializzo il tunnel dei canali RDP 1-4 dalla guardia verso il client
			Logger.log.info("START TUNNEL RDP channel 1-4 from idClient:"+clientOfLastRequestView.getIdClient()+ " to controller!");
			if (clientOfLastRequestView==null)
			{
				Logger.log.error("Valore non atteso: idClientOfLastRequestView == null!!");
			}
			else
			{
				//Lancio il tunnel
				udpPacketTunnel = new UDPPacketTunnel(
						controller.getUdpMultiplexer(), 
						controller.getIdClient(),
						clientOfLastRequestView.getUdpMultiplexer(),
						clientOfLastRequestView.getIdClient());
				
			}
			
			//Lancio il viewer sul controller
			controller.sendStartViewer();
			return true;
		}
		return false;
	}
	
	public void stopViewerController()
	{
		if (controller!=null)
		{
			if (isUdpPacketTunnelActive())
			{
				controller.stopViewerController();
			}
		}
	}
	
	public void sendStopView()
	{
		if (clientOfLastRequestView!=null)
		{
			clientOfLastRequestView.sendStopView();
		}
		
		udpPacketTunnelFinalize();
	}
	
	public void udpPacketTunnelFinalize()
	{
		//finalizzo udpPacketTunnel
		if (udpPacketTunnel!=null)
		{
			udpPacketTunnel.setActive(false);
		}
		udpPacketTunnel = null;
		
	}
	
	public boolean isUdpPacketTunnelActive()
	{
		if (udpPacketTunnel==null)
		{
			return false;
		}
		return udpPacketTunnel.isActive();
		
	}
	
	public void newIncomingClient(int newIdClient)
	{
		ServerClient serverClient = new ServerClient(newIdClient, this, this.server);
		serverClient.start();
	}
	
	public Hashtable<Integer, ServerClient> getGuardsHash()
	{
		return guardsHash;
	}

	public void setGuardsHash(Hashtable<Integer, ServerClient> guardsMap)
	{
		this.guardsHash = guardsMap;
	}

	public ServerClient getController()
	{
		return controller;
	}

	public void setController(ServerClient controller)
	{
		this.controller = controller;
	}
	
		
	public static void main(String[] args)
	{
		new Server();
	}

}
