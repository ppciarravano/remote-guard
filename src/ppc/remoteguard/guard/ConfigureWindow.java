package ppc.remoteguard.guard;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ppc.remoteguard.log.Logger;
import ppc.remoteguard.util.ConstantManager;

/**
 * Classe per creare e gestire la GUI di configurazione dei parametri di connessione della Guard.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ConfigureWindow
{
	private GuardTray guardTray;
	private JFrame jFrame = null; 
	
	private JTextField hostNameTx;
	private JTextField udpPortNumberTx;
	private JPasswordField passwordTx;
	private JTextField guardNameTx;
	
	private static String validHostName;
	private static int validUdpPortNumber;
	private static String validPassword;
	private static String validGuardName;
	
	static
	{
		validHostName = ConstantManager.SERVER_HOST;
		validUdpPortNumber = ConstantManager.SERVER_PORT;
		validPassword = ConstantManager.SERVER_PASSWORD_GUARD;
		validGuardName = "";
	}
		
	public ConfigureWindow(GuardTray guardTray)
	{
		this.guardTray = guardTray;
		init(guardTray.getImageIcon());
	}
	
	/*
	 * Crea la finestra di input dei parametri di configurazione
	 */
	private void init(Image image) 
	{
		if(jFrame==null)
		{
			
			
			//Creo JFrame
			JFrame jFrameTemp = new JFrame();
			jFrameTemp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jFrameTemp.setSize(350, 300);
			jFrameTemp.setLocation(300,50);
			jFrameTemp.setTitle("RemoteGuard (Guard) Connection configuration");
			jFrameTemp.setIconImage(image);
			jFrameTemp.setResizable(false);
			
			//Creo Layout
			JPanel jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
						
			//Costruisco i l'interfaccia
			
			//Host name
			JLabel hostNameLb=new JLabel("Host: ", JLabel.LEFT);
			hostNameTx=new JTextField(15);
			hostNameTx.setText(validHostName);
			hostNameTx.setMaximumSize(hostNameTx.getPreferredSize());
			Box obox1=Box.createHorizontalBox();
			obox1.add(hostNameLb);
			obox1.add(hostNameTx);
			
			//udp port number
			JLabel udpPortNumberLb=new JLabel("UDP Port Number: ", JLabel.LEFT);
			udpPortNumberTx=new JTextField(5);
			udpPortNumberTx.setText(String.valueOf(validUdpPortNumber));
			udpPortNumberTx.setMaximumSize(udpPortNumberTx.getPreferredSize());
			Box obox2=Box.createHorizontalBox();
			obox2.add(udpPortNumberLb);
			obox2.add(udpPortNumberTx);
			
			//password
			JLabel passwordLb=new JLabel("Password: ", JLabel.LEFT);
			passwordTx = new JPasswordField(validPassword, 10);
			passwordTx.setEchoChar('*');
			passwordTx.setMaximumSize(passwordTx.getPreferredSize());
			Box obox3=Box.createHorizontalBox();
			obox3.add(passwordLb);
			obox3.add(passwordTx);
			
			//guard name
			JLabel guardNameLb=new JLabel("Nome Guard: ", JLabel.LEFT);
			guardNameTx=new JTextField(10);
			guardNameTx.setText(validGuardName);
			guardNameTx.setMaximumSize(guardNameTx.getPreferredSize());
			Box obox4=Box.createHorizontalBox();
			obox4.add(guardNameLb);
			obox4.add(guardNameTx);
			
			
			//Pulsanti
			JButton annullaBtn = new JButton("Annulla");
			annullaBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Logger.log.info("Configuration: Annulla button pressed!");
					jFrame.dispose();
					guardTray.setEnableConnect(true);
				}
			});
			JButton connettiBtn = new JButton("Connetti");
			connettiBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Logger.log.info("Configuration: Connetti button pressed!");
					
					if (validate())
					{
						jFrame.dispose();
						guardTray.setEnableConnect(true);
						guardTray.connect(validHostName, validUdpPortNumber, validPassword, validGuardName);
					}
					else
					{
						Logger.log.info("Correggere Parametri in input nella finestra di configurazione");
					}
					
				}
			});
			Box oboxButtons=Box.createHorizontalBox();
			oboxButtons.add(annullaBtn);
			oboxButtons.add(Box.createHorizontalStrut(10));
			oboxButtons.add(connettiBtn);
			
			
			//Creo un Box verticale che racchiude i box orizzontali
			Box vbox=Box.createVerticalBox();
			vbox.add(Box.createVerticalStrut(20));	
			vbox.add(obox1);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(obox2);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(obox3);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(obox4);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(oboxButtons);
			vbox.add(Box.createVerticalStrut(10));
			
			//Aggiungo il vbox al pannello
			jContentPane.add(vbox, BorderLayout.CENTER);
						
			//Aggiungo jContentPane al JFrame
			jFrameTemp.setContentPane(jContentPane);
			
			//Visualizzo il JFrame
			jFrameTemp.setVisible(true);
			
			jFrame = jFrameTemp;
					
			
		}
		
		
	}
	
	private boolean validate()
	{
		//Controllo e valido i parametri
		
		if ((hostNameTx.getText().trim().equals(""))||(hostNameTx.getText().trim().length()>100))
		{
			JOptionPane.showMessageDialog(jFrame, "Inserire un valido nome di host o indirizzo IP!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
			return false;
		}
		validHostName = hostNameTx.getText().trim();
		
		try
		{
			int udpPortNumber = Integer.parseInt(udpPortNumberTx.getText().trim());
			if (udpPortNumber<=1024)
			{
				JOptionPane.showMessageDialog(jFrame, "Inserire un valore maggiore di 1024 per il numero di porta udp!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
				return false;
			}
			validUdpPortNumber = udpPortNumber;
		}
		catch (java.lang.NumberFormatException nfe)
		{
			JOptionPane.showMessageDialog(jFrame, "Inserire un valore numerico valido per il numero di porta udp!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
			return false;
		}
				
		String password = new String(passwordTx.getPassword());
		if ((password.trim().equals(""))||(password.trim().length()>20))
		{
			JOptionPane.showMessageDialog(jFrame, "Inserire una password valida!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
			return false;
		}
		validPassword = password;
		
		if ((guardNameTx.getText().trim().equals(""))||(guardNameTx.getText().trim().length()>20))
		{
			JOptionPane.showMessageDialog(jFrame, "Inserire un valido nome Guard!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
			return false;
		}
		validGuardName = guardNameTx.getText().trim();

		return true;
	}
		
	

}
