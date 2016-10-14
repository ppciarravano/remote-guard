package ppc.remoteguard.guard;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import ppc.remoteguard.AboutWindow;
import ppc.remoteguard.log.Logger;
import ppc.remoteguard.log.LoggerWindow;
import ppc.remoteguard.util.ConstantManager;

/**
 * Classe per la gestione del menu della Guard utilizzando java.awt.SystemTray di Java 1.6.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class GuardTray {

	private Guard guard;
	private GuardTray thisObj;
	
	private TrayIcon trayIcon;
	private Image image;
	
	private MenuItem connectItem;
	private MenuItem disconnectItem;
		
	public GuardTray(Guard guard) throws IOException
    {
        this.guard = guard;
        this.thisObj = this;
        
        if (SystemTray.isSupported()) {

        	SystemTray tray = SystemTray.getSystemTray();
        	
        	//Leggo l'icona da visualizzare nel system tray
        	InputStream ioStream = GuardTray.class.getResourceAsStream("/ppc/remoteguard/resources/guard_icon.gif");
        	this.image = ImageIO.read(ioStream);
			  
        	/*
            MouseListener mouseListener = new MouseListener() {
                
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse clicked!");                 
                }
                public void mouseEntered(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse entered!");                 
                }
                public void mouseExited(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse exited!");                 
                }
                public void mousePressed(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse pressed!");                 
                }
                public void mouseReleased(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse released!");                 
                }

            };
            */

            ActionListener aboutListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("ABOUT");
                	
                	//Creo il testo per la finestra about
        			StringBuffer lblTxt = new StringBuffer();
        			//lblTxt.append("<html><font color=blue><b></b></font><br><b></u></b><br>" +
        			//		"<code><font background-color=yellow>&nbsp;&nbsp;<br>&nbsp;&nbsp;test" +
        			//		"</font></code></html>");
        			lblTxt.append("<html>");
        			lblTxt.append("<center>");
        			lblTxt.append("<b>\"La Sapienza\"<br>Universit&agrave; di Roma</b><br><br>");
        			
        			lblTxt.append("Facolt&agrave; di Ingegneria<br>");
        			lblTxt.append("Corso di Laurea in Ingegneria Informatica<br>");
        			lblTxt.append("<font size=-1>Anno Accedemico 2008-2009</font><br><br>");
        			
        			lblTxt.append("<u>Corso di Progetto di Reti di Calcolatori<br> e Sistemi Informatici</u><br>");
        			lblTxt.append("<font size=-1>Prof. Stefano Millozzi</font><br><br>");
        			
        			lblTxt.append("Sistema di Video/Audio Sorveglianza Remota<br>");
        			lblTxt.append("<font color=blue><b>RemoteGuard</b> (Guard)</font><br><br>");
        			
        			lblTxt.append("<b>di Pier Paolo Ciarravano</b><br>");
        			lblTxt.append("<font size=-1>Matr. 773970</font><br><br>");
        			
        			lblTxt.append("<code>Version: "+ConstantManager.VERSION+"</code><br>");
        			
        			lblTxt.append("</center>");
        			lblTxt.append("</html>");
                	
                	AboutWindow.init("About RemoteGuard (Guard)", getImageIcon(), lblTxt.toString());
                }
            };
            
            ActionListener logListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("VISUALIZZATORE LOG");
                	LoggerWindow.show();
                }
            };
            
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("EXIT GUARD!");
                	
                	disconnect();
                    System.exit(0);
                }
            };
            
            ActionListener disconnectListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("DISCONNECT");
                	
                	disconnect();               
                	
                }
            };
            
            ActionListener connectListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("CONNECT");
                	
                	setEnableConnect(false);
                	ConfigureWindow cc = new ConfigureWindow(thisObj);
            		
                }
            };
            
            //Creo menu contestuale
            PopupMenu popup = new PopupMenu();
            
            MenuItem logItem = new MenuItem("Log Viewer");
            logItem.addActionListener(logListener);
            popup.add(logItem);
            
            this.connectItem = new MenuItem("Connect");
            connectItem.addActionListener(connectListener);
            popup.add(connectItem);
            
            this.disconnectItem = new MenuItem("Disconnect");
            disconnectItem.addActionListener(disconnectListener);
            popup.add(disconnectItem);
            setEnableDisconnect(false);
            
            MenuItem aboutItem = new MenuItem("About");
            aboutItem.addActionListener(aboutListener);
            popup.add(aboutItem);
            
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(exitListener);
            popup.add(exitItem);
            

            trayIcon = new TrayIcon(image, "RemoteGuard (Guard)", popup);

            /*
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    trayIcon.displayMessage("Action Event", 
                        "An Action Event Has Been Peformed!",
                        TrayIcon.MessageType.INFO);
                    System.out.println("***");  
                }
            };
            */
            
            trayIcon.setImageAutoSize(true);
            //trayIcon.addActionListener(actionListener);
            //trayIcon.addMouseListener(mouseListener);

                try {
                      tray.add(trayIcon);
                } catch (AWTException e) {
                    System.err.println("TrayIcon could not be added.");
                }

        } else {
        	Logger.log.error("System tray is currently not supported.");
        }
    }
	
	public void displayMessageINFO(String message)
	{
		this.displayMessage(message, TrayIcon.MessageType.INFO);
	}
	
	public void displayMessageERROR(String message)
	{
		this.displayMessage(message, TrayIcon.MessageType.ERROR);
	}
	
	public void displayMessageWARNING(String message)
	{
		this.displayMessage(message, TrayIcon.MessageType.WARNING);
	}
	
	private void displayMessage(String message, TrayIcon.MessageType messageType)
	{
		this.trayIcon.displayMessage("RemoteGuard (Guard)", message, messageType);
	}
	
	public Image getImageIcon()
	{
		return this.image;
	}

	public void connect(String hostName, int udpPortNumber, String password, String guardName)
	{
		enableDisconnection();
		guard.connect(hostName, udpPortNumber, password, guardName);
	}
	
	public void disconnect()
	{
		enableConnection();
		guard.disconnect();
	}
	
	public void enableConnection()
	{
		setEnableConnect(true);
		setEnableDisconnect(false);
	}
	
	public void enableDisconnection()
	{
		setEnableConnect(false);
		setEnableDisconnect(true);
	}
	
	public void setEnableConnect(boolean val)
	{
		this.connectItem.setEnabled(val);
	}
	
	public void setEnableDisconnect(boolean val)
	{
		this.disconnectItem.setEnabled(val);
	}
	

}
