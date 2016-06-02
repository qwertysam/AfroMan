package ca.afroman.assets;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import ca.afroman.client.ClientGame;
import ca.afroman.log.ALogType;

public class AudioClip extends Asset
{
	private static final String AUDIO_DIR = "/audio/";
	
	private Clip clip;
	
	public AudioClip(AssetType type, Clip clip)
	{
		super(type);
		
		this.clip = clip;
	}
	
	public static AudioClip fromResource(AssetType type, String path)
	{
		Clip clip = null;
		
		try
		{
			URL url = AudioClip.class.getResource(AUDIO_DIR + path);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
		}
		catch (UnsupportedAudioFileException e)
		{
			ClientGame.instance().logger().log(ALogType.CRITICAL, "Audio file is an unsupported type", e);
		}
		catch (IOException e)
		{
			ClientGame.instance().logger().log(ALogType.CRITICAL, "", e);
		}
		catch (LineUnavailableException e)
		{
			ClientGame.instance().logger().log(ALogType.CRITICAL, "", e);
		}
		
		return new AudioClip(type, clip);
	}
	
	public void startLoop()
	{
		// clip.setFramePosition(0);
		// clip.loop(200);
		// clip.start();
	}
	
	public void start()
	{
		// clip.setFramePosition(0);
		//
		// clip.start();
	}
	
	public void stop()
	{
		clip.stop();
	}
	
	@Override
	public Asset clone()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void dispose()
	{
		clip.stop();
		clip.flush();
		clip.close();
	}
}
