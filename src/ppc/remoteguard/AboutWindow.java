package ppc.remoteguard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Classe per visualizzazione finestra di about.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class AboutWindow
{
	private AboutWindow(){};
	
	private JFrame jFrame = null; 
	
		
	private AboutWindow(String title, Image image, String lblTxt)
	{
		if(jFrame==null)
		{
			
			//Creo JFrame
			JFrame jFrameTemp = new JFrame();
			jFrameTemp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			//jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrameTemp.setSize(450, 600);
			jFrameTemp.setLocation(200,50);
			jFrameTemp.setTitle(title);
			jFrameTemp.setIconImage(image);
			jFrameTemp.setResizable(false);
			
			//Creo Layout
			JPanel jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			
			//Creo e aggiungo pulsante chiusura al JPanel
			JButton closeButton = new JButton();
			closeButton.setText("Chiudi");
			//closeButton.setSize(200, 30);
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jFrame.dispose();
				}
			});
			//Per prevenire il resizing del JButton nel BorderLayout
			JPanel panelButton = new JPanel();
			panelButton.setLayout(new FlowLayout());
			panelButton.add(closeButton);
			jContentPane.add(panelButton, BorderLayout.SOUTH);
			//jContentPane.add(closeButton, BorderLayout.SOUTH);
			
			//Creo JLabel e aggiungo al JPanel
			JLabel jLabel = new JLabel();
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			//jLabel.setVerticalAlignment(SwingConstants.CENTER);
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
			jLabel.setText(lblTxt);
			jLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			jContentPane.add(jLabel, BorderLayout.CENTER);
			
			//Aggiungo jContentPane al JFrame
			jFrameTemp.setContentPane(jContentPane);
			
			//Visualizzo il JFrame
			jFrameTemp.setVisible(true);
			
			jFrame = jFrameTemp;
		}
	}
	
	public static void init(final String title, final Image image, final String lblTxt)
	{
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AboutWindow aboutWindow = new AboutWindow(title, image, lblTxt);
			}
		});
		
	}
	
	
	
}
