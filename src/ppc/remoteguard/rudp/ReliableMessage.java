package ppc.remoteguard.rudp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.rudp.ReliableSocket;
import net.rudp.ReliableSocketProfile;

/**
 * Classe che implementa ReliableSocket, e crea un nuovo costruttore per il DatagramWrapper.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ReliableMessage extends ReliableSocket {

	private ReliableMessage() throws IOException {
		
	}

	private ReliableMessage(ReliableSocketProfile profile) throws IOException {
		super(profile);
		
	}

	private ReliableMessage(String host, int port) throws UnknownHostException,
			IOException {
		super(host, port);
		
	}

	private ReliableMessage(InetAddress address, int port,
			InetAddress localAddr, int localPort) throws IOException {
		super(address, port, localAddr, localPort);
		
	}

	private ReliableMessage(String host, int port, InetAddress localAddr,
			int localPort) throws IOException {
		super(host, port, localAddr, localPort);
		
	}

	private ReliableMessage(InetSocketAddress inetAddr,
			InetSocketAddress localAddr) throws IOException {
		super(inetAddr, localAddr);
		
	}

	private ReliableMessage(DatagramSocket sock) {
		super(sock);
		
	}

	private ReliableMessage(DatagramSocket sock, ReliableSocketProfile profile) {
		super(sock, profile);
		
	}
	
	public ReliableMessage(DatagramWrapper datagramWrapper) {
		super(datagramWrapper, new ReliableSocketProfile());
		
	}

	public void connect() throws IOException
	{
		//passa a connect un indirizzo qualsiasi, comunque non verra' utilizzato
		// dato che la connessione e' wrappata dalla class UDPMultiplexer
	    connect(new InetSocketAddress("127.0.0.1", 80), 0);
	}
	
}
