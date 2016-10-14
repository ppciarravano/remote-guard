package ppc.remoteguard.log;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Classe per gestire l'appender di Log4j in una jTextArea.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class TextAreaAppender extends WriterAppender {
	
	static private JTextArea jTextArea = null;
	
	/** Set the target JTextArea for the logging information to appear. */
	static public void setTextArea(JTextArea jTextArea) {
		TextAreaAppender.jTextArea = jTextArea;
	}
	
	@Override
	/**
	 * Format and then append the loggingEvent to the stored
	 * JTextArea.
	 */
	public void append(LoggingEvent loggingEvent) {
		if (jTextArea!=null)
		{
			final String message = this.layout.format(loggingEvent);
	
			// Append formatted message to textarea using the Swing Thread.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jTextArea.append(message);
				}
			});
		}
	}
}
