package ppc.remoteguard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import ppc.remoteguard.log.Logger;
import ppc.remoteguard.util.ConstantManager;
import ppc.remoteguard.util.Utility;

/**
 * Classe che implementa i metodi descritti nell'interfaccia UDPMultiplexer,
 * chiamati dal client (Guard o Controller).
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class UDPMultiplexerClient extends Thread implements UDPMultiplexer
{

	private int serverPort;
	private InetAddress serverIPAddress;
	private DatagramSocket udpClientSocket;
	private int idClient;
	private ClientQueues clientQueue;
	private byte[] dataBuffer;
	private boolean isConnected;
	private boolean inConnectionAttempts;
	private Semaphore connSemaphore;
	private int totalAttemptClientConnection;
	
	public UDPMultiplexerClient(int serverPort, InetAddress serverIPAddress) throws SocketException
	{
		this.serverPort = serverPort;
		this.serverIPAddress = serverIPAddress;
		this.idClient = 0;
		this.udpClientSocket = new DatagramSocket();
		//clientQueue = new ClientQueues(this.idClient); //Posso chiamare questa inizializzazione solo dopo aver popolato l'idClient successivamente all'handshaking con il server
		this.dataBuffer = new byte[2*1024 + 128]; // twice the MTU size, just to be safe.
		this.isConnected = false;
		connSemaphore = new Semaphore(0);
		totalAttemptClientConnection = 0;
		inConnectionAttempts=false;
		
	}
	
	
	public boolean isConnect()
	{
		return this.isConnected;
	}
	
	public int getIdClient()
	{
		return this.idClient;
	}
	
	public boolean connect() throws IOException
	{
		this.start();
		totalAttemptClientConnection = 0;
		inConnectionAttempts = true;
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
		final Runnable sendConnectionRequest = new Runnable() {
            			public void run() { 
            				
            				totalAttemptClientConnection++;
            				if (totalAttemptClientConnection>ConstantManager.MAX_ATTEMPT_CLIENT_CONNECTION)
            				{
            					Logger.log.debug("MAX_ATTEMPT_CLIENT_CONNECTION... STOP CONNECTION ATTEMPTS!!");
            					if (!isConnected)
            					{
            						//Sbocco il semaphore su il connect
            		            	connSemaphore.release();
            					}
            					inConnectionAttempts = false;
            					
            					//sendConnectionHandle.cancel(true); //Dovrei dichiarare sendConnectionHandle a livello di attributo di classe e richiamarlo qui
            					scheduler.shutdownNow(); //Preferisco usare lo shutdownNow per non modificare la visibilita' di sendConnectionHandle
            				}
            				else
            				{
            					Logger.log.debug("SEND CONNECTION Handshaking request... attempt:"+totalAttemptClientConnection);
            					if (!isConnected)
            					{
	            					String connectString = CommandMessage.CONNECT;
	            					UDPPacket connPacket = new UDPPacket(0, 0, 0, connectString.getBytes());
	            					
	            					try {
										sendUDPPacket(connPacket);
									} catch (IOException ioe) {
										Logger.log.error(Utility.exceptionToString(ioe));
									}
									catch (RuntimeException re) {
										Logger.log.error(Utility.exceptionToString(re));
									}
            					}
            				}
            			}
            		};
        //final ScheduledFuture<?> sendConnectionHandle = 
            	scheduler.scheduleAtFixedRate(sendConnectionRequest, 0, ConstantManager.DELAY_RETRY_CONNECTION, TimeUnit.MILLISECONDS);

				
		Logger.log.info("WAITING FOR SERVER Handshaking...");
		//Attendo finche' non ho ricevuto un pacchetto WELCOME di handshaking dal server
		//Iplementato con wait o con preferibilmente un semaphore
		//while(!this.isConnected);
		try {
			connSemaphore.acquire();
		} catch (InterruptedException ie) {
			Logger.log.error(Utility.exceptionToString(ie));
		}
		
		scheduler.shutdownNow();
		
		if(this.isConnected)
		{
			Logger.log.info("AVVENUTO handshaking con idClient:" + getIdClient());
		}
		else
		{
			Logger.log.info("TIMEOUT handshaking con il Server non avvenuto!!");
		}
		
		return this.isConnected;
	}
	
	private int parseConnPacket(UDPPacket connFromServerPacket)
	{
		String hsString = new String(connFromServerPacket.getData());
    	Logger.log.debug("CONNECT Handshaking message from Server: "+hsString);
    	//Parso la stringa per estrarre idClient assegnato dal server
    	String idClientString = hsString.substring(CommandMessage.WELCOME.length()+1);
    	Logger.log.debug("parseConnPacket idClientString:"+idClientString);
    	int newIdClientFromServer = Integer.parseInt(idClientString);
    	    	
    	return newIdClientFromServer;
	}
	
	
	public int sendUDPPacket(UDPPacket udpPacket) throws IOException
	{
		int idClient = udpPacket.getIdClient();
		if (idClient != this.idClient)
    	{
    		throw new RuntimeException("send UDPPacket: Conflitto idClient: idClient diversi!");
    	}			
		
		//Invio il pacchetto al server
		byte[] packetData = udpPacket.getUDPPacket();
		DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length,
				this.serverIPAddress, this.serverPort);
	    this.udpClientSocket.send(sendPacket);
	    Logger.log.debug("----->SEND:"+ Utility.datagramPacketToString(sendPacket));
	    Logger.log.debug("----->Inviato: udpPacket" + udpPacket);
	    return packetData.length;
		
	}
	
	
	public void run() {
		//Riceve i vari pacchetti e li mette nella coda in modo poi che qualche altro
		// thread successivamente si occupera' della lettura
		while(true) //TODO: finalizzazione
		{
			try
			{
				//Leggo un pacchetto in arrivo sulla porta UDP
				DatagramPacket receivePacket = new DatagramPacket(this.dataBuffer, this.dataBuffer.length);
				this.udpClientSocket.receive(receivePacket);
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
	            
	            //Controllo se il pacchetto e' una response di handshaking di prima connessione
	            if ((this.inConnectionAttempts)&&
	            		(!this.isConnected)&&
	            		(udpPacket.getIdClient()==0)&&
	            		(udpPacket.getIdChannel()==0)&&
	            		(udpPacket.getIdMessage()==0)&&
	            		((new String(udpPacket.getData())).startsWith(CommandMessage.WELCOME))) {
	            	
	            	this.idClient = parseConnPacket(udpPacket);
	            	this.isConnected = true;
	            	clientQueue = new ClientQueues(this.idClient);
	            	//Sbocco il semaphore su il connect
	            	connSemaphore.release();
	            	//Salto la restante parte del codice del ciclo	            	
	            	continue;
	            }
	            else if ((udpPacket.getIdClient()!=0)&&(this.isConnected))
	            {
	            	//Aggiungo il pacchetto alla coda
		            int idClient = udpPacket.getIdClient();
		            if (idClient != this.idClient)
		        	{
		            	Logger.log.error("receive UDPPacket: Conflitto idClient: idClient diversi!");
		            	continue;
		        	}	
		            Logger.log.debug("<-----Ricevo: udpPacket" + udpPacket);
		            //Aggiungo il pacchetto nel gestore delle code
		            clientQueue.putInQueue(udpPacket);
	            
	            }
	            else
	            {
	            	Logger.log.debug("IGNORO udpPacket:" + udpPacket + " isConnected:"+this.isConnected);
	            }
	        }
            catch (IOException e)
            {
            	Logger.log.error("UDPMultiplexerClient.run:" + Utility.exceptionToString(e));
			}
            
		}
	}
	
	public UDPPacket getUDPPacketFromQueue(int idClient, int idChannel)
	{
		if (this.idClient!=idClient)
    	{
    		throw new RuntimeException("Conflitto idClient: idClient diversi!");
    	}	
		return clientQueue.getFromQueue(idChannel);
	}
	
	public int getQueueSize(int idClient, int idChannel)
	{
		if (this.idClient!=idClient)
    	{
    		throw new RuntimeException("Conflitto idClient: idClient diversi!");
    	}	
		return clientQueue.getQueueSize(idChannel);
	}
	
	
}


