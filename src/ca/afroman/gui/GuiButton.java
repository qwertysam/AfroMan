package ca.afroman.gui;

import java.awt.Rectangle;

import ca.afroman.assets.AssetType;
import ca.afroman.assets.Assets;
import ca.afroman.assets.Texture;
import ca.afroman.client.ClientGame;
import ca.afroman.input.InputType;

public class GuiButton extends InputType
{
	protected GuiScreen screen;
	
	protected Rectangle hitbox;
	
	private int id;
	private boolean canHold = false;
	private boolean isEnabled = true;
	private int state = 0;
	private Texture[] textures;
	
	public GuiButton(GuiScreen screen, int id, int x, int y)
	{
		this(screen, Assets.getTexture(AssetType.BUTTON_NORMAL), Assets.getTexture(AssetType.BUTTON_HOVER), Assets.getTexture(AssetType.BUTTON_PRESSED), id, x, y);
	}
	
	public GuiButton(GuiScreen screen, Texture normal, Texture hover, Texture pressed, int id, int x, int y)
	{
		hitbox = new Rectangle(x, y, normal.getWidth(), normal.getHeight());
		
		textures = new Texture[3];
		textures[0] = normal;
		textures[1] = hover;
		textures[2] = pressed;
		
		this.screen = screen;
		this.id = id;
	}
	
	public void tick()
	{
		if (isEnabled)
		{
			int mouseX = ClientGame.instance().input.getMouseX();
			int mouseY = ClientGame.instance().input.getMouseY();
			
			if (hitbox.contains(mouseX, mouseY))
			{
				if (ClientGame.instance().input.mouseLeft.isPressed())
				{
					state = 2; // Down
					
					this.setPressed(true);
				}
				else
				{
					state = 1; // Hovering
					
					this.setPressed(false);
				}
			}
			else
			{
				state = 0; // Not on the button at all
				
				this.setPressed(false);
			}
			
			if (this.canHold() && this.isPressed())
			{
				onPressed();
			}
			else if (this.isPressedFiltered())
			{
				onPressed();
			}
			else if (this.isReleasedFiltered() && ClientGame.instance().input.mouseLeft.isReleased())
			{
				onRelease();
			}
		}
	}
	
	public boolean canHold()
	{
		return canHold;
	}
	
	public void setEnabled()
	{
		setEnabled(true);
	}
	
	public void setEnabled(boolean enabled)
	{
		isEnabled = enabled;
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}
	
	public void setCanHold(boolean canHold)
	{
		this.canHold = canHold;
	}
	
	protected void onPressed()
	{
		if (isEnabled && screen != null) screen.pressAction(id);
	}
	
	protected void onRelease()
	{
		if (isEnabled && screen != null) screen.releaseAction(id);
	}
	
	public Texture getTexture()
	{
		return (isEnabled ? textures[state] : textures[2]);
	}
	
	public void render(Texture drawTo)
	{
		drawTo.draw(getTexture(), hitbox.x, hitbox.y);
	}
}
