package ppc.remoteguard.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Classe per gestire la visualizzazione del log di Log4j all'interno di un JFrame.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class LoggerWindow
{
	private LoggerWindow(){};
	
	private static JFrame jFrame = null; 
	
	public static void show()
	{
		if(jFrame!=null)
		{
			jFrame.setVisible(true);
		}
	}
	
	public static void hide()
	{
		if(jFrame!=null)
		{
			jFrame.setVisible(false);
		}
	}
	
	private LoggerWindow(String title, Image image)
	{
		if(jFrame==null)
		{
			
			//Creo JFrame
			JFrame jFrameTemp = new JFrame();
			jFrameTemp.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			//jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrameTemp.setSize(800, 450);
			jFrameTemp.setLocation(100,100);
			jFrameTemp.setTitle(title);
			jFrameTemp.setIconImage(image);
			
			//Creo Layout
			JPanel jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			
			//Creo e aggiungo pulsante chiusura al JPanel
			JButton closeButton = new JButton();
			closeButton.setText("Chiudi");
			//closeButton.setSize(200, 30);
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					hide();
				}
			});
			//Per prevenire il resizing del JButton nel BorderLayout
			JPanel panelButton = new JPanel();
			panelButton.setLayout(new FlowLayout());
			panelButton.add(closeButton);
			jContentPane.add(panelButton, BorderLayout.SOUTH);
			//jContentPane.add(closeButton, BorderLayout.SOUTH);
			
			//Creo JTextArea
			JTextArea textArea = new JTextArea();
			textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
			textArea.setText("");
			textArea.setBorder(BorderFactory.createLineBorder(Color.BLUE));
			
			//setto textArea come appender per log4j
			TextAreaAppender.setTextArea(textArea);
			
			//creo uno JScrollPane con dentro la textArea e lo aggiungo
			JScrollPane jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(textArea);
			jContentPane.add(jScrollPane, BorderLayout.CENTER);
			
			//Aggiungo jContentPane al JFrame
			jFrameTemp.setContentPane(jContentPane);
			
			//Visualizzo il JFrame
			//jFrame.setVisible(true);
			
			jFrame = jFrameTemp;
		}
	}
	
	public static void init(final String title, final Image image)
	{
		/*
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				LoggerWindow loggerWindow = new LoggerWindow(title, image);
			}
		});
		*/
		LoggerWindow loggerWindow = new LoggerWindow(title, image);
	}
	
	
	
}
