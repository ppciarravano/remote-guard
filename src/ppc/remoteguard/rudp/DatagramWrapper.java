package ppc.remoteguard.rudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import ppc.remoteguard.UDPMultiplexer;
import ppc.remoteguard.UDPMultiplexerServer;
import ppc.remoteguard.UDPPacket;
import ppc.remoteguard.log.Logger;

/**
 * Classe per gestire il wrapper dei pacchetti UDPPacket personalizzati
 * sul canale 0 dei messaggi di controllo, attraverso il canale ReliableUDP,
 * implementato con la libreria http://rudp.sourceforge.net/.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class DatagramWrapper extends DatagramSocket 
{
	private UDPMultiplexer udpMultiplexer;
	private int idClient;
    
	public DatagramWrapper(UDPMultiplexer udpMultiplexer, int idClient) throws SocketException 
	{
		this.udpMultiplexer = udpMultiplexer;
		this.idClient = idClient;
	}
	
	
	private DatagramWrapper() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	private DatagramWrapper(DatagramSocketImpl arg0) {
		super(arg0);
		throw new RuntimeException("DatagramWrapper Call Not Implement Constructor!!");
	}

	private DatagramWrapper(SocketAddress arg0) throws SocketException {
		super(arg0);
		throw new RuntimeException("DatagramWrapper Call Not Implement Constructor!!");
	}

	private DatagramWrapper(int arg0) throws SocketException {
		super(arg0);
		throw new RuntimeException("DatagramWrapper Call Not Implement Constructor!!");
	}

	private DatagramWrapper(int arg0, InetAddress arg1) throws SocketException {
		super(arg0, arg1);
		throw new RuntimeException("DatagramWrapper Call Not Implement Constructor!!");
	}

	
	@Override
	public synchronized void receive(DatagramPacket p) throws IOException {
		Logger.log.debug("Call DatagramWrapper.receive");	
		//Leggo un pacchetto dalla coda di messaggi
		UDPPacket udpPacket = udpMultiplexer.getUDPPacketFromQueue(this.idClient, 0);
		//Se il buffer dei p e' piu' piccolo della lunghezza dei dati arrivati rilancio una RuntimeException
		if (p.getLength()<udpPacket.getDataLength())
		{
			Logger.log.error("DatagramWrapper.receive too much datas for buffer!!" );
			throw new RuntimeException("DatagramWrapper.receive too much datas for buffer!!");
		}
		//Setto il buffer di p con i dati di udpPacket
		p.setData(udpPacket.getData());
		p.setLength(udpPacket.getDataLength());		
		
	}

	@Override
	public void send(DatagramPacket p) throws IOException {
		
		//Forma compatta in un unico if del codice di seguito commentato
		if ( (!(this.udpMultiplexer instanceof ppc.remoteguard.UDPMultiplexerServer)) ||
				((this.udpMultiplexer instanceof ppc.remoteguard.UDPMultiplexerServer) &&
					(((UDPMultiplexerServer)this.udpMultiplexer).isClientAddrMapped(this.idClient))	) ) {
			
			Logger.log.debug("Call DatagramWrapper.send");
			//i pachetti rudp sono momentaneamente contrassegnati con idMessage= 1972 (campo perlaltro non utilizzato)
			// solo per visualizzare quali pacchetti sono gestiti dal ReliableUDP 
			UDPPacket udpPacket = new UDPPacket(this.idClient, 0, 1972, p.getData(), 0, p.getLength());
			this.udpMultiplexer.sendUDPPacket(udpPacket);
			
		}
		
		/*	
		if (this.udpMultiplexer instanceof ppc.remoteguard.UDPMultiplexerServer)
		{
			if (((UDPMultiplexerServer)this.udpMultiplexer).isClientAddrMapped(this.idClient))
			{
				System.out.println("Call DatagramWrapper.send");
				UDPPacket udpPacket = new UDPPacket(this.idClient, 0, 0, p.getData(), 0, p.getLength());
				this.udpMultiplexer.sendUDPPacket(udpPacket);
			}
		}
		else
		{
			System.out.println("Call DatagramWrapper.send");
			UDPPacket udpPacket = new UDPPacket(this.idClient, 0, 0, p.getData(), 0, p.getLength());
			this.udpMultiplexer.sendUDPPacket(udpPacket);
		}
		*/
		
	}
		
	
	@Override
	public synchronized void bind(SocketAddress addr) throws SocketException {
		Logger.log.debug("Call DatagramWrapper.bind .... Do nothing, not implemented method!!");		
	}

	@Override
	public void close() {
		Logger.log.debug("Call DatagramWrapper.close .... Do nothing, not implemented method!!");	
		
	}

	@Override
	public void connect(InetAddress address, int port) {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
		
	}

	@Override
	public void connect(SocketAddress addr) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public void disconnect() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized boolean getBroadcast() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
		
	}

	@Override
	public DatagramChannel getChannel() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
		
	}

	@Override
	public InetAddress getInetAddress() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
		
	}

	@Override
	public InetAddress getLocalAddress() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public int getLocalPort() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public int getPort() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized boolean getReuseAddress() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized int getSendBufferSize() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized int getSoTimeout() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized int getTrafficClass() throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public boolean isBound() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public boolean isClosed() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public boolean isConnected() {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized void setBroadcast(boolean on) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized void setReceiveBufferSize(int size) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized void setReuseAddress(boolean on) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized void setSendBufferSize(int size) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	@Override
	public synchronized void setTrafficClass(int tc) throws SocketException {
		throw new RuntimeException("DatagramWrapper Call Not Implement Method!!");
	}

	
}
