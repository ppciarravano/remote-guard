package ppc.remoteguard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Classe per la gestione del pacchetto UDP che andra' ad incapsulare i pacchetti udp
 * per il canale di controllo e per la comunicazione RTP.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class UDPPacket
{
	private int idClient;
	private int idChannel;
	private long timeArrive;
	 
	//Campo che non e' stato piu' utilizzato per ordinare i pacchetti, 
	//rimane inutilizzato dall'applicazione e senza un particolare significato
	private int idMessage;
	
	private byte[] data;
	private int dataLength;
	
		
	public UDPPacket(int idClient, int idChannel, int idMessage, byte[] dataInput)
	{
		this.idClient = idClient;
		this.idChannel = idChannel;
		this.idMessage = idMessage;
		this.data = dataInput;
		this.dataLength = dataInput.length;
		
		this.timeArrive = System.currentTimeMillis();
		
	}
	
	public UDPPacket(int idClient, int idChannel, int idMessage, byte dataInput[], int offset, int len)
	{
		this(idClient, idChannel, idMessage, dataInput);
		this.data = Arrays.copyOfRange(dataInput, offset, offset+len);
		this.dataLength = len;
	}
	
	public UDPPacket(byte[] dataOfObject)  throws IOException
	{
		this(dataOfObject, 0, dataOfObject.length);
	}
	
	public UDPPacket(byte[] dataOfObject, int length)  throws IOException
	{
		this(dataOfObject, 0, length);
	}
	
	public UDPPacket(byte[] dataOfObject, int offset, int length) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(dataOfObject, offset, length);
		DataInputStream dis = new DataInputStream(bais);
		//dis.reset();
		this.idClient = dis.readInt();
		this.idChannel = dis.readInt();
		this.idMessage = dis.readInt();
		this.dataLength = dis.readInt();
		this.data = new byte[this.dataLength];
		dis.read(this.data, 0, this.dataLength);
		dis.close(); //inutili
		bais.close(); //inutili
		
		this.timeArrive = System.currentTimeMillis();
		
	}
	
	
	public byte[] getUDPPacket() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.idClient);
        dos.writeInt(this.idChannel);
        dos.writeInt(this.idMessage);
        dos.writeInt(this.dataLength);
        dos.write(this.data, 0, this.dataLength); 
        dos.flush();
        dos.close();
        return baos.toByteArray();
        
	}

	public int getIdClient() {
		return idClient;
	}
	
	public void setIdClient(int value) {
		this.idClient=value;
	}

	public int getIdChannel() {
		return idChannel;
	}

	public long getTimeArrive() {
		return timeArrive;
	}

	public int getIdMessage() {
		return idMessage;
	}

	public byte[] getData() {
		return data;
	}
	
	public int getDataLength() {
		return dataLength;
	}
	
	public String toString() {
		return "UDPPacket [idClient=" + idClient + ", idChannel=" + idChannel
				+ ", idMessage=" + idMessage + ", timeArrive=" + timeArrive
				+ ", dataLength="
				+ dataLength + "]";
	}
	
	
}
