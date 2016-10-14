package ppc.remoteguard.server;

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
 * Classe per la gestione del menu del Server utilizzando java.awt.SystemTray di Java 1.6.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ServerTray {

	private TrayIcon trayIcon;
	private Image image;
		
	public ServerTray() throws IOException
    {
                
        if (SystemTray.isSupported()) {

        	SystemTray tray = SystemTray.getSystemTray();
        	
        	//Leggo l'icona da visualizzare nel system tray
        	InputStream ioStream = ServerTray.class.getResourceAsStream("/ppc/remoteguard/resources/server_icon.gif");
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

            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("EXIT SERVER!");
                    System.exit(0);
                }
            };
            
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
        			lblTxt.append("<font color=blue><b>RemoteGuard</b> (Server)</font><br><br>");
        			
        			lblTxt.append("<b>di Pier Paolo Ciarravano</b><br>");
        			lblTxt.append("<font size=-1>Matr. 773970</font><br><br>");
        			
        			lblTxt.append("<code>Version: "+ConstantManager.VERSION+"<br>");
        			lblTxt.append("UDP Port Server: "+ConstantManager.SERVER_PORT+"<br></code><br>");
        			        			
        			lblTxt.append("</center>");
        			lblTxt.append("</html>");
                	
                	AboutWindow.init("About RemoteGuard (Server)", getImageIcon(), lblTxt.toString());
                }
            };
            
            ActionListener logListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	Logger.log.info("VISUALIZZATORE LOG");
                	LoggerWindow.show();
                }
            };
            
            //Creo menu contestuale
            PopupMenu popup = new PopupMenu();
            
            MenuItem logItem = new MenuItem("Log Viewer");
            logItem.addActionListener(logListener);
            popup.add(logItem);
            
            MenuItem aboutItem = new MenuItem("About");
            aboutItem.addActionListener(aboutListener);
            popup.add(aboutItem);
            
            MenuItem defaultItem = new MenuItem("Shutdown Server");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            

            trayIcon = new TrayIcon(image, "RemoteGuard (Server)", popup);

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
		this.trayIcon.displayMessage("RemoteGuard (Server)", message, messageType);
	}
	
	public Image getImageIcon()
	{
		return this.image;
	}

	

}
