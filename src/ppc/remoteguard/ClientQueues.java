package ppc.remoteguard;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Classe di gestione delle code dei pacchetti in arrivo del client/server,
 * gestita attraverso un array di 5 elementi di LinkedBlockingQueue, 
 * uno per il canale di controllo, e gli altri 4 per la comunicazione RTP.<br>
 * 
 * TODO:Si potrebbe migliorare queste coda implementando (in parte implementato, ma commentato) un ciclo periodico su tutte le code,
 * che va ad eliminare i messaggi piu' vecchi di un certo tempo, questo per evitare che
 * messaggi ormai vecchi rimangano in coda. Per l'applicazione RemoteGuard questo problema pero' non crea
 * particolari conseguenze.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ClientQueues //extends Thread
{
	private LinkedBlockingQueue<UDPPacket>[] messageQueues;
	private static final int CHANNELS_NUMBER = 5;
	//private static final int EXPIRE_TIME_PACKET = 1000;
	//private static final int EXPIRE_TIME_CLIENT = 600000; //10 minuti
	private boolean activeClient;
	//private long lastAccess;
	private int idClient;
	
	@SuppressWarnings("unchecked")
	public ClientQueues(int idClient)
	{
		activeClient = true;
		this.idClient = idClient;
		this.messageQueues = new LinkedBlockingQueue[CHANNELS_NUMBER]; //new LinkedBlockingQueue<UDPPacket>[CHANNELS_NUMBER];
		for (int i = 0; i < messageQueues.length; i++) {
			this.messageQueues[i] = new LinkedBlockingQueue<UDPPacket>();
		}
		//lastAccess = System.currentTimeMillis();
		//this.start(); //TODO
	}	
	
	//Metodo non sincronizzato in quanto l'oggetto LinkedBlockingQueue ha gia' al suo interno un meccanismo di sincronizzazione
	public boolean putInQueue(UDPPacket udpPacket)
	{
		//lastAccess = System.currentTimeMillis();
		
		if (udpPacket.getIdClient()!=this.idClient)
		{
			throw new RuntimeException("Non e' possibile inserire in coda un pacchetto su una coda diversa dalla coda corrispondente al suo idClient!");
		}
		
		if ((udpPacket.getIdChannel()<0)||(udpPacket.getIdChannel()>=CHANNELS_NUMBER))
		{
			throw new RuntimeException("Non e' possibile inserire in coda il pacchetto: idChannel fuori dal range ammesso!");
		}
		
		boolean result = this.messageQueues[udpPacket.getIdChannel()].add(udpPacket);
		
		return result;
			
	}
	
	//Metodo non sincronizzato in quanto l'oggetto LinkedBlockingQueue ha gia' al suo interno un meccanismo di sincronizzazione
	public UDPPacket getFromQueue(int idChannel)
	{
		//lastAccess = System.currentTimeMillis();
		
		UDPPacket result = null;
		
		try {
			result = this.messageQueues[idChannel].take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return result;
	}
	
	//Metodo non sincronizzato in quanto l'oggetto LinkedBlockingQueue ha gia' al suo interno un meccanismo di sincronizzazione
	public int getQueueSize(int idChannel)
	{
		//lastAccess = System.currentTimeMillis();
		int result = this.messageQueues[idChannel].size();
		return result;
	}
	
	public void closeClientQueues()
	{
		activeClient = false;
	}
	
	public boolean isActiveClient()
	{
		return activeClient;
	}
	
	/*
	public void run() //Non faccio partire questo thread!!!
	{
		while(activeClient)
		{
			//Se non si e' avuto accesso a nessun metodo del thread per piu' di EXPIRE_TIME_CLIENT il thread viene terminato
			if ((System.currentTimeMillis()-lastAccess)>EXPIRE_TIME_CLIENT)
			{
				System.out.println("ClientQueues idClient:"+ this.idClient + " TERMINATO PER SCADENZA TIMEOUT ACCESSI SUI SUOI METODI!");
				closeClientQueues();
				//TODO: Rimuoverlo dalla hash di UDPPacketQueue
				
			}
			
			//Ripulisce le code dalla 1 in poi dai messaggi piu' vecchi di EXPIRE_TIME_PACKET
			//TODO
			
			
			//Visualizza code per test
//			System.out.println("CODA IDCLIENT:"+this.idClient);
//			for (int i = 0; i < messageQueues.length; i++) {
//			
//				System.out.print(i+":");
//				Iterator<UDPPacket> itr = this.messageQueues[i].iterator(); 
//				while(itr.hasNext())
//				{
//					UDPPacket udpPacket = itr.next();
//					System.out.print("*");
//				} 
//				System.out.println("-");
//			}
			
			//System.out.println("-");
			try {
				sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	*/
		
}
