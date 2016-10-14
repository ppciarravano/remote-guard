package ppc.remoteguard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;

import ppc.remoteguard.log.Logger;
import ppc.remoteguard.server.Server;
import ppc.remoteguard.util.Utility;

/**
 * Classe che implementa i metodi descritti nell'interfaccia UDPMultiplexer,
 * chiamati dal server.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class UDPMultiplexerServer extends Thread implements UDPMultiplexer
{

	private int serverPort;
	//private InetAddress serverIPAddress;
	private DatagramSocket udpServerSocket;
	private Hashtable<Integer, ClientAddr> clientMap;
	private UDPPacketQueue udpPacketQueue;
	private byte[] dataBuffer;
	private int idClientSequencer;
	
	Server instanceServer = null;
	
	public UDPMultiplexerServer(int serverPort, Server instanceServer) throws SocketException
	{
		this.serverPort = serverPort;
		this.instanceServer = instanceServer;
		//try
		//{
			//this.udpServerSocket = new DatagramSocket(this.serverPort, InetAddress.getLocalHost());
			this.udpServerSocket = new DatagramSocket(this.serverPort);
		//}
		//catch (UnknownHostException e)
		//{
			//e.printStackTrace();
		//}
		clientMap = new Hashtable<Integer, ClientAddr>();
		udpPacketQueue = new UDPPacketQueue();
		this.dataBuffer = new byte[2*1024 + 128]; // twice the MTU size, just to be safe.
		idClientSequencer = 0;
		this.start();
	}
		
	public boolean isClientAddrMapped(int idClient)
	{
		return clientMap.containsKey(new Integer(idClient));
	}	
		
	public int sendUDPPacket(UDPPacket udpPacket) throws IOException
	{
		int idClient = udpPacket.getIdClient();
		ClientAddr clientAddrFromMap = null;
		boolean containsKey = false;
		if (clientMap.containsKey(new Integer(idClient)))
		{
			clientAddrFromMap = clientMap.get(new Integer(idClient));
			containsKey = true;
		}					
    	
		if (!containsKey)
		{
			throw new RuntimeException("Client non presente in mappa dei cliente e sconosciuto!");
		}
		else if (idClient!=clientAddrFromMap.getIdClient())
    	{
    		throw new RuntimeException("Conflitto idClient: idClient diversi!");
    	}
		
		//Invio il pacchetto al client corrispondente
		return sendUDPPacket(udpPacket, clientAddrFromMap);
		
	}
	
	private int sendUDPPacket(UDPPacket udpPacket, ClientAddr clientAddrFromMap) throws IOException
	{
		//Invio il pacchetto al client clientAddrFromMap
		byte[] packetData = udpPacket.getUDPPacket();
		DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length,
				clientAddrFromMap.getClientIPAddress(), clientAddrFromMap.getClientPort());
		this.udpServerSocket.send(sendPacket);	
		Logger.log.debug("----->SEND:"+ Utility.datagramPacketToString(sendPacket));
		Logger.log.debug("----->Inviato: udpPacket" + udpPacket);
		return packetData.length;
		
	}
	
	public void run() {
		//Riceve i vari pacchetti e li mette nelle varie code in modo poi che qualche altro
		// thread successivamente si occupera' della lettura
		while(true) //TODO: finalizzazione
		{
			try
			{
				//Leggo un pacchetto in arrivo sulla porta UDP
				DatagramPacket receivePacket = new DatagramPacket(this.dataBuffer, this.dataBuffer.length);
	            this.udpServerSocket.receive(receivePacket);
	            Logger.log.debug("<-----RECV:"+ Utility.datagramPacketToString(receivePacket));
				
	            UDPPacket udpPacket = null;
	            
	            try 
	            {
	            	udpPacket = new UDPPacket(receivePacket.getData(), receivePacket.getLength());
	            }
	            catch (java.lang.Throwable e) {
	            	Logger.log.error("Arrivato un pacchetto che non e' stato possibile leggere!");
	            	continue;
	            }
	            	            
	            int idClient = udpPacket.getIdClient();
	            InetAddress clientIPAddress = receivePacket.getAddress();
	            int clientPort = receivePacket.getPort();
	            ClientAddr clientAddr = new ClientAddr(idClient, clientIPAddress, clientPort);
	            
	            
	            //Controllo se il pacchetto e' una richiesta di connection e handshaking di prima connessione
	            if ((udpPacket.getIdClient()==0)&&
	            		(udpPacket.getIdChannel()==0)&&
	            		(udpPacket.getIdMessage()==0)&&
	            		((new String(udpPacket.getData())).equals(CommandMessage.CONNECT))) {
	            	
	            	int newIdClient = getNewIdClient();
	            	Logger.log.info("CONNECT Handshaking new Client... newIdClient:"+newIdClient);
	            	//Restituisco un messaggio di WELCOME con id assegnato al nuovo client
	            	String hsString = CommandMessage.WELCOME+" "+newIdClient;
	            	UDPPacket hsPacket = new UDPPacket(0, 0, 0, hsString.getBytes());
	            	//Invio il pacchetto al client
	            	sendUDPPacket(hsPacket, clientAddr);
	            	
	            	
	            	//Chiamo il metodo di instanceServer (Server.class) che si occupera'
	            	//di gestire la comunicazione con il client
	            	this.instanceServer.newIncomingClient(newIdClient);
	            	
	            	
	            	//Salto la restante parte del codice del ciclo
	            	continue;
	            }
	            else if (udpPacket.getIdClient()==0)
	            {
	            	Logger.log.error("idClient==0 senza richiesta di Handshaking! Pacchetto scartato!");
	            	continue;
	            }
	            
	            
	            //Controllo se per questo client ho gia' inserito una entry in clientMap,
	            // altrimenti la inserisco 
	            if (clientMap.containsKey(new Integer(idClient)))
	            {
	            	//Controllo se la entry corrisponde con clientIPAddress e clientPort
	            	ClientAddr clientAddrFromMap = clientMap.get(new Integer(idClient));
	            	//System.out.println(clientAddrFromMap);
	            	//System.out.println(clientAddr);
	            	if (!clientAddrFromMap.equals(clientAddr))
	            	{
	            		Logger.log.error("Conflitto idClient: porta o address diversi! Pacchetto scartato!");
	            		continue;
	            	}
	            }
	            else
	            {
	            	//E' la prima volta che ricevo un pacchetto da questo idClient
	            	// creo una entry in clientMap
	            	clientMap.put(new Integer(idClient), clientAddr);
	            }
	            Logger.log.debug("<-----Ricevo: udpPacket" + udpPacket);
	            
	            //Aggiungo il pacchetto nel gestore delle code
	            udpPacketQueue.putUDPPacket(udpPacket);
	            
            }
            catch (IOException e)
            {
            	Logger.log.error("UDPMultiplexerServer.run:" + Utility.exceptionToString(e));
			}
            
		}
	}
	
	private int getNewIdClient()
	{
		//TODO: controllo che il nuovo id che vado a restituire non sia gia' nella UDPPacketQueue inserito da un client malevolo
		//	questo controllo tuttavia non e' necessario, ma renderebbe piu' robusta l'applicazione nel caso di un attacco da client malevolo.
		idClientSequencer++;
		return idClientSequencer;
	}
	
	public UDPPacket getUDPPacketFromQueue(int idClient, int idChannel)
	{
		return udpPacketQueue.getUDPPacket(idClient, idChannel);
	}
	
	public int getQueueSize(int idClient, int idChannel)
	{
		return udpPacketQueue.getQueueSize(idClient, idChannel);
	}
	
	
}
