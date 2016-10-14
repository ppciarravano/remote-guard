package ppc.remoteguard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ppc.remoteguard.util.SerializeUtility;

/**
 * Classe di gestione dei tipi dei messaggi di comunicazione.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class CommandMessage implements Serializable
{
	//serialVersionUID
    private static final long serialVersionUID = 123456789000000L;
    
    public final static String CONNECT = "CONNECT";
    public final static String WELCOME = "WELCOME";
    
	//ACK acknowledgement (conferma ricevimento messaggio)
    public enum Commands { 
		IAM, ACK,  HELLO, WRONGPW, KILL, VIEW, 
		STOP_VIEW, LIST, ACKLIST, VIEWID, DISCONNECT, 
		CLIENT_NOT_FOUND, ACKVIEW, START_VIEWER, STOP_VIEWER, STOP_VIEWER_CONTROLLER }
	
	public enum ParamsName { PASSWORD, IDCLIENT, GUARDNAME, CLIENT_TYPE, LIST_GUARDS }
	public enum ClientType { GUARD, CONTROLLER }

	private Commands command;
	private HashMap<ParamsName,Object> parameters;
	
	public CommandMessage(Commands c)
	{
		command = c;
		parameters = new HashMap<ParamsName,Object>();
	}
		
	public Commands getCommand()
	{
		return this.command;
	}
	
	public void putParam(ParamsName key, Object val)
	{
		parameters.put(key, val);
	}
	
	public Object getParam(ParamsName key)
	{
		return parameters.get(key);
	}
	
	public void writeCommandMessage(ObjectOutputStream out) throws IOException
	{
		out.writeObject(this);
		out.flush();
	}
	
	public static CommandMessage readCommandMessage(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		return (CommandMessage)in.readObject();
	}
	
	
	public byte[] serialize()
	{
		//return SerializeUtility.serializeObj(this);
		return SerializeUtility.serializeObjZip(this);
	}
	
	public static CommandMessage unserialize(byte[] data)
	{
		//return (CommandMessage)SerializeUtility.unserializeObj(data);
		return (CommandMessage)SerializeUtility.unserializeObjZip(data);
	}
	
	@Override
	public String toString() {
		
		StringBuffer paramString = new StringBuffer();
		paramString.append("{");
		Set entries = parameters.entrySet();
	    Iterator it = entries.iterator();
	    while (it.hasNext()) {
	      Map.Entry entry = (Map.Entry) it.next();
	      paramString.append(entry.getKey() + "=" + entry.getValue());
	      if(it.hasNext())
	    	  paramString.append(";");
	    }
	    paramString.append("}");
	    
		return "CommandMessage [command=" + command + ", parameters="
				+ paramString.toString() + "]";
	}
	
}
