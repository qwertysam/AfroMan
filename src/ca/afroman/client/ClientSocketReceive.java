package ca.afroman.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.List;

import ca.afroman.assets.AssetType;
import ca.afroman.entity.ClientPlayerEntity;
import ca.afroman.entity.api.ClientAssetEntity;
import ca.afroman.entity.api.Direction;
import ca.afroman.entity.api.Entity;
import ca.afroman.entity.api.Hitbox;
import ca.afroman.gfx.PointLight;
import ca.afroman.gui.GuiClickNotification;
import ca.afroman.gui.GuiConnectToServer;
import ca.afroman.gui.GuiJoinServer;
import ca.afroman.gui.GuiLobby;
import ca.afroman.gui.GuiMainMenu;
import ca.afroman.gui.GuiSendingLevels;
import ca.afroman.level.ClientLevel;
import ca.afroman.level.Level;
import ca.afroman.level.LevelType;
import ca.afroman.network.ConnectedPlayer;
import ca.afroman.network.IPConnection;
import ca.afroman.packet.DenyJoinReason;
import ca.afroman.packet.Packet;
import ca.afroman.packet.PacketConfirmReceived;
import ca.afroman.packet.PacketType;
import ca.afroman.player.Role;
import ca.afroman.thread.DynamicThread;

public class ClientSocketReceive extends DynamicThread
{
	private ClientSocketManager manager;
	
	// private DatagramSocket socket;
	
	private List<Integer> receivedPackets; // The ID's of all the packets that have been received
	
	public ClientSocketReceive(ClientSocketManager manager)
	{
		this.manager = manager;
		
		receivedPackets = new ArrayList<Integer>();
		
		this.setName("Client-Socket-Receive");
	}
	
	@Override
	public void onRun()
	{
		byte[] buffer = new byte[1024];
		
		// Loads up the buffer with incoming data
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		try
		{
			manager.socket().receive(packet);
			
			this.parsePacket(packet.getData(), new IPConnection(packet.getAddress(), packet.getPort()));
		}
		catch (PortUnreachableException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void parsePacket(byte[] data, IPConnection connection)
	{
		PacketType type = Packet.readType(data);
		if (ClientSocketManager.TRACE_PACKETS) System.out.println("[CLIENT] [RECIEVE] [" + connection.asReadable() + "] " + type.toString());
		
		// If is the server sending the packet
		if (ClientGame.instance().sockets().getConnectedPlayer().getConnection().equals(connection))
		{
			int packetID = Packet.readID(data);
			
			if (packetID != -1)
			{
				for (int packID : receivedPackets)
				{
					if (packID == packetID)
					{
						System.out.println("Received packet already: " + packID);
						
						// If the packet with this ID has already been received, tell the server to stop sending it, and don't parse it
						manager.sender().sendPacket(new PacketConfirmReceived(packetID));
						return;
					}
				}
				
				// If the packet with the ID has not already been received
				manager.sender().sendPacket(new PacketConfirmReceived(packetID));
				// Add it to the list
				receivedPackets.add(packetID);
			}
			
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
				case ASSIGN_CLIENTID:
					ClientGame.instance().sockets().getConnectedPlayer().setID(Integer.parseInt(Packet.readContent(data)));;
					break;
				case UPDATE_PLAYERLIST:
				{
					ClientGame.instance().updatePlayerList();
					
					String[] split = Packet.readContent(data).split(",");
					
					List<ConnectedPlayer> players = new ArrayList<ConnectedPlayer>();
					for (int i = 0; i < split.length; i += 3)
					{
						players.add(new ConnectedPlayer(Integer.parseInt(split[i]), Role.fromOrdinal(Integer.parseInt(split[i + 1])), split[i + 2]));
					}
					
					ClientGame.instance().sockets().updateConnectedPlayer(players);
					
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
				case SEND_LEVELS:
				{
					boolean sendingLevels = (Integer.parseInt(Packet.readContent(data)) == 1);
					
					if (sendingLevels)
					{
						// Prepare the level storage for new levels to be sent
						ClientGame.instance().getLevels().clear();
						
						// Display the loading level screen
						if (!(ClientGame.instance().getCurrentScreen() instanceof GuiSendingLevels))
						{
							ClientGame.instance().setCurrentScreen(new GuiSendingLevels(null));
						}
					}
					else
					{
						// Stop displaying the loading level screen
						if (ClientGame.instance().getCurrentScreen() instanceof GuiSendingLevels)
						{
							ClientGame.instance().setCurrentScreen(ClientGame.instance().getCurrentScreen().getParent());
						}
					}
				}
					break;
				case INSTANTIATE_LEVEL:
				{
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(Packet.readContent(data)));
					
					if (ClientGame.instance().getLevelByType(levelType) == null)
					{
						ClientGame.instance().getLevels().add(new ClientLevel(levelType));
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
					
					if (level != null)
					{
						int layer = Integer.parseInt(split[2]);
						
						AssetType asset = AssetType.fromOrdinal(Integer.parseInt(split[3]));
						
						double x = Double.parseDouble(split[4]);
						double y = Double.parseDouble(split[5]);
						double width = Double.parseDouble(split[6]);
						double height = Double.parseDouble(split[7]);
						
						List<Entity> tiles = level.getTiles(layer);
						
						synchronized (tiles)
						{
							// If it has custom hitboxes defined
							if (split.length > 8)
							{
								List<Hitbox> tileHitboxes = new ArrayList<Hitbox>();
								
								for (int i = 8; i < split.length; i += 4)
								{
									tileHitboxes.add(new Hitbox(Double.parseDouble(split[i]), Double.parseDouble(split[i + 1]), Double.parseDouble(split[i + 2]), Double.parseDouble(split[i + 3])));
								}
								
								tiles.add(new ClientAssetEntity(id, level, asset, x, y, width, height, Entity.hitBoxListToArray(tileHitboxes)));
							}
							else
							{
								tiles.add(new ClientAssetEntity(id, level, asset, x, y, width, height));
							}
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
						int layer = Integer.parseInt(split[1]);
						Entity tile = level.getTile(Integer.parseInt(split[2]));
						
						if (tile != null)
						{
							List<Entity> tiles = level.getTiles(layer);
							
							synchronized (tiles)
							{
								tiles.remove(tile);
							}
						}
					}
				}
					break;
				case ADD_LEVEL_HITBOX:
				{
					String[] split = Packet.readContent(data).split(",");
					
					ClientLevel level = ClientGame.instance().getLevelByType(LevelType.fromOrdinal(Integer.parseInt(split[0])));
					
					if (level != null)
					{
						int id = Integer.parseInt(split[1]);
						double x = Double.parseDouble(split[2]);
						double y = Double.parseDouble(split[3]);
						double width = Double.parseDouble(split[4]);
						double height = Double.parseDouble(split[5]);
						
						level.getHitboxes().add(new Hitbox(id, x, y, width, height));
					}
				}
					break;
				case REMOVE_LEVEL_HITBOX:
				{
					String[] split = Packet.readContent(data).split(",");
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(split[0]));
					Level level = ClientGame.instance().getLevelByType(levelType);
					
					if (level != null)
					{
						Hitbox box = level.getHitbox(Integer.parseInt(split[1]));
						
						if (box != null)
						{
							level.getHitboxes().remove(box);
						}
					}
				}
					break;
				case ADD_LEVEL_POINTLIGHT:
				{
					String[] split = Packet.readContent(data).split(",");
					
					ClientLevel level = ClientGame.instance().getLevelByType(LevelType.fromOrdinal(Integer.parseInt(split[0])));
					
					if (level != null)
					{
						int id = Integer.parseInt(split[1]);
						double x = Double.parseDouble(split[2]);
						double y = Double.parseDouble(split[3]);
						double radius = Double.parseDouble(split[4]);
						
						level.getLights().add(new PointLight(id, level, x, y, radius));
					}
				}
					break;
				case REMOVE_LEVEL_POINTLIGHT: // TODO
				{
					String[] split = Packet.readContent(data).split(",");
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(split[0]));
					Level level = ClientGame.instance().getLevelByType(levelType);
					
					if (level != null)
					{
						PointLight light = level.getLight(Integer.parseInt(split[1]));
						
						if (light != null)
						{
							level.getLights().remove(light);
						}
					}
				}
					break;
				case ADD_LEVEL_PLAYER:
				{
					String[] split = Packet.readContent(data).split(",");
					LevelType levelType = LevelType.fromOrdinal(Integer.parseInt(split[0]));
					ClientLevel level = ClientGame.instance().getLevelByType(levelType);
					
					if (level != null)
					{
						ClientPlayerEntity player = ClientGame.instance().getPlayer(Role.fromOrdinal(Integer.parseInt(split[1])));
						
						if (player != null)
						{
							player.setX(Double.parseDouble(split[2]));
							player.setY(Double.parseDouble(split[3]));
							player.addToLevel(level);
							
							// If it's adding the player that this player is, center the camera on them
							if (player.getRole() == ClientGame.instance().sockets().getConnectedPlayer().getRole())
							{
								player.setCameraToFollow(true);
							}
						}
					}
				}
					break;
				case SET_PLAYER_LOCATION:
				{
					String[] split = Packet.readContent(data).split(",");
					ClientPlayerEntity player = ClientGame.instance().getPlayer(Role.fromOrdinal(Integer.parseInt(split[0])));
					
					if (player != null)
					{
						player.setDirection(Direction.fromOrdinal(Integer.parseInt(split[1])));
						player.setLastDirection(Direction.fromOrdinal(Integer.parseInt(split[2])));
						player.setX(Double.parseDouble(split[3]));
						player.setY(Double.parseDouble(split[4]));
					}
				}
					break;
				case CONFIRM_RECEIVED:
				{
					int sentID = Integer.parseInt(Packet.readContent(data));
					
					manager.sender().removePacketFromQueue(sentID);
				}
					break;
			}
		}
		else
		{
			System.out.println("[CLIENT] [CRITICAL] A server (" + connection.asReadable() + ") is tring to send a packet to this unlistening client." + type.toString());
		}
	}
	
	@Override
	public void onStop()
	{
		manager.socket().close();
		receivedPackets.clear();
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onPause()
	{
		
	}
	
	@Override
	public void onUnpause()
	{
		
	}
}