package ppc.remoteguard.rudp;

import net.rudp.ReliableSocket;
import net.rudp.ReliableSocketStateListener;

/**
 * Classe che implementa ReliableSocketStateListener.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class StateListener implements ReliableSocketStateListener {

	
	public void connectionClosed(ReliableSocket sock) {
		//System.out.println("connectionClosed");

	}

	
	public void connectionFailure(ReliableSocket sock) {
		//System.out.println("connectionFailure");

	}

	
	public void connectionOpened(ReliableSocket sock) {
		//System.out.println("connectionOpened");

	}

	
	public void connectionRefused(ReliableSocket sock) {
		//System.out.println("connectionRefused");

	}

	
	public void connectionReset(ReliableSocket sock) {
		//System.out.println("connectionReset");

	}

}
