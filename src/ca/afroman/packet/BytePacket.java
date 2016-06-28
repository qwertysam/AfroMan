package ca.afroman.packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.afroman.legacy.packet.PacketType;
import ca.afroman.network.IPConnection;
import ca.afroman.util.ArrayUtil;
import ca.afroman.util.ByteUtil;
import ca.afroman.util.IDCounter;

public class BytePacket
{
	private static IDCounter idCounter;
	
	public static IDCounter getIDCounter()
	{
		if (idCounter == null)
		{
			idCounter = new IDCounter();
		}
		
		return idCounter;
	}
	
	private PacketType type;
	private int id;
	private byte[] content;
	private List<IPConnection> connections;
	
	/**
	 * Creates a new BytePacket to send.
	 * 
	 * @param type the type of packet being sent
	 * @param mustSend whether this packet must be pushed until the other end confirms of receiving it
	 * @param receiver the desired receiver of the packet
	 */
	public BytePacket(PacketType type, boolean mustSend, IPConnection... receivers)
	{
		this.type = type;
		id = mustSend ? getIDCounter().getNext() : IDCounter.WASTE_ID;
		
		connections = new ArrayList<IPConnection>();
		
		if (receivers != null)
		{
			for (IPConnection con : receivers)
			{
				connections.add(con);
			}
		}
	}
	
	/**
	 * Parses a BytePacket from raw byte data.
	 * 
	 * @param rawData
	 * @param sender
	 */
	public BytePacket(byte[] rawData, IPConnection sender)
	{
		type = PacketType.fromOrdinal(ByteUtil.shortFromBytes(Arrays.copyOfRange(rawData, 0, 2)));
		id = ByteUtil.intFromBytes(Arrays.copyOfRange(rawData, 2, 7));
		content = Arrays.copyOfRange(rawData, 7, rawData.length);
		connections = new ArrayList<IPConnection>();
		connections.add(sender);
	}
	
	/**
	 * Gets the data from this Packet in a sendable form.
	 * <p>
	 * <b>WARNING:</b> Only intended for reading, DO NOT OVERRIDE.
	 * <p>
	 * Override <code>getUniqueData()</code> instead
	 *
	 * @return the data
	 */
	public final byte[] getData()
	{
		byte[] type = ByteUtil.shortAsBytes((short) getType().ordinal());
		byte[] id = ByteUtil.intAsBytes(this.id);
		byte[] toConcat = getUniqueData();
		// byte[] toRet = new byte[7 + toConcat.length];
		//
		// for (int i = 0; i < type.length; i++)
		// toRet[i] = type[i];
		//
		// for (int i = 0; i < id.length; i++)
		// toRet[i + type.length] = id[i];
		//
		// for (int i = 0; i < toConcat.length; i++)
		// toRet[i + type.length + id.length] = toConcat[i];
		
		// for (byte e : type)
		// {
		// System.out.println("type: " + e);
		// }
		//
		// for (byte e : id)
		// {
		// System.out.println(" id: " + e);
		// }
		//
		// for (byte e : toConcat)
		// {
		// System.out.println("cnct: " + e);
		// }
		
		byte[] ret = ArrayUtil.concatByteArrays(type, id, toConcat);
		
		return ret;
	}
	
	/**
	 * Gets the data from this Packet in a sendable form.
	 * 
	 * @return the data
	 */
	public byte[] getUniqueData()
	{
		return new byte[] {};
	}
	
	public byte[] getContent()
	{
		return content;
	}
	
	public boolean mustSend()
	{
		return id != IDCounter.WASTE_ID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public PacketType getType()
	{
		return type;
	}
	
	public List<IPConnection> getConnections()
	{
		return connections;
	}
	
	public void setConnections(IPConnection... con)
	{
		this.connections = Arrays.asList(con);
	}
	
	public void setConnections(List<IPConnection> con)
	{
		this.connections = con;
	}
}