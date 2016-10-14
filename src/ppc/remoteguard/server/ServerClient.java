package ppc.remoteguard.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rudp.ReliableSocket;
import ppc.remoteguard.CommandMessage;
import ppc.remoteguard.UDPMultiplexer;
import ppc.remoteguard.CommandMessage.ClientType;
import ppc.remoteguard.CommandMessage.Commands;
import ppc.remoteguard.CommandMessage.ParamsName;
import ppc.remoteguard.log.Logger;
import ppc.remoteguard.rudp.DatagramWrapper;
import ppc.remoteguard.rudp.ReliableMessage;
import ppc.remoteguard.rudp.StateListener;
import ppc.remoteguard.util.ConstantManager;
import ppc.remoteguard.util.Utility;

/*
 * Questa classe si occupa di gestire il riconoscimento  dei client,
 * successivamente all'handshaking, utilizzando un timeout qualora non sia stato ricevuto un messaggio
 * di "IAM", e finalizzando l'oggetto nella mappa di ServerClient presente nella classe Server.
 * Inoltre gestisce tutta la comunicazione con il client e il canale ReliableUDP con ObjectInputStream e ObjectOutputStream,
 * con i relativi controllo e finalizzazione nel caso la connessione dovesse chiudersi da parte del client
 */
/**
 * Classe per gestire la logica di comunicazione con le istanze dei vari client connessi (Guard o Controller).
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ServerClient extends Thread 
{

	private int idClient;
	private UDPMultiplexer udpMultiplexer = null;
	private Server instanceServer = null;
	
	private enum Actions { INHELLO, RECEIVE }
	private Actions state;
	private boolean active;
	private ClientType clientType; //GUARD o CONTROLLER
	
	private ObjectInputStream messageIn = null;
	private ObjectOutputStream messageOut = null;
	private ReliableMessage channel = null;
	
	private String guardName = "";
	
	public ServerClient(int idClient, Server instanceServer, UDPMultiplexer udpMultiplexer)
	{
		Logger.log.info("Creato un nuovo ServerClient!");
		this.idClient = idClient;
		this.instanceServer = instanceServer;
		this.udpMultiplexer = udpMultiplexer;
		
		active = true;
		state = Actions.INHELLO;
	}
		
	
	public void run()
	{
		while (active)
		{
						
			try
			{
				switch (state) {
				
					case INHELLO:
						inHello();
						break;
			
					case RECEIVE:
						receive();
						break;	
												
					default:
						//Messaggio ignorato
						
						break;
						
				}
			}
			catch (Exception e)
			{
				Logger.log.error(Utility.exceptionToString(e));
			} 
		}
	}
	
	private void receive()
	{
		try
		{
			//Leggo il messaggio dal client
			CommandMessage messageRequest = CommandMessage.readCommandMessage(messageIn);
			Logger.log.info("Commands messageRequest from Guard/Controller:"+messageRequest);
			
			//Identificazione messaggio e esecuzione azione comando richiesto dal server
			switch (messageRequest.getCommand()) {
			
				case LIST:
					//Il controller richiede al server di inoltrare la lista delle Guard attive
					Logger.log.info("Il controller richiede al server di inoltrare la lista delle Guard attive");
					CommandMessage messageResponse = new CommandMessage(Commands.ACKLIST);
					
					//Costruisco la hashtable delle guard connesse da restituire
					Hashtable<Integer, String> listGuards = new Hashtable<Integer, String>();
					Enumeration<Integer> ids = instanceServer.getGuardsHash().keys();
					while(ids.hasMoreElements())
					{
						Integer idClientInHash = ids.nextElement();
						String guardNameInHash = instanceServer.getGuardsHash().get(idClientInHash).guardName;
						listGuards.put(idClientInHash, guardNameInHash);
					}
					messageResponse.putParam(ParamsName.LIST_GUARDS, listGuards);
					Logger.log.info("Commands messageResponse:"+messageResponse);
					//Invio il messaggio
					messageResponse.writeCommandMessage(messageOut);
											
					break;
		
				case VIEWID:
					//Il controller richiede al server di inoltrare la richiesta di view alla guard corrispondente
					Integer idClientToView = (Integer)messageRequest.getParam(ParamsName.IDCLIENT);
					Logger.log.info("Controller request VIEWID: " + idClientToView);
					
					//Inoltro la richiesta al client corrispondete
					boolean resultSearch = instanceServer.searchClientAndsendViewRequest(idClientToView.intValue());
					if (!resultSearch)
					{
						//Se il client non e' stato trovato nell'hashtable del server allora
						//Inoltro un messaggio CLIENT_NOT_FOUND al controller
						CommandMessage messageResponseCnf = new CommandMessage(Commands.CLIENT_NOT_FOUND);
						//Invio il messaggio
						Logger.log.info("Commands.CLIENT_NOT_FOUND messageResponseCnf:"+messageResponseCnf);
						messageResponseCnf.writeCommandMessage(messageOut);
					}
										
					break;
					
				case ACKVIEW:
					//La guard attiva la trasmissione audio/video, rimando un messaggio START_VIEW al controller
					Logger.log.info("Server receive: ACKVIEW from guard");
					boolean sendResult = instanceServer.sendAckViewToController();
					if (!sendResult)
					{
						//Se il controller non risulta connesso dare un messaggio di STOP_VIEW alla guardia
						//Inoltro un messaggio STOP_VIEW alla guardia
						CommandMessage messageResponseSv = new CommandMessage(Commands.STOP_VIEW);
						//Invio il messaggio
						Logger.log.info("Commands.STOP_VIEW messageResponseSv:"+messageResponseSv);
						messageResponseSv.writeCommandMessage(messageOut);
					}
					
					break;
				
				case STOP_VIEWER:
					Logger.log.info("Ricevuto STOP_VIEWER dal controller!");
					instanceServer.sendStopView();					
					
					break;
				
				case KILL:
					//Il client richiede al server di interrompere la connessione
					Logger.log.info("Il client richiede al server di interrompere la connessione: idClient:"+idClient+" clientType:"+clientType);
					
					instanceServer.stopViewerController();
					
					//Cancello l'istanza di ServerClient da guardsMap o da Controller del Server
					if (clientType==ClientType.GUARD)
					{
						//Cancello da Server.guardsMap
						if (instanceServer.getGuardsHash().containsKey(new Integer(idClient)))
			            {
							instanceServer.getGuardsHash().remove(new Integer(idClient));
			            }
					}
					else
					{
						//Cancello da Server.controller
						instanceServer.setController(null);
					}
					closeRUDP();
					active = false;
					
					break;	
					
				default:
					//Messaggio ignorato
					
					break;
			}
			
		}
		catch (IOException ioe) {
			//Logger.log.error(Utility.exceptionToString(ioe));
		} catch (ClassNotFoundException cnfe) {
			//Logger.log.error(Utility.exceptionToString(cnfe));
		}
	}
	
	public void sendViewRequest()
	{
		//Utilizzato dal server per inoltrare la richiesta di VIEW ad una guard
		Logger.log.info("Ready for SendViewRequest to idclient:" + this.idClient);
		
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.VIEW);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
						
		}
		catch (IOException ioe)
		{
			Logger.log.error("Guard.getListGuards:" + Utility.exceptionToString(ioe));
		}
	}
	
	public void sendStopView()
	{
		//Utilizzato dal server per inoltrare la richiesta di STOP_VIEW ad una guard
		Logger.log.info("STOP_VIEW to idclient:" + this.idClient);
		
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.STOP_VIEW);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
						
		}
		catch (IOException ioe)
		{
			Logger.log.error("Guard.getListGuards:" + Utility.exceptionToString(ioe));
		}
	}
	
	public void sendStartViewer()
	{
		//Utilizzato dal server per inoltrare la richiesta di START_VIEWER al controller
		Logger.log.info("Send Start viewer to controller");
		
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.START_VIEWER);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
						
		}
		catch (IOException ioe)
		{
			Logger.log.error("Guard.getListGuards:" + Utility.exceptionToString(ioe));
		}
		
	}
	
			
	public void stopViewerController()
	{
		//Utilizzato dal server per inoltrare la richiesta di STOP_VIEWER_CONTROLLER al controller
		Logger.log.info("Send STOP_VIEWER_CONTROLLER to controller");
		
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.STOP_VIEWER_CONTROLLER);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
						
		}
		catch (IOException ioe)
		{
			Logger.log.error("Guard.getListGuards:" + Utility.exceptionToString(ioe));
		}
		
	}
	
	private void inHello()
	{
		
		try
		{
			
			//inizio conversazione con Guard
			DatagramWrapper dw = new DatagramWrapper(udpMultiplexer, this.idClient);
			channel = new ReliableMessage(dw);
			//-----------------------------			
			//channel.addStateListener(new StateListener()); 		
			//channel.addListener(new PacketListener()); 		
			channel.connect();
			
			//Inizializzo ObjectInputStream
			messageIn = new ObjectInputStream(channel.getInputStream());
			//Leggo il messaggio dalla guard
			CommandMessage messageRequest = CommandMessage.readCommandMessage(messageIn);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Inizializzo ObjectOutputStream (lo devo fare non prima di aver almeno inviato un messaggio in outputStream sul canale rudp)
			messageOut = new ObjectOutputStream(channel.getOutputStream());
			
			
			//Controllo che il messaggio arrivato sia di tipo Commands.IAM
			if (messageRequest.getCommand().equals(Commands.IAM))
			{
						
				//Identifico se guard o controller
				clientType = (ClientType)messageRequest.getParam(ParamsName.CLIENT_TYPE);
								
				//Controllo Password
				String passwordFromGuard = (String)messageRequest.getParam(ParamsName.PASSWORD);
				String passwordFromGuardMD5 = Utility.stringToMD5(passwordFromGuard);
				Logger.log.info("Password from Client:"+passwordFromGuard+ " ("+passwordFromGuardMD5+")");
				CommandMessage messageResponse = null;
				
				String md5CorrectPw = "";
				if (clientType==ClientType.GUARD)
					md5CorrectPw = ConstantManager.SERVER_PASSWORD_MD5_GUARD;
				else 
					md5CorrectPw = ConstantManager.SERVER_PASSWORD_MD5_CONTROLLER;
				
				if(passwordFromGuardMD5.equals(md5CorrectPw))
				{
					//Preparo messaggio di response HELLO
					messageResponse = new CommandMessage(Commands.HELLO);
					Logger.log.info("Commands messageResponse:"+messageResponse);
					//Invio il messaggio
					messageResponse.writeCommandMessage(messageOut);
					Logger.log.info("PASSWORD CORRETTA!");	
					
					if (clientType==ClientType.GUARD)
					{
						//Se il client e' una Guard
						guardName = (String)messageRequest.getParam(ParamsName.GUARDNAME);
						Logger.log.info("Nome Guard connessa: " + guardName);
						
						//Aggiungo l'instanza di ServerClient (this) alla hashmap guardsMap di Server
						if (instanceServer.getGuardsHash().containsKey(new Integer(idClient)))
			            {
			            	//Errore: nell'Hashmap delle guardie e' gia' presente un client con questo id
		            		Logger.log.error("Errore: nell'Hashmap delle guardie e' gia' presente un client con questo id!!");
			            
				    		//Preparo messaggio di response DISCONNECT
							messageResponse = new CommandMessage(Commands.DISCONNECT);
							Logger.log.info("Commands messageResponse:"+messageResponse);
							//Invio il messaggio
							messageResponse.writeCommandMessage(messageOut);
							Logger.log.info("CLIENT DISCONNECT");	
		            		
		            		closeRUDP();
			            }
		            	else
		            	{
		            		Logger.log.info("Creato e aggiunto un nuovo ServerClient Guard!");
		            		instanceServer.getGuardsHash().put(new Integer(idClient), this);
		            		state = Actions.RECEIVE;
		            		addFinalizeStateListener();
		            	}
						
					}
					else
					{
						//Se il cliente e' un Controller
						
						//Controllo se c'e' gia' un controller connesso al server
						if (instanceServer.getController()!=null)
						{
							//Esiste gia' un controller connesso al server
							Logger.log.info("Controller gia' connesso, non e' possibile connettere piu' di un Controller nello stesso tempo!!");
							
							//Preparo messaggio di response DISCONNECT
							messageResponse = new CommandMessage(Commands.DISCONNECT);
							Logger.log.info("Commands messageResponse:"+messageResponse);
							//Invio il messaggio
							messageResponse.writeCommandMessage(messageOut);
							Logger.log.info("CLIENT DISCONNECT");	
							
							closeRUDP();
						}
						else
						{
							//Popolo l'instanza di controller su instanceServer (Server)
							instanceServer.setController(this);
							state = Actions.RECEIVE;
							addFinalizeStateListener();
						}
					}					
						
					
				}
				else
				{
					//Preparo messaggio di response WRONGPW
					messageResponse = new CommandMessage(Commands.WRONGPW);
					Logger.log.info("Commands messageResponse:"+messageResponse);
					//Invio il messaggio
					messageResponse.writeCommandMessage(messageOut);
					Logger.log.info("PASSWORD SBAGLIATA!");	
					
					closeRUDP();
				}
				
			}
			else
			{
				//Non e' un messaggio di IAM
				Logger.log.error("Non ho un messaggio di IAM dove era atteso!");	

				closeRUDP();
			}
		}
		catch (IOException ioe)
		{
			Logger.log.info("Catch IOException ioe e gestita!");
			Logger.log.debug(Utility.exceptionToString(ioe));
			active = false;
		}
		catch (ClassNotFoundException e)
		{
			Logger.log.error(Utility.exceptionToString(e));
			active = false;
		}
				
	}
	
	private void addFinalizeStateListener()
	{
		channel.addStateListener(new StateListener(){
			//Azione da compiere in caso di chiusura connessione
			public void connectionClosed(ReliableSocket sock) {
				Logger.log.info("Connessione chiusa... Finalizzo il ServerClient: idClient:"+idClient+" clientType:"+clientType);
				//Cancello l'istanza di ServerClient da guardsMap o da Controller del Server
				if (clientType==ClientType.GUARD)
				{
					//Cancello da Server.guardsMap
					if (instanceServer.getGuardsHash().containsKey(new Integer(idClient)))
		            {
						instanceServer.getGuardsHash().remove(new Integer(idClient));
		            }
				}
				else
				{
					//Cancello da Server.controller
					if ((instanceServer.getController()!=null)&&(instanceServer.getController().getIdClient()==idClient))
					{
						Logger.log.info("setController TO NULL!!");
						instanceServer.setController(null);
					}
				}
				active = false;
			}
			/*
			public void connectionFailure(ReliableSocket sock) {
				Logger.log.info("Connessione chiusa... Finalizzo il ServerClient: idClient:"+idClient);
				active = false;
			}
			*/
		});
	}
	
	public int getIdClient()
	{
		return this.idClient;
	}
	
	public UDPMultiplexer getUdpMultiplexer()
	{
		return this.udpMultiplexer;
	}
	
	//Serve per la classe Server, che richimandolo si occupa di cancellare l'istanza dalla hashmap serversMap e finalizzare l'oggetto
	public boolean isActive()
	{
		return active;
	}
	
	public void closeRUDP() throws IOException
	{
		//Chiudo il canale rudp
		messageOut.close();
		messageIn.close();
		channel.close();
		active = false;
	}
	
}
