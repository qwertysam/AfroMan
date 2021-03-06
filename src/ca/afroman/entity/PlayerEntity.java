package ca.afroman.entity;

import ca.afroman.assets.AssetType;
import ca.afroman.assets.Assets;
import ca.afroman.assets.SpriteAnimation;
import ca.afroman.assets.Texture;
import ca.afroman.battle.BattlePlayerEntity;
import ca.afroman.battle.BattlePosition;
import ca.afroman.client.ClientGame;
import ca.afroman.entity.api.DrawableEntityDirectional;
import ca.afroman.entity.api.Hitbox;
import ca.afroman.game.Role;
import ca.afroman.inventory.Inventory;
import ca.afroman.level.api.Level;
import ca.afroman.packet.level.PacketPlayerInteract;
import ca.afroman.packet.level.PacketSetPlayerLevel;
import ca.afroman.packet.level.PacketSetPlayerLocationServerClient;
import ca.afroman.resource.Vector2DDouble;
import ca.afroman.server.ServerGame;

public class PlayerEntity extends DrawableEntityDirectional
{
	private static final int INVINCIBILITY_FLICKER_TICKS_SLOW = 15;
	private static final int INVINCIBILITY_FLICKER_TICKS_FAST = 5;
	
	private static SpriteAnimation getDown(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_DOWN).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_DOWN).clone();
	}
	
	private static SpriteAnimation getIdleDown(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_IDLE_DOWN).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_IDLE_DOWN).clone();
	}
	
	private static SpriteAnimation getIdleLeft(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_IDLE_LEFT).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_IDLE_LEFT).clone();
	}
	
	private static SpriteAnimation getIdleRight(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_IDLE_RIGHT).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_IDLE_RIGHT).clone();
	}
	
	private static SpriteAnimation getIdleUp(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_IDLE_UP).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_IDLE_UP).clone();
	}
	
	private static SpriteAnimation getLeft(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_LEFT).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_LEFT).clone();
	}
	
	private static SpriteAnimation getRight(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_RIGHT).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_RIGHT).clone();
	}
	
	private static SpriteAnimation getUp(boolean isServerSide, Role role)
	{
		return isServerSide ? null : role == Role.PLAYER1 ? (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_ONE_UP).clone() : (SpriteAnimation) Assets.getSpriteAnimation(AssetType.PLAYER_TWO_UP).clone();
	}
	
	private Role role;
	private Inventory inv;
	
	private boolean invincibilityFlicker = false;
	
	private int invincibilityFlickerCounter = 0;
	
	/**
	 * An entity representing a human player.
	 * 
	 * @param isServerSide whether this is on the server instance or not
	 * @param role the Role assigned to this player
	 * @param position the position
	 */
	public PlayerEntity(boolean isServerSide, Role role, Vector2DDouble position)
	{
		super(isServerSide, true, position, getUp(isServerSide, role), getDown(isServerSide, role), getLeft(isServerSide, role), getRight(isServerSide, role), getIdleUp(isServerSide, role), getIdleDown(isServerSide, role), getIdleLeft(isServerSide, role), getIdleRight(isServerSide, role), new Hitbox(isServerSide, true, 3, 5, 10, 11));
		
		this.role = role;
		inv = new Inventory(this);
		
		battleEntity1 = new BattlePlayerEntity(this, role == Role.PLAYER1 ? BattlePosition.RIGHT_TOP : BattlePosition.RIGHT_BOTTOM);
	}
	
	/**
	 * Removes an entity from their current level and puts them in another level.
	 * 
	 * @param level the new level.
	 */
	@Override
	public void addToLevel(Level newLevel)
	{
		if (level == newLevel) return;
		
		if (level != null)
		{
			level.getPlayers().remove(this);
		}
		
		// Sets the new level
		level = newLevel;
		
		if (level != null)
		{
			level.getPlayers().add(this);
		}
		
		if (isServerSide())
		{
			ServerGame.instance().sockets().sender().sendPacketToAllClients(new PacketSetPlayerLevel(this.getRole(), level));
		}
		// }
		// else if (isServerSide())
		// {
		// ServerGame.instance().logger().log(ALogType.IMPORTANT, "Shouldn't be adding a player to a null level... Do something to fix this?");
		// }
	}
	
	public Inventory getInventory()
	{
		return inv;
	}
	
	/**
	 * @return the role of this.
	 */
	public Role getRole()
	{
		return role;
	}
	
	@Override
	public void render(Texture renderTo)
	{
		if (!isInvincible() || invincibilityFlicker)
		{
			super.render(renderTo);
		}
	}
	
	/**
	 * Stop the camera from following this, and removes this from the level.
	 */
	public void reset()
	{
		setCameraToFollow(false);
		removeFromLevel();
		setBattle(null);
		battleEntity1.reset();
	}
	
	@Override
	public void setPosition(double x, double y)
	{
		setPosition(x, y, true);
	}
	
	public void setPosition(double x, double y, boolean sendPositionpackets)
	{
		super.setPosition(x, y);
		
		if (isServerSide() && sendPositionpackets)
		{
			ServerGame.instance().sockets().sender().sendPacketToAllClients(new PacketSetPlayerLocationServerClient(getRole(), getPosition(), true));
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if (isServerSide())
		{
			
		}
		else
		{
			// Does the invincibility flicker
			if (isInvincible())
			{
				invincibilityFlickerCounter++;
				
				// Flickers slowly until the last second (60 frames)
				if (invincibilityFrames > 60)
				{
					if (invincibilityFlickerCounter > INVINCIBILITY_FLICKER_TICKS_SLOW)
					{
						invincibilityFlickerCounter = 0;
						invincibilityFlicker = !invincibilityFlicker;
					}
				}
				// Within the last second it flickers quickly
				else if (invincibilityFlickerCounter > INVINCIBILITY_FLICKER_TICKS_FAST)
				{
					invincibilityFlickerCounter = 0;
					invincibilityFlicker = !invincibilityFlicker;
				}
			}
			
			// If it's not in build mode and the role of this is the role of the client, let them move
			if (!ClientGame.instance().isBuildMode() && this.role == ClientGame.instance().getRole())
			{
				if (ClientGame.instance().getCurrentScreen() == null && !ClientGame.instance().getThisPlayer().isInBattle())
				{
					byte xa = 0;
					byte ya = 0;
					
					if (ClientGame.instance().input().interact.isPressedFiltered())
					{
						ClientGame.instance().sockets().sender().sendPacket(new PacketPlayerInteract(getPosition()));
					}
					if (ClientGame.instance().input().prevItem.isPressedTyping() || ClientGame.instance().input().mouseWheelDown.isPressedFiltered())
					{
						getInventory().gotoPrevItem();
					}
					if (ClientGame.instance().input().nextItem.isPressedTyping() || ClientGame.instance().input().mouseWheelUp.isPressedFiltered())
					{
						getInventory().gotoNextItem();
					}
					if (ClientGame.instance().input().dropItem.isPressedTyping())
					{
						getInventory().dropItem();
					}
					if (ClientGame.instance().input().useItem.isPressedTyping())
					{
						// TODO use item
					}
					if (ClientGame.instance().input().up.isPressed())
					{
						ya = -1;
					}
					if (ClientGame.instance().input().down.isPressed())
					{
						ya = 1;
					}
					if (ClientGame.instance().input().left.isPressed())
					{
						xa = -1;
					}
					if (ClientGame.instance().input().right.isPressed())
					{
						xa = 1;
					}
					
					this.move(xa, ya);
				}
			}
		}
	}
	
	@Override
	public void tryInteract(PlayerEntity triggerer)
	{
		
	}
}
