package ca.afroman;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ca.afroman.asset.AssetType;
import ca.afroman.entity.Entity;
import ca.afroman.entity.TextureEntity;
import ca.afroman.gui.GuiClickNotification;
import ca.afroman.gui.GuiConnectToServer;
import ca.afroman.gui.GuiJoinServer;
import ca.afroman.gui.GuiLobby;
import ca.afroman.gui.GuiMainMenu;
import ca.afroman.level.ClientLevel;
import ca.afroman.level.Level;
import ca.afroman.level.LevelType;
import ca.afroman.network.ConnectedPlayer;
import ca.afroman.network.IPConnection;
import ca.afroman.packet.DenyJoinReason;
import ca.afroman.packet.Packet;
import ca.afroman.packet.PacketType;
import ca.afroman.player.Role;
import ca.afroman.server.ServerSocket;

public class ClientSocket extends Thread
{
	public static int id = -1;
	private InetAddress serverIP = null;
	private DatagramSocket socket;
	// private Game game;
	private List<ConnectedPlayer> playerList;
	
	public ClientSocket()
	{
		playerList = new ArrayList<ConnectedPlayer>();
		
		try
		{
			this.socket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized void setServerIP(String serverIpAddress)
	{
		if (serverIpAddress == null)
		{
			serverIP = null;
			return;
		}
		
		InetAddress ip = null;
		
		try
		{
			ip = InetAddress.getByName(serverIpAddress);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
			
			ClientGame.instance().setCurrentScreen(new GuiJoinServer(new GuiMainMenu()));
			new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "UNKNOWN", "HOST");
			return;
		}
		
		this.serverIP = ip;
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			byte[] buffer = new byte[1024];
			
			// Loads up the buffer with incoming data
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			try
			{
				socket.receive(packet);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			this.parsePacket(packet.getData(), new IPConnection(packet.getAddress(), packet.getPort()));
		}
	}
	
	public void parsePacket(byte[] data, IPConnection connection)
	{
		PacketType type = Packet.readType(data);
		
		// If is the server sending the packet
		if (connection.getIPAddress().getHostAddress().equals(this.serverIP.getHostAddress()) && ServerSocket.PORT == connection.getPort())
		{
			System.out.println("[CLIENT] [RECIEVE] [" + connection.asReadable() + "] " + type.toString());
			
			switch (type)
			{
				default:
				case INVALID:
					System.out.println("[CLIENT] INVALID PACKET");
					break;
				case DENY_JOIN:
					// Game.instance().setPassword("INVALID PASSWORD");
					ClientGame.instance().setCurrentScreen(new GuiJoinServer(new GuiMainMenu()));
					
					DenyJoinReason reason = DenyJoinReason.fromOrdinal(Integer.parseInt(Packet.readContent(data)));
					
					switch (reason)
					{
						default:
							new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "CAN'T CONNECT", "TO SERVER");
							break;
						case DUPLICATE_USERNAME:
							new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "DUPLICATE", "USERNAME");
							break;
						case FULL_SERVER:
							new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "SERVER", "FULL");
							break;
						case NEED_PASSWORD:
							new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "INVALID", "PASSWORD");
							break;
						case OLD_CLIENT:
							new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "CLIENT", "OUTDATED");
							break;
						case OLD_SERVER:
							new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "SERVER", "OUTDATED");
							break;
					}
					break;
				case ASSIDN_CLIENTID:
					id = Integer.parseInt(Packet.readContent(data));
					break;
				case UPDATE_PLAYERLIST:
				{
					ClientGame.instance().updatePlayerList = 2;
					
					String[] split = Packet.readContent(data).split(",");
					
					List<ConnectedPlayer> players = new ArrayList<ConnectedPlayer>();
					for (int i = 0; i < split.length; i += 3)
					{
						players.add(new ConnectedPlayer(Integer.parseInt(split[i]), Role.fromOrdinal(Integer.parseInt(split[i + 1])), split[i + 2]));
					}
					
					this.playerList = players;
					
					if (ClientGame.instance().getCurrentScreen() instanceof GuiConnectToServer)
					{
						ClientGame.instance().setCurrentScreen(new GuiLobby(null));
					}
				}
					break;
				case STOP_SERVER:
					ClientGame.instance().exitFromGame();
					new GuiClickNotification(ClientGame.instance().getCurrentScreen(), "SERVER", "CLOSED");
					break;
				case INSTANTIATE_LEVEL:
				{
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(Packet.readContent(data)));
					
					if (ClientGame.instance().getLevelByType(levelType) == null)
					{
						System.out.println("[CLIENT] Adding Level: " + levelType);
						ClientGame.instance().levels.add(new ClientLevel(levelType));
						ClientGame.instance().setCurrentLevel(ClientGame.instance().getLevelByType(levelType));
					}
					else
					{
						System.out.println("[CLIENT] Level with type " + levelType + " already exists.");
					}
				}
					break;
				case ADD_LEVEL_TILE:
				{
					String[] split = Packet.readContent(data).split(",");
					int id = Integer.parseInt(split[0]);
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(split[1]));
					ClientLevel level = ClientGame.instance().getLevelByType(levelType);
					
					System.out.println("LEVEL SHIT: " + id);
					
					if (level != null)
					{
						AssetType asset = AssetType.fromOrdinal(Integer.parseInt(split[2]));
						
						double x = Double.parseDouble(split[3]);
						double y = Double.parseDouble(split[4]);
						double width = Double.parseDouble(split[5]);
						double height = Double.parseDouble(split[6]);
						
						// If it has custom hitboxes defined
						if (split.length > 7)
						{
							List<Rectangle2D.Double> tileHitboxes = new ArrayList<Rectangle2D.Double>();
							
							for (int i = 8; i < split.length; i += 4)
							{
								tileHitboxes.add(new Rectangle2D.Double(Double.parseDouble(split[i]), Double.parseDouble(split[i + 1]), Double.parseDouble(split[i + 2]), Double.parseDouble(split[i + 3])));
							}
							
							level.addTile(new TextureEntity(id, level, asset, x, y, width, height, Entity.hitBoxListToArray(tileHitboxes)));
						}
						else
						{
							level.addTile(new TextureEntity(id, level, asset, x, y, width, height));
						}
					}
					else
					{
						System.out.println("[CLIENT] No level with type " + levelType);
					}
				}
					break;
				case REMOVE_LEVEL_TILE:
				{
					String[] split = Packet.readContent(data).split(",");
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(split[0]));
					Level level = ClientGame.instance().getLevelByType(levelType);
					
					if (level != null)
					{
						Entity tile = level.getTile(Integer.parseInt(split[1]));
						
						if (tile != null)
						{
							level.removeTile(tile);
						}
					}
				}
					break;
				case ADD_LEVEL_HITBOX:
				{
					String[] split = Packet.readContent(data).split(",");
					
					ClientLevel level = ClientGame.instance().getLevelByType(LevelType.fromOrdinal(Integer.parseInt(split[0])));
					double x = Double.parseDouble(split[1]);
					double y = Double.parseDouble(split[2]);
					double width = Double.parseDouble(split[3]);
					double height = Double.parseDouble(split[4]);
					
					level.addHitbox(new Rectangle2D.Double(x, y, width, height));
				}
					break;
			}
		}
		else
		{
			System.out.println("[CLIENT] [CRITICAL] A server (" + connection.asReadable() + ") is tring to send a packet to this unlistening client." + type.toString());
		}
	}
	
	public int getPlayerID()
	{
		return id;
	}
	
	/**
	 * Sends a packet to the server.
	 * 
	 * @param packet the packet
	 */
	public void sendPacket(Packet packet)
	{
		sendData(packet.getData());
	}
	
	/**
	 * Sends a byte array of data to the server.
	 * 
	 * @param data the data
	 * 
	 * @deprecated Still works to send raw data, but sendPacket() is preferred.
	 */
	@Deprecated
	public void sendData(byte[] data)
	{
		if (serverIP != null)
		{
			DatagramPacket packet = new DatagramPacket(data, data.length, serverIP, ServerSocket.PORT);
			
			System.out.println("[CLIENT] [SEND] [" + this.serverIP + ":" + ServerSocket.PORT + "] " + new String(data));
			
			try
			{
				socket.send(packet);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return if this client has a server that it's listening to.
	 */
	public boolean isListening()
	{
		return this.serverIP != null;
	}
	
	public ConnectedPlayer thisPlayer()
	{
		return playerByID(id);
	}
	
	public ConnectedPlayer playerByRole(Role role)
	{
		for (ConnectedPlayer player : playerList)
		{
			if (player.getRole() == role) return player;
		}
		
		return null;
	}
	
	public ConnectedPlayer playerByID(int id)
	{
		for (ConnectedPlayer player : playerList)
		{
			if (player.getID() == id) return player;
		}
		
		return null;
	}
	
	/**
	 * @return a list of all the ConnectedPlayers, exclusing this current player.
	 */
	public List<ConnectedPlayer> otherPlayers()
	{
		List<ConnectedPlayer> toReturn = new ArrayList<ConnectedPlayer>();
		
		for (ConnectedPlayer player : playerList)
		{
			if (player.getID() != id) toReturn.add(player);
		}
		
		return toReturn;
	}
	
	public List<ConnectedPlayer> getPlayers()
	{
		return playerList;
	}
}
