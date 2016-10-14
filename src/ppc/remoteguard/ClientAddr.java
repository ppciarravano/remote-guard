package ppc.remoteguard;

import java.net.InetAddress;

/**
 * Classe per memorizzare terna di idclient, host e porta.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ClientAddr
{
	private int idClient = 0;
	private InetAddress clientIPAddress;
	private int clientPort;
	
	public ClientAddr(int idClient, InetAddress clientIPAddress, int clientPort)
	{
		this.idClient = idClient;
		this.clientIPAddress = clientIPAddress;
		this.clientPort = clientPort;
	}

	public int getIdClient() {
		return idClient;
	}

	public InetAddress getClientIPAddress() {
		return clientIPAddress;
	}

	public int getClientPort() {
		return clientPort;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientIPAddress == null) ? 0 : clientIPAddress.hashCode());
		result = prime * result + clientPort;
		result = prime * result + idClient;
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientAddr other = (ClientAddr) obj;
		if (clientIPAddress == null) {
			if (other.clientIPAddress != null)
				return false;
		} else if (!clientIPAddress.equals(other.clientIPAddress))
			return false;
		if (clientPort != other.clientPort)
			return false;
		if (idClient != other.idClient)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClientAddr [clientIPAddress=" + clientIPAddress
				+ ", clientPort=" + clientPort + ", idClient=" + idClient + "]";
	}
}
