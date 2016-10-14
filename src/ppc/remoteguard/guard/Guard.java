package ppc.remoteguard.guard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

import net.rudp.ReliableSocket;
import ppc.remoteguard.CommandMessage;
import ppc.remoteguard.UDPMultiplexerClient;
import ppc.remoteguard.CommandMessage.ClientType;
import ppc.remoteguard.CommandMessage.Commands;
import ppc.remoteguard.CommandMessage.ParamsName;
import ppc.remoteguard.log.Logger;
import ppc.remoteguard.log.LoggerWindow;
import ppc.remoteguard.rudp.DatagramWrapper;
import ppc.remoteguard.rudp.ReliableMessage;
import ppc.remoteguard.rudp.StateListener;
import ppc.remoteguard.util.Utility;

/**
 * Classe che gestisce tutta la logica della Guard.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class Guard extends Thread
{

	private UDPMultiplexerClient client = null;
	private int idClient;
	private ObjectOutputStream messageOut = null;
	private ObjectInputStream messageIn = null;
	private boolean isInView = false;
	
	private DatagramWrapper dw = null;
	private ReliableMessage channel = null;
	private RTPVideoAudioTransmitter rtpVideoAudioTransmitter = null;
	private GuardTray guardTray = null;
	
	private boolean active;
		
	public Guard() throws IOException
	{
		guardTray = new GuardTray(this);
		LoggerWindow.init("RemoteGuard (Guard) Log Output", guardTray.getImageIcon());
		//LoggerWindow.show();
		active = false;
		this.start();
	}
	
	public void run()
	{
		//Leggo il messaggio dal server
		CommandMessage messageRequest = null;
		while (true)
		{
			if (active)
			{
				try {
					messageRequest = CommandMessage.readCommandMessage(messageIn);
					Logger.log.info("Commands messageRequest from Server:"+messageRequest);
					
					//Identificazione messaggio e esecuzione azione comando richiesto dal server
					switch (messageRequest.getCommand()) {
					
						case KILL:
							//Il server richiede al client di interrompere la connessione
							//TODO
							break;	
							
						case VIEW:
							//Il server richiede al client di avviare l'invio RTP
							if (!isInView)
							{
								Logger.log.info("Send RTP video/audio....");
								viewRequest();
							}
							else
							{
								Logger.log.info("Guard already send RTP!");
							}
							break;
							
						case STOP_VIEW:
							//Il server richiede al client di interrompere l'invio RTP
							if (isInView)
							{
								stopViewRequest();
							}
							else
							{
								Logger.log.info("Guard don't send RTP!");
							}
							break;
										
						default:
							//Messaggio ignorato
							
							break;
							
					}
					
					
				} catch (IOException ioe) {
					//Logger.log.error(Utility.exceptionToString(ioe));
				} catch (ClassNotFoundException cnfe) {
					//Logger.log.error(Utility.exceptionToString(cnfe));
				}
			}
			else
			{
				try
				{
					sleep(300);
				}
				catch (InterruptedException ie)
				{
					Logger.log.error(Utility.exceptionToString(ie));
				}
			}
			
		}
		
	}
	
	private void viewRequest() throws IOException
	{
		this.isInView = true;
		//Invio ACKVIEW al server
		CommandMessage messageResponse = new CommandMessage(Commands.ACKVIEW);
		messageResponse.writeCommandMessage(messageOut);
		
		this.rtpVideoAudioTransmitter = new RTPVideoAudioTransmitter(client, idClient);
		// Start RTP the transmission
		boolean result = rtpVideoAudioTransmitter.start();
		Logger.log.info("RTP video/audio start: result:" + result);
		
	}
	
	private void stopViewRequest() throws IOException
	{
		this.isInView = false;
		//Invio ACK al server
		CommandMessage messageResponse = new CommandMessage(Commands.ACK);
		messageResponse.writeCommandMessage(messageOut);
		
		this.rtpVideoAudioTransmitter.stop();
		
	}
	
		
	public void connect(String hostName, int udpPortNumber, String password, String guardName)
	{
		Logger.log.info("hostName:"+hostName+" udpPortNumber:"+udpPortNumber+" password:"+password+" guardName:"+guardName);
		
		try
		{			
			//Inizializzo la connessione
			InetAddress IPAddress = InetAddress.getByName(hostName);
			client = new UDPMultiplexerClient(udpPortNumber, IPAddress);
			
			if (client.connect())
			{
				this.idClient = client.getIdClient();
				
				//TODO:Risoluzione parziale del conflitto di sincronizzazione sull'apertura del canale RUDP
				try{
					sleep(3000);
				}catch (InterruptedException e){ }
				
				//Inizializzo la connessione affidabile rudp
				this.dw = new DatagramWrapper(client, this.idClient);
				this.channel = new ReliableMessage(dw);
				channel.addStateListener(new StateListener(){
					//Azione da compiere in caso di chiusura connessione
					public void connectionClosed(ReliableSocket sock) {
						Logger.log.info("Connessione chiusa... Finalizzo la guard!");
						guardTray.displayMessageERROR("Disconnesso dal server!!");
						guardTray.enableConnection();
						
					}
					/*
					public void connectionFailure(ReliableSocket sock) {
						
					}
					*/
				});				
				//this.channel.addStateListener(new StateListener()); 		
				//this.channel.addListener(new PacketListener()); 		
				this.channel.connect();
				
				//Inizializzo ObjectOutputStream
				messageOut = new ObjectOutputStream(this.channel.getOutputStream());
				//Preparo il messaggio
				CommandMessage messageRequest = new CommandMessage(Commands.IAM);
				messageRequest.putParam(ParamsName.IDCLIENT, new Integer(this.idClient));
				messageRequest.putParam(ParamsName.GUARDNAME, guardName);
				messageRequest.putParam(ParamsName.PASSWORD, password);
				messageRequest.putParam(ParamsName.CLIENT_TYPE, ClientType.GUARD);
				Logger.log.info("Commands messageRequest:"+messageRequest);
				//Invio il messaggio
				messageRequest.writeCommandMessage(messageOut);
				//Inizializzo ObjectInputStream (lo devo fare non prima di aver almeno inviato un messaggio in outputStream sul canale rudp)
				messageIn = new ObjectInputStream(this.channel.getInputStream());
				//Leggo il messaggio dal server
				CommandMessage messageResponse = CommandMessage.readCommandMessage(messageIn);
				Logger.log.info("Commands messageResponse:"+messageResponse);
				
				if (messageResponse.getCommand().equals(Commands.HELLO))
				{
					Logger.log.info("HELLO FROM SERVER!!");
					//Lancio il thread di ricezione messaggi della guard
					Logger.log.info("Guard Thread Run is active...");
					guardTray.displayMessageINFO("Guard attiva connessa!");
					active = true;
					//this.start();
					
				}
				else if (messageResponse.getCommand().equals(Commands.WRONGPW))
				{
					Logger.log.info("WRONG PASSWORD FROM SERVER!!!!!");
					//Chiudo il canale rudp
					messageOut.close();
					messageIn.close();
					this.channel.close();
					guardTray.displayMessageERROR("Password errata!!");
					guardTray.enableConnection();
					//TODO: finalizzo
				}
				else
				{
					//Il server ha risposto in qualche altro modo
					//Errore!
					Logger.log.error("Il server ha risposto con un messaggio non atteso!!");
					//Chiudo il canale rudp
					messageOut.close();
					messageIn.close();
					this.channel.close();
					guardTray.displayMessageERROR("Errore connessione!!");
					guardTray.enableConnection();
					//TODO: finalizzo
				}					
				
			}
			else
			{
				guardTray.displayMessageERROR("Timeout connessione con il server!");
				guardTray.enableConnection();
				//TODO: finalizzo
			}	
			
		}
		catch (IOException ioe)
		{
			Logger.log.error("Guard.connect:" + Utility.exceptionToString(ioe));
		}
		catch (ClassNotFoundException cnfe) 
		{
			Logger.log.error("Guard.connect:" + Utility.exceptionToString(cnfe));
		}
		
	}
	
	public void disconnect()
	{
		try
		{
			Logger.log.info("DISCONNECT CALL!");
			active = false;
			
			if (messageOut!=null)
			{
				//Invio KILL al server
				CommandMessage messageResponse = new CommandMessage(Commands.KILL);
				messageResponse.writeCommandMessage(messageOut);
				if (isInView)
				{
					this.isInView = false;
					this.rtpVideoAudioTransmitter.stop();
				}
				messageOut.close();
				messageIn.close();
				this.channel.close();
			}
			
			//TODO: finalizzo
			guardTray.displayMessageINFO("Disconnesso dal server!!");
		}
		catch (IOException ioe)
		{
			//Logger.log.error("Guard.connect:" + Utility.exceptionToString(ioe));
		}
	}
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new Guard();
	}
	
}
