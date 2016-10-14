package ppc.remoteguard.controller;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ppc.remoteguard.log.Logger;

/**
 * Classe per creare e gestire la GUI di scelta dalla guard da visualizzare.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class ChoiceGuardWindow
{
	private JFrame jFrame = null; 
	
	private Hashtable<Integer, String> listGuards;
	private Controller controller;
	
	private JComboBox comboGuards = null;
		
	public ChoiceGuardWindow(Hashtable<Integer, String> listGuards, Controller controller, Image image)
	{
		this.listGuards = listGuards;
		this.controller = controller;
		
		init(image);
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
			jFrameTemp.setSize(350, 200);
			jFrameTemp.setLocation(300,300);
			jFrameTemp.setTitle("RemoteGuard (Controller) Scelta Guard");
			jFrameTemp.setIconImage(image);
			jFrameTemp.setResizable(false);
			
			//Creo Layout
			JPanel jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
						
			//Costruisco l'interfaccia
			
			//Combo box per scelta guard
			JLabel hostNameLb=new JLabel("Scelta Guard: ", JLabel.LEFT);
			comboGuards = new JComboBox();
			comboGuards.addItem(new IdClientNameGuard(-1, "Selezionare..."));
			
			Enumeration<Integer> ids = listGuards.keys();
			while(ids.hasMoreElements())
			{
				Integer idClientInHash = ids.nextElement();
				String guardNameInHash = listGuards.get(idClientInHash);
				Logger.log.info("Guard Attiva: idClient:"+idClientInHash+" nameGuard:"+guardNameInHash);
				IdClientNameGuard idName = new IdClientNameGuard(idClientInHash, guardNameInHash);
				comboGuards.addItem(idName);
			}
			comboGuards.setMaximumSize(comboGuards.getPreferredSize());
			Box obox1=Box.createHorizontalBox();
			obox1.add(hostNameLb);
			obox1.add(comboGuards);
			
			//Pulsanti
			JButton annullaBtn = new JButton("Annulla");
			annullaBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Logger.log.info("Configuration: Annulla button pressed!");
					controller.getControllerTray().enableExitViewGuard();
					jFrame.dispose();
					
				}
			});
			JButton visualizzaBtn = new JButton("Visualizza");
			visualizzaBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Logger.log.info("Visualizza Guard button pressed!");
					
					if (validate())
					{
						jFrame.dispose();
						IdClientNameGuard idName = (IdClientNameGuard)comboGuards.getSelectedItem();
						Logger.log.info("Scelta Guard: idClient:"+idName.idClient+" nameGuard:"+idName.nameGuard);
						//Invio la richiesta al server che poi la inoltrera' alla guard
						controller.sendViewRequest(idName.idClient);
					}
					else
					{
						Logger.log.info("Scegliere una guard o premere annulla!");
					}
					
				}
			});
			Box oboxButtons=Box.createHorizontalBox();
			oboxButtons.add(annullaBtn);
			oboxButtons.add(Box.createHorizontalStrut(10));
			oboxButtons.add(visualizzaBtn);
			
			
			//Creo un Box verticale che racchiude i box orizzontali
			Box vbox=Box.createVerticalBox();
			vbox.add(Box.createVerticalStrut(20));	
			vbox.add(obox1);
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
		IdClientNameGuard idName = (IdClientNameGuard)comboGuards.getSelectedItem();
		if (idName.idClient == -1)
		{
			JOptionPane.showMessageDialog(jFrame, "Selezionare una guard nella lista!", "Attenzione!!", JOptionPane.ERROR_MESSAGE); 
			return false;
		}
		
		return true;
	}
		
	
	class IdClientNameGuard
	{
		int idClient;
		String nameGuard;
		
		IdClientNameGuard(int idClient, String nameGuard)
		{
			this.idClient = idClient;
			this.nameGuard = nameGuard;
		}

		public String toString()
		{
			return this.nameGuard;
		}
		
		
	}

}
