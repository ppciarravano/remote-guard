package ppc.remoteguard.guard;

import java.awt.Dimension;
import java.io.IOException;

import javax.media.Codec;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;

import ppc.remoteguard.RTPMultiplexerConnector;
import ppc.remoteguard.UDPMultiplexer;
import ppc.remoteguard.log.Logger;
import ppc.remoteguard.util.ConstantManager;
import ppc.remoteguard.util.Utility;

/**
 * Classe RTPVideoAudioTransmitter per inviare gli stream RTP utilizzando RTPMultiplexerConnector.
 * 
 * <br><br>License: 	GNU General Public License<br>
 * 
 * @author  	Pier Paolo Ciarravano  
 * @version  	Vers. 0.98 (29/09/2009) 
 */
public class RTPVideoAudioTransmitter
{
	private UDPMultiplexer client;
	private int idClient;
	
	private Processor processor = null;
	private RTPManager rtpMgrs[];
	private DataSource dataOutput = null;
	
	public RTPVideoAudioTransmitter(UDPMultiplexer client, int idClient)
	{
		this.client = client;
		this.idClient = idClient;
	}

	/**
	 * Starts the transmission. Returns null if transmission started ok.
	 * Otherwise it returns a string with the reason why the setup failed.
	 */
	public synchronized boolean start()
	{
		boolean result;

		// Create a processor for the specified media locator
		result = createProcessor();
		if (result == false)
			return false;

		// Create an RTP session to transmit the output of the
		// processor to the specified IP address and port no.
		result = createTransmitter();
		if (result == false)
		{
			processor.close();
			processor = null;
			return false;
		}

		// Start the transmission
		processor.start();

		return true;
	}

	/**
	 * Stops the transmission if already started
	 */
	public void stop()
	{
		synchronized (this)
		{
			if (processor != null)
			{
				processor.stop();
				processor.close();
				processor = null;
				for (int i = 0; i < rtpMgrs.length; i++)
				{
					rtpMgrs[i].removeTargets("Session ended.");
					rtpMgrs[i].dispose();
				}
			}
		}
	}

	private boolean createProcessor()
	{
		DataSource dataSources[] = new DataSource[2];
		DataSource mergedDataSource = null;

		MediaLocator video = new MediaLocator(ConstantManager.VIDEO_MEDIA_LOCATOR);
		try
		{
			dataSources[0] = javax.media.Manager.createDataSource(video);
		}
		catch (Exception e)
		{
			Logger.log.error("Couldn't create DataSource Video");
			return false;
		}

		MediaLocator audio = new MediaLocator(ConstantManager.AUDIO_MEDIA_LOCATOR);
		try
		{
			dataSources[1] = javax.media.Manager.createDataSource(audio);
		}
		catch (Exception e)
		{
			Logger.log.error("Couldn't create DataSource Audio");
			return false;
		}

		try
		{
			mergedDataSource = javax.media.Manager.createMergingDataSource(dataSources);
		}
		catch (IncompatibleSourceException ise)
		{
			Logger.log.error("createMergingDataSource return IncompatibleSourceException:" + Utility.exceptionToString(ise));
			return false;
		}
		
		try
		{
			processor = javax.media.Manager.createProcessor(mergedDataSource);
		}
		catch (NoProcessorException npe)
		{
			Logger.log.error("Couldn't create processor");
			return false;
		}
		catch (IOException ioe)
		{
			Logger.log.error("IOException creating processor");
			return false;
		}

		//Wait for it to configure
		boolean waitForStateResult = waitForState(processor, Processor.Configured);
		if (waitForStateResult == false)
		{
			Logger.log.error("Couldn't configure processor");
			return false;
		}
		
		//Get the tracks from the processor
		TrackControl[] tracks = processor.getTrackControls();

		//Controllo se ho tutte le tracce
		if ((tracks == null) || (tracks.length != 2))
		{
			Logger.log.error("Nel Processor non ci sono tutte le due tracce audio/video!");
			return false;
		}
		else
		{
			Logger.log.info("Processor tracce totali: " + tracks.length);
		}
				
		//Set the output content descriptor to RAW_RTP
		//This will limit the supported formats reported from
		//Track.getSupportedFormats to only valid RTP formats.
		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		Format supported[];
		Format chosen;
		int countTrack = 0;

		//Program the tracks.
		for (int trackId = 0; trackId < tracks.length; trackId++)
		{
			//Format format = tracks[trackId].getFormat();
			if (tracks[trackId].isEnabled())
			{
				supported = tracks[trackId].getSupportedFormats();
				//Dump formati supportati per questa traccia
				Logger.log.info("Traccia " + trackId + ": " + tracks[trackId].getFormat());
				for (int i = 0; i < supported.length; i++)
				{
					Logger.log.info("Formato ["+i+"] supportato:"+supported[i]);
				}
				
				//Setto i formati audio video tra quelli supportati				
				int formatIndex = 0;
				if (trackId==0)
				{
					//Video Track
					if (ConstantManager.VIDEO_FORMAT < supported.length)
					{
						formatIndex = ConstantManager.VIDEO_FORMAT;
					}
					else
					{
						Logger.log.info("VIDEO_FORMAT formatIndex:" +ConstantManager.VIDEO_FORMAT+ " is not supported!");
					}
					Logger.log.info("VIDEO_FORMAT set to formatIndex:"+formatIndex);
				}
				else if (trackId==1)
				{
					//Audio Track
					if (ConstantManager.AUDIO_FORMAT < supported.length)
					{
						formatIndex = ConstantManager.AUDIO_FORMAT;
					}
					else
					{
						Logger.log.info("AUDIO_FORMAT formatIndex:" +ConstantManager.AUDIO_FORMAT+ " is not supported!");
					}
					Logger.log.info("AUDIO_FORMAT set to formatIndex:"+formatIndex);
				}				
												
				if (supported.length > 0)
				{
					if (supported[formatIndex] instanceof VideoFormat)
					{
						// For video formats, we should double check the
						// sizes since not all formats work in all sizes.
						chosen = checkForVideoSizes(tracks[trackId].getFormat(), supported[formatIndex]);
					}
					else
					{
						chosen = supported[formatIndex];
					}
					tracks[trackId].setFormat(chosen);
					Logger.log.info("Track " + trackId + " is set to transmit as: "+ chosen);
					countTrack++;
				}
				else
				{
					tracks[trackId].setEnabled(false);
				}
			}
			else
			{
				tracks[trackId].setEnabled(false);
			}
		}

		if (countTrack!=2)
		{
			Logger.log.error("Non è stato possibile settare un valido formato RTP per tutte le tracce!");
			return false;
		}
		
		// Realize the processor. This will internally create a flow
		// graph and attempt to create an output datasource for JPEG/RTP
		// audio frames.
		waitForStateResult = waitForState(processor, Controller.Realized);
		if (waitForStateResult == false)
		{
			Logger.log.error("Couldn't realize processor");
			return false;
		}
		
		/*
		//Per settare eventuali parametri sulla codifica H.263		
		Object objH263Control = processor.getControl("javax.media.control.H263Control");
		if(objH263Control!=null)
		{
			Logger.log.info("Setting H.263 coding...");
			H263Control control = (H263Control)objH263Control;
			//Per test, setto i parametri negando tutti i parametri di default
			control.setAdvancedPrediction(!control.getAdvancedPrediction());
			control.setArithmeticCoding(!control.getArithmeticCoding());
			control.setErrorCompensation(!control.getErrorCompensation());
			control.setPBFrames(!control.getPBFrames());
			control.setUnrestrictedVector(!control.getUnrestrictedVector());
		}
		*/		
				
		//Setto la qualita' della compressione JPEG del video eventualmente usata
		setJPEGQuality(processor, ConstantManager.JPEG_QUALITY/100f);

		// Get the output data source of the processor
		dataOutput = processor.getDataOutput();

		return true;
	}

	/**
	 * Use the RTPManager API to create sessions for each media track of the
	 * processor.
	 */
	private boolean createTransmitter()
	{
		PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
		PushBufferStream pbss[] = pbds.getStreams();

		rtpMgrs = new RTPManager[pbss.length];
		SendStream sendStream;
		
		for (int sessionIndex = 0; sessionIndex < pbss.length; sessionIndex++)
		{
			try
			{
				rtpMgrs[sessionIndex] = RTPManager.newInstance();

				if (sessionIndex == 0)
				{
					rtpMgrs[sessionIndex].initialize(new RTPMultiplexerConnector(client, this.idClient, 1, 2));
				}
				else if (sessionIndex == 1)
				{
					rtpMgrs[sessionIndex].initialize(new RTPMultiplexerConnector(client, this.idClient, 3, 4));
				}
				
				sendStream = rtpMgrs[sessionIndex].createSendStream(dataOutput, sessionIndex);
				sendStream.start();
			}
			catch (Exception e)
			{
				Logger.log.error("Errore inizializzazione RTPManager: " + Utility.exceptionToString(e));
				return false;
			}
		}

		return true;
	}

	/**
	 * For JPEG and H263, we know that they only work for particular sizes. So
	 * we'll perform extra checking here to make sure they are of the right
	 * sizes.
	 */
	Format checkForVideoSizes(Format original, Format supported)
	{
		int width, height;
		Dimension size = ((VideoFormat) original).getSize();
		Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
		Format h263Fmt = new Format(VideoFormat.H263_RTP);
				
		if (supported.matches(jpegFmt))
		{
			// For JPEG, make sure width and height are divisible by 8.
			width = (size.width % 8 == 0 ? size.width : (int) (size.width / 8) * 8);
			height = (size.height % 8 == 0 ? size.height : (int) (size.height / 8) * 8);
		}
		else if (supported.matches(h263Fmt))
		{
			// For H.263, we only support some specific sizes.
			if (size.width < 128)
			{
				width = 128;
				height = 96;
			}
			else if (size.width < 176)
			{
				width = 176;
				height = 144;
			}
			else
			{
				width = 352;
				height = 288;
			}
		}
		else
		{
			// We don't know this particular format. We'll just
			// leave it alone then.
			return supported;
		}

		return (new VideoFormat(null, new Dimension(width, height), Format.NOT_SPECIFIED, null, Format.NOT_SPECIFIED)).intersects(supported);
	}

	/**
	 * Setting the encoding quality to the specified value on the JPEG encoder.
	 * 0.5 is a good default.
	 */
	void setJPEGQuality(Player p, float val)
	{
		Control cs[] = p.getControls();
		QualityControl qc = null;
		VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

		// Loop through the controls to find the Quality control for
		// the JPEG encoder.
		for (int i = 0; i < cs.length; i++)
		{
			if (cs[i] instanceof QualityControl && cs[i] instanceof Owned)
			{
				Object owner = ((Owned) cs[i]).getOwner();

				// Check to see if the owner is a Codec.
				// Then check for the output format.
				if (owner instanceof Codec)
				{
					Format fmts[] = ((Codec) owner).getSupportedOutputFormats(null);
					for (int j = 0; j < fmts.length; j++)
					{
						if (fmts[j].matches(jpegFmt))
						{
							qc = (QualityControl) cs[i];
							qc.setQuality(val);
							Logger.log.debug("Setting quality to " + val + " on " + qc);
							break;
						}
					}
				}
				if (qc != null)
					break;
			}
		}
	}	
	

	/****************************************************************
	 * Convenience methods to handle processor's state changes.
	 ****************************************************************/
	private Integer stateLock = new Integer(0);
	private boolean failed = false;

	Integer getStateLock()
	{
		return stateLock;
	}

	void setFailed()
	{
		failed = true;
	}

	private synchronized boolean waitForState(Processor p, int state)
	{
		p.addControllerListener(new StateListener());
		failed = false;

		// Call the required method on the processor
		if (state == Processor.Configured)
		{
			p.configure();
		}
		else if (state == Processor.Realized)
		{
			p.realize();
		}

		// Wait until we get an event that confirms the
		// success of the method, or a failure event.
		// See StateListener inner class
		while (p.getState() < state && !failed)
		{
			synchronized (getStateLock())
			{
				try
				{
					getStateLock().wait();
				}
				catch (InterruptedException ie)
				{
					return false;
				}
			}
		}

		if (failed)
			return false;
		else
			return true;
	}

	/****************************************************************
	 * Inner Classes
	 ****************************************************************/
	class StateListener implements ControllerListener
	{

		public void controllerUpdate(ControllerEvent ce)
		{

			// If there was an error during configure or
			// realize, the processor will be closed
			if (ce instanceof ControllerClosedEvent)
				setFailed();

			// All controller events, send a notification
			// to the waiting thread in waitForState method.
			if (ce instanceof ControllerEvent)
			{
				synchronized (getStateLock())
				{
					getStateLock().notifyAll();
				}
			}
		}
	}
	

}
