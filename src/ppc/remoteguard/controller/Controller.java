package ppc.remoteguard.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Hashtable;

import javax.swing.JOptionPane;

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
 * Classe che gestisce tutta la logica del Controller.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class Controller extends Thread
{

	private UDPMultiplexerClient client = null;
	private int idClient;
	private ObjectOutputStream messageOut = null;
	private ObjectInputStream messageIn = null;
		
	private DatagramWrapper dw = null;
	private ReliableMessage channel = null;
	private RTPVideoAudioReceiver rtpVideoAudioReceiver = null;
	private ControllerTray controllerTray = null;
	
	private boolean active;
			
	public Controller() throws IOException
	{
		controllerTray = new ControllerTray(this);
		LoggerWindow.init("RemoteGuard (Controller) Log Output", controllerTray.getImageIcon());
		//LoggerWindow.show();
		active = false;
		this.start();		
	}
				
		
	public void connect(String hostName, int udpPortNumber, String password)
	{
		Logger.log.info("hostName:"+hostName+" udpPortNumber:"+udpPortNumber+" password:"+password);
		
		try
		{			
			//Inizializzo la connessione
			InetAddress IPAddress = InetAddress.getByName(hostName);
			Logger.log.info("IPAddress:"+IPAddress);
			client = new UDPMultiplexerClient(udpPortNumber, IPAddress);
			
			if (client.connect())
			{
				this.idClient = client.getIdClient();
				
				//TODO:Risoluzione parziale del conflitto di sincronizzazione sull'apertura del canale RUDP
				try{
					Thread.sleep(3000);
				}catch (InterruptedException e){ }
				
				//Inizializzo la connessione affidabile rudp
				this.dw = new DatagramWrapper(client, this.idClient);
				this.channel = new ReliableMessage(dw);
				channel.addStateListener(new StateListener(){
					//Azione da compiere in caso di chiusura connessione
					public void connectionClosed(ReliableSocket sock) {
						Logger.log.info("Connessione chiusa... Finalizzo il controller!");
						controllerTray.displayMessageERROR("Disconnesso dal server!!");
						controllerTray.enableConnection();
						
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
				messageRequest.putParam(ParamsName.PASSWORD, password);
				messageRequest.putParam(ParamsName.CLIENT_TYPE, ClientType.CONTROLLER);
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
					//Lancio il thread di ricezione messaggi del Controller
					Logger.log.info("Controller Thread Run is active...");
					controllerTray.displayMessageINFO("Controller attivo connesso!");
					active = true;
										
				}
				else if (messageResponse.getCommand().equals(Commands.WRONGPW))
				{
					Logger.log.info("WRONG PASSWORD FROM SERVER!!!!!");
					//Chiudo il canale rudp
					messageOut.close();
					messageIn.close();
					this.channel.close();
					controllerTray.displayMessageERROR("Password errata!!");
					controllerTray.enableConnection();
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
					controllerTray.displayMessageERROR("Errore connessione!!");
					controllerTray.enableConnection();
					//TODO: finalizzo
				}					
				
			}
			else
			{
				controllerTray.displayMessageERROR("Timeout connessione con il server!");
				controllerTray.enableConnection();
				//TODO: finalizzo
			}	
			
		}
		catch (IOException ioe)
		{
			Logger.log.error("Controller.connect:" + Utility.exceptionToString(ioe));
		}
		catch (ClassNotFoundException cnfe) 
		{
			Logger.log.error("Controller.connect:" + Utility.exceptionToString(cnfe));
		}
		
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
							
						case ACKLIST:
							Hashtable<Integer, String> listGuards = (Hashtable<Integer, String>)messageRequest.getParam(ParamsName.LIST_GUARDS);
							//se la listGuards e' vuota lancio un alert
							if (listGuards.size()==0)
							{
								JOptionPane.showMessageDialog(null, "Nessuna Guard attualmente connessa al server!!", "Attenzione!!", JOptionPane.WARNING_MESSAGE); 
							}
							else
							{
								//altrimenti visualizzo la finestra con una lista per la scelta della guard da visualizzare 
								ChoiceGuardWindow cw = new ChoiceGuardWindow(listGuards, this, controllerTray.getImageIcon());
							}
							
							break;
						
						case CLIENT_NOT_FOUND:
							JOptionPane.showMessageDialog(null, "Non e' stata trovata piu' attiva la Guard richiesta!!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
							
							break;
							
						case DISCONNECT:
							JOptionPane.showMessageDialog(null, "C'e' gia' un Controller connesso al server,\nnon e' possibile connettere piu' controller contemporaneamente!!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
							disconnect();
							
							break;
							
						case START_VIEWER:
							Logger.log.info("RECEIVE START_VIEWER!");
							startViewer();
							break;
							
						case STOP_VIEWER_CONTROLLER:
							Logger.log.info("Ricevuto STOP_VIEWER_CONTROLLER dal server!");
							controllerTray.enableExitViewGuard();
							rtpVideoAudioReceiver.closePlayerAndSession();				
							
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
	
	private void startViewer() throws IOException
	{
		//Star receiver RTP
		this.rtpVideoAudioReceiver = new RTPVideoAudioReceiver(this, ""); //TODO: gestione nome guard
		boolean result = rtpVideoAudioReceiver.initialize();
		Logger.log.info("RTP video/audio start: result:" + result);
		
	}
	
	
	public void sendViewRequest(int idClient)
	{
		//Invio la richiesta al server che poi la inoltrera' alla guard
		Logger.log.info("sendViewRequest idClient:" + idClient);
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.VIEWID);
			messageRequest.putParam(ParamsName.IDCLIENT, new Integer(idClient));
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
		}
		catch (IOException ioe)
		{
			Logger.log.error("Controller.sendViewRequest:" + Utility.exceptionToString(ioe));
		}
				
	}
	
	public void stopViewer()
	{
		//Invio la richiesta al server che poi la inoltrera' alla guard
		Logger.log.info("send stopView !!!");
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.STOP_VIEWER);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
		}
		catch (IOException ioe)
		{
			Logger.log.error("Controller.sendViewRequest:" + Utility.exceptionToString(ioe));
		}
				
	}
	
	public void getListGuards()
	{
		try
		{
			//Preparo il messaggio
			CommandMessage messageRequest = new CommandMessage(Commands.LIST);
			Logger.log.info("Commands messageRequest:"+messageRequest);
			//Invio il messaggio
			messageRequest.writeCommandMessage(messageOut);
						
		}
		catch (IOException ioe)
		{
			Logger.log.error("Controller.getListGuards:" + Utility.exceptionToString(ioe));
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
				messageOut.close();
				messageIn.close();
				this.channel.close();
			}
			
			//TODO: finalizzo
			controllerTray.displayMessageINFO("Disconnesso dal server!!");
		}
		catch (IOException ioe)
		{
			Logger.log.error("Controller.connect:" + Utility.exceptionToString(ioe));
		}
	}
	
	public UDPMultiplexerClient getUDPMultiplexerClient()
	{
		return this.client;
	}
	
	public int getIdClient()
	{
		return this.idClient;
	}
	
	public ControllerTray getControllerTray()
	{
		return this.controllerTray;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new Controller();
	}
	
}
