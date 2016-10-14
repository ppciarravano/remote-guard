package ppc.remoteguard.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.IncompatibleSourceException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.swing.JOptionPane;

import ppc.remoteguard.RTPMultiplexerConnector;
import ppc.remoteguard.UDPMultiplexer;
import ppc.remoteguard.guard.GuardTray;
import ppc.remoteguard.log.Logger;
import ppc.remoteguard.util.ConstantManager;
import ppc.remoteguard.util.Utility;

/**
 * Classe RTPVideoAudioReceiver per ricevere gli stream RTP utilizzando RTPMultiplexerConnector.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class RTPVideoAudioReceiver implements ReceiveStreamListener, SessionListener, ControllerListener
{
	private UDPMultiplexer server;
	private int idClient;
    private String guardName;
    private Controller controller;
	
    private RTPManager sessionManagers[] = null;
    private Player player = null;
    private PlayerWindow playerWindow = null;
    private Vector<DataSource> dataSources = new Vector<DataSource>();
        
    private boolean streamsReceived = false;
    private long startConnectionTime;
    private Semaphore connSemaphore;
            

    public RTPVideoAudioReceiver(Controller controller, String guardName) {
	
    	this.server = controller.getUDPMultiplexerClient();
    	this.idClient = controller.getIdClient();
    	this.controller = controller;
    	this.guardName = guardName;
    }

    protected boolean initialize() {

        try
        {
        	sessionManagers = new RTPManager[2];
        	//Open the RTP sessions
        	for (int sessionIndex = 0; sessionIndex < 2; sessionIndex++)
        	{
        		sessionManagers[sessionIndex] = (RTPManager)RTPManager.newInstance();
        		sessionManagers[sessionIndex].addSessionListener(this);
        		sessionManagers[sessionIndex].addReceiveStreamListener(this);
        		
        		if (sessionIndex==0)
        		{
        			//Inizializzo lo stream video
        			sessionManagers[sessionIndex].initialize(new RTPMultiplexerConnector(server, this.idClient, 1, 2));
        			BufferControl bc = (BufferControl)sessionManagers[sessionIndex].getControl("javax.media.control.BufferControl");
        			if (bc != null)
        			{
        				Logger.log.info("setBufferLength VIDEO_BUFFER_LENGTH: " + ConstantManager.VIDEO_BUFFER_LENGTH);
        				bc.setBufferLength(ConstantManager.VIDEO_BUFFER_LENGTH);
        			}
        		}
        		else if (sessionIndex==1)
        		{
        			//Inizializzo lo stream audio
        			sessionManagers[sessionIndex].initialize(new RTPMultiplexerConnector(server, this.idClient, 3, 4));
        			BufferControl bc = (BufferControl)sessionManagers[sessionIndex].getControl("javax.media.control.BufferControl");
        			if (bc != null)
        			{
        				Logger.log.info("setBufferLength AUDIO_BUFFER_LENGTH: " + ConstantManager.AUDIO_BUFFER_LENGTH);
        				bc.setBufferLength(ConstantManager.AUDIO_BUFFER_LENGTH);
        			}
        		}
					
        	}
	    
        } 
        catch (Exception e)
        {
        	Logger.log.error("Cannot create the RTP Session:" + Utility.exceptionToString(e));
            return false;
        }

        
        //Attendo la connessione RTP fino al raggiungimento del timeout:
        //Implementato utilizzando uno ScheduledExecutorService
        //Piu' semplicemente era implementabile con il seguente codice:
        /*
        	Object dataSync = new Object();
         	try
         	{
	    		synchronized (dataSync)
	    		{
					while (!streamsReceived && 
						System.currentTimeMillis() - startConnectionTime < ConstantManager.TIMEOUT_RTP_CONNECTION)
						{
				    		if (!streamsReceived)
				    			System.out.println("Waiting....");
				    		dataSync.wait(1000);
						}
	    		}
			}
			catch (Exception e) {}
			
			//Per notificare ad intialize() che lo stream e' connesso
			synchronized (dataSync)
			{
		    	streamsReceived = true;
		    	dataSync.notifyAll();
			}
         */
        connSemaphore = new Semaphore(0);
        startConnectionTime = System.currentTimeMillis();
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
		final Runnable connectionReceived = new Runnable() {
            			public void run() { 
            				
            				if (((!streamsReceived) && 
            						((System.currentTimeMillis() - startConnectionTime) < ConstantManager.TIMEOUT_RTP_CONNECTION)))
            				{
            					//Waiting for connection...
            					Logger.log.info("WAIT FOR RTP STREAMS FROM GUARD....");
            					
            				}
            				else if (((!streamsReceived) && 
            						((System.currentTimeMillis() - startConnectionTime) > ConstantManager.TIMEOUT_RTP_CONNECTION)))
            				{
            					//Timeout raggiunto senza connessione: return false
            					Logger.log.info("TIMEOUT STREAM RTP CONNECTION!!");
            					connSemaphore.release();
            					//sendConnectionHandle.cancel(true); //Dovrei dichiarare sendConnectionHandle a livello di attributo di classe e richiamarlo qui
            					scheduler.shutdownNow(); //Preferisco usare lo shutdownNow per non modificare la visibilita' di sendConnectionHandle
            				}
            				else if (streamsReceived)
            				{
            					//Connessione RTP avvenuta: return true
            					Logger.log.info("CONNECT TO STREAM RTP!!");
            					connSemaphore.release();
            					//sendConnectionHandle.cancel(true); //Dovrei dichiarare sendConnectionHandle a livello di attributo di classe e richiamarlo qui
            					scheduler.shutdownNow(); //Preferisco usare lo shutdownNow per non modificare la visibilita' di sendConnectionHandle
            				}
            			}
            		};
            	//final ScheduledFuture<?> sendConnectionHandle = 
            	scheduler.scheduleAtFixedRate(connectionReceived, 0, ConstantManager.DELAY_RETRY_CONNECTION, TimeUnit.MILLISECONDS);    
        
        //Aspetto finche' non e' scaduto il timeout o non ho ricevuto la connessione
		try {
			connSemaphore.acquire();
		} catch (InterruptedException ie) {
			Logger.log.error(Utility.exceptionToString(ie));
		}
		
		
		if (!streamsReceived) {
			closeSession();
		    return false;
		}

        return true;
    }

    

    /**
     * Close the players and the session managers.
     */
    public void closeSession()
    {
    	//if (playerWindow!=null)
    	//{
    	//	playerWindow.close();
    	//}
	
		// close the RTP session.
		for (int sessionIndex = 0; sessionIndex < sessionManagers.length; sessionIndex++) {
		    if (sessionManagers[sessionIndex] != null)
		    {
	                sessionManagers[sessionIndex].removeTargets("Closing session from RTPVideoAudioReceiver");
	                sessionManagers[sessionIndex].dispose();
	                sessionManagers[sessionIndex] = null;
		    }
		}
    }
    
    public void closePlayerAndSession()
    {
    	if (playerWindow!=null)
    	{
    		playerWindow.close();
    	}
    }
 

    /**
     * SessionListener.
     */
	public synchronized void update(SessionEvent evt)
	{
		if (evt instanceof NewParticipantEvent)
		{
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			Logger.log.debug("A new participant had just joined: " + p.getCNAME());
		}
	}


	/**
	 * ReceiveStreamListener
	 */
	public synchronized void update(ReceiveStreamEvent evt)
	{
		RTPManager mgr = (RTPManager) evt.getSource();
		ReceiveStream stream = evt.getReceiveStream(); //potrebbe essere null
		Participant participant = evt.getParticipant(); //potrebbe essere null

		if (evt instanceof RemotePayloadChangeEvent)
		{
			Logger.log.error("Received an RTP PayloadChangeEvent, sorry, cannot handle payload change!");
			throw new RuntimeException("Errore RTPVideoAudioReceiver: ricevuto un RTP PayloadChangeEvent!!");
		}
		else if (evt instanceof NewReceiveStreamEvent)
		{
			if (!streamsReceived)
			{
				try
				{
					stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
					DataSource ds = stream.getDataSource();
	
					// Find out the formats.
					RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
					if (ctl != null)
						Logger.log.debug("Recevied new RTP stream: " + ctl.getFormat());
					else
						Logger.log.debug("Recevied new RTP stream");
	
					if (participant == null)
						Logger.log.debug("The sender of this stream had yet to be identified.");
					else
						Logger.log.debug("The stream comes from: " + participant.getCNAME());
					
					//Aggiungo il DataSource in arrivo al vettore
					dataSources.addElement(ds);
					
					//Se il vettore e' pieno dei due datasources arrivati: video e audio
					if (dataSources.size()==2)
					{
						//Allora vado a fare il merge dei datasource e creo il player
						DataSource mergedDataSource = null;
						try
						{
							//Creo l'array da passare a Manager.createMergingDataSource
							DataSource[] dataSourcesArray = new DataSource[2];
							dataSourcesArray[0] = dataSources.elementAt(0);
							dataSourcesArray[1] = dataSources.elementAt(1);
							mergedDataSource = javax.media.Manager.createMergingDataSource(dataSourcesArray);
						}
						catch (IncompatibleSourceException ise)
						{
							Logger.log.error(Utility.exceptionToString(ise));
							throw ise;
						}
	
						//Creo il player usando il mergedDataSource
						player = javax.media.Manager.createPlayer(mergedDataSource);
						if (player == null)
						{
							throw new RuntimeException("Errore creazione Player RTP: risultato null!!");
						}
						
						player.addControllerListener(this);
						player.realize();
						playerWindow = new PlayerWindow(player);
						
						streamsReceived = true;
					}
	
				}
				catch (Exception e)
				{
					Logger.log.error(Utility.exceptionToString(e));
					throw new RuntimeException(e);
				}
			}
		}
		else if (evt instanceof StreamMappedEvent)
		{
			if ((stream != null) && (stream.getDataSource() != null))
			{
				DataSource ds = stream.getDataSource();
				// Find out the formats.
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				if (ctl != null)
					Logger.log.debug("The previously unidentified stream: " + ctl.getFormat());
				if (participant != null)
					Logger.log.debug("Had now been identified as sent by: " + participant.getCNAME());
			}
		}
		else if (evt instanceof ByeEvent)
		{
			if (participant != null)
				Logger.log.debug("Got \"bye\" from: " + participant.getCNAME());
			if (playerWindow != null)
			{
				playerWindow.close();
				playerWindow = null;
			}
		}

	}

    
	/**
	 * ControllerListener for the Players.
	 */
	public synchronized void controllerUpdate(ControllerEvent ce)
	{
		Player p = (Player) ce.getSourceController();
		if (p == null)
		{
			return;
		}
		
		//Eseguito quando il player e' realized
		if (ce instanceof RealizeCompleteEvent)
		{
			if (playerWindow == null)
			{
				//Some strange happened.
				throw new RuntimeException("playerWindow null, in una parte di codice dove non era atteso!");
			}
			playerWindow.initialize();
			playerWindow.setVisible(true);
			
			//Nel caso avessi 2 player distinti e separati per audio e video,
			//senza aver effettuato il merge dei datasource, per sincronizzare
			//l'audio e il video devo eseguire in questo punto prima dello start su i players la seguente riga:
			//L_PLAYER2.setTimeBase(L_PLAYER1.getTimeBase());
			p.start();
			
		}
		if (ce instanceof ControllerErrorEvent)
		{
			p.removeControllerListener(this);
			if (playerWindow != null)
			{
				playerWindow.close();
				playerWindow = null;
			}
			Logger.log.error("Ricevuto un evento di errore ControllerErrorEvent: " + ce);
		}
		
	}


	/**
	 * GUI classes for the Player.
	 */
	class PlayerWindow extends Frame
	{
		private Player player;

		PlayerWindow(Player p)
		{
			Logger.log.debug("INIT PlayerWindow!");
			player = p;
			
			this.setLocation(100,100);
			this.setTitle("RemoteGuard (Controller) - "+guardName);
			//this.setResizable(false);
			this.toFront();
					
			addWindowListener( new  WindowAdapter() {  
				         public void windowClosing( WindowEvent e ) {  
					             int option = JOptionPane.showOptionDialog(  
					            		 PlayerWindow.this,  
					                     "Sei sicuro che vuoi interrompere la connessione Audio/Video?",  
					                     "Chiusura visualizzazione Guardia", JOptionPane.YES_NO_OPTION,  
					                     JOptionPane.WARNING_MESSAGE, null, null, null );  
					             if( option == JOptionPane.YES_OPTION )
					             {  
					                 //CHIUDE La guardia inviando un messaggio alla guard
					            	 controller.stopViewer();
					            	 controller.getControllerTray().enableExitViewGuard();
					            	 close();
					             }  
					         }  
					     });
			
						
			//Leggo l'icona da visualizzare in alto a sinistra e la setto
			try
			{
				InputStream ioStream = GuardTray.class.getResourceAsStream("/ppc/remoteguard/resources/controller_icon.gif");
	        	Image image = ImageIO.read(ioStream);
	        	if (image!=null)
	        		this.setIconImage(image);
			}
			catch (IOException ioe)
			{
				Logger.log.error(Utility.exceptionToString(ioe));
			}
			
		}

		public void initialize()
		{
			add(new PlayerPanel(player));
		}

		public void close()
		{
			closeSession();
			player.close();
			setVisible(false);
			dispose();
		}

		public void addNotify()
		{
			super.addNotify();
			pack();
		}
	}

	/**
	 * GUI classes for the Player.
	 */
	class PlayerPanel extends Panel
	{
		Component visualComponent;
		Component controlComponent;

		PlayerPanel(Player p)
		{
			Logger.log.debug("INIT PlayerPanel!");
			setLayout(new BorderLayout());
			if ((visualComponent = p.getVisualComponent()) != null)
			{
				add("Center", visualComponent);
			}
			if ((controlComponent = p.getControlPanelComponent()) != null)
			{
				add("South", controlComponent);
			}
		}

		public Dimension getPreferredSize()
		{
			int w = 0, h = 0;
			if (visualComponent != null)
			{
				Dimension size = visualComponent.getPreferredSize();
				w = size.width;
				h = size.height;
			}
			if (controlComponent != null)
			{
				Dimension size = controlComponent.getPreferredSize();
				if (w == 0)
					w = size.width;
				h += size.height;
			}
			if (w < 160)
				w = 160;
			return new Dimension(w, h);
		}
	}

    
}




