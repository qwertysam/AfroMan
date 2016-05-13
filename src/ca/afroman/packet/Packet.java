package ca.afroman.packet;

public abstract class Packet
{
	/** The pattern that separates the PacketType ordinal from a the content. */
	public static final String SEPARATOR = ":;";
	
	protected PacketType type;
	
	public Packet(PacketType type)
	{
		this.type = type;
	}
	
	/**
	 * Gets the data from this Packet in a sendable form.
	 * 
	 * @return the data
	 */
	public abstract byte[] getData();
	
	/**
	 * Gets the packet's content as a String without the initial
	 * separator that separates the PacketType ordinal in the data.
	 * 
	 * @param data the raw data
	 * @return the data as a String
	 */
	public static String readContent(byte[] data)
	{
		String message = "";
		try
		{
			message = new String(data).trim();
			String[] split = message.split(SEPARATOR);
			message = "";
			
			for (int i = 1; i < split.length; i++)
			{
				if (split[i] != null)
				{
					message += split[i];
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		return message;
	}
	
	/**
	 * Gets the packet's PacketType from the raw data.
	 * 
	 * @param data the raw data
	 * @return the PacketType
	 */
	public static PacketType readType(byte[] data)
	{
		int ordinal = 0;
		
		try
		{
			String message = new String(data).trim();
			String[] split = message.split(SEPARATOR);
			ordinal = Integer.parseInt(split[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return PacketType.INVALID;
		}
		
		if (ordinal >= 0 && ordinal < PacketType.values().length)
		{
			return PacketType.fromOrdinal(ordinal);
		}
		else
		{
			return PacketType.INVALID;
		}
	}
}