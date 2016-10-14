package ppc.remoteguard;

import java.util.Hashtable;

/**
 * Classe per la gestione delle code dei messaggi in arrivo per tutti i clients,
 * attraverso una Hashtable di oggetti ClientQueues. L'Hashtable e' referenziata tramite l'idClient,
 * e ogni elemento contiene il gestore ClientQueues delle code dei messaggi provenienti da quel certo client.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class UDPPacketQueue
{
	private Hashtable<Integer, ClientQueues> clientQueuesMap;
	
	public UDPPacketQueue()
	{
		clientQueuesMap = new Hashtable<Integer, ClientQueues>();
	}
		
	//Aggiungo il pacchetto nella coda corrispondente al suo client, se non c'e' la creo
	public boolean putUDPPacket(UDPPacket udpPacket)
	{
		boolean result = false;
		//Controllo se per questo client ho gia' inserito una entry in clientQueuesMap,
        // altrimenti la creo 
        if (clientQueuesMap.containsKey(new Integer(udpPacket.getIdClient())))
        {
        	ClientQueues cq = clientQueuesMap.get(udpPacket.getIdClient());
        	result = cq.putInQueue(udpPacket);
        }
        else
        {
        	//E' la prima volta che ricevo un pacchetto da questo idClient
        	// creo una entry in clientQueuesMap e ci aggiungo il pacchetto
    		ClientQueues cq = new ClientQueues(udpPacket.getIdClient());
    		clientQueuesMap.put(new Integer(udpPacket.getIdClient()), cq);
    		result = cq.putInQueue(udpPacket);
        }
        return result;
	}
	
	public UDPPacket getUDPPacket(int idClient, int idChannel)
	{
		//System.out.println("getUDPPacket: idClient:"+idClient+" idChannel:"+idChannel);
		UDPPacket result = null;
		
		//Se non esiste creo un ClientQueque in Hashtable, in modo che l'interrogazione cq.getFromQueue sia bloccante fino a che un pacchetto non sia presente 
		if (!clientQueuesMap.containsKey(new Integer(idClient)))
		{
			ClientQueues cq = new ClientQueues(idClient);
			clientQueuesMap.put(new Integer(idClient), cq);
		}
		ClientQueues cq = clientQueuesMap.get(new Integer(idClient));
    	result = cq.getFromQueue(idChannel);
		
    	/*
		if (clientQueuesMap.containsKey(new Integer(idClient)))
        {
        	ClientQueues cq = clientQueuesMap.get(new Integer(idClient));
        	result = cq.getFromQueue(idChannel);
        }
		else
		{
			throw new RuntimeException("idClient non presente nella coda! (errore da commentare)");
		}
		*/
			
		return result;
	}
	
	public int getQueueSize(int idClient, int idChannel)
	{
		//System.out.println("getUDPPacket: idClient:"+idClient+" idChannel:"+idChannel);
		int result = 0;
		if (clientQueuesMap.containsKey(new Integer(idClient)))
        {
        	ClientQueues cq = clientQueuesMap.get(new Integer(idClient));
        	result = cq.getQueueSize(idChannel);
        }
		return result;
	}
	
	
}
