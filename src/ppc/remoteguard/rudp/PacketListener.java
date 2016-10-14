package ppc.remoteguard.rudp;

import net.rudp.ReliableSocketListener;

/**
 * Classe che implementa ReliableSocketListener.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class PacketListener implements ReliableSocketListener {

	
	public void packetReceivedInOrder() {
		//System.out.println("packetReceivedInOrder");

	}

	
	public void packetReceivedOutOfOrder() {
		//System.out.println("packetReceivedOutOfOrder");

	}

	
	public void packetRetransmitted() {
		//System.out.println("packetRetransmitted");

	}

	
	public void packetSent() {
		//System.out.println("packetSent");

	}

}
