package ca.afroman.gui;

import java.awt.Color;

import ca.afroman.Game;
import ca.afroman.assets.Assets;
import ca.afroman.assets.SpriteAnimation;
import ca.afroman.assets.Texture;
import ca.afroman.gfx.FlickeringLight;
import ca.afroman.gfx.LightMap;

public class GuiJoinServer extends GuiScreen
{
	private SpriteAnimation afroMan;
	private SpriteAnimation player2;
	private LightMap lightmap;
	private FlickeringLight light;
	
	private GuiTextField username;
	private GuiTextField serverIP;
	private GuiTextField password;
	
	private GuiTextButton joinButton;
	
	public GuiJoinServer(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void init()
	{
		afroMan = Assets.getSpriteAnimation(Assets.PLAYER_ONE_IDLE_DOWN);
		player2 = Assets.getSpriteAnimation(Assets.PLAYER_TWO_IDLE_DOWN);
		
		lightmap = new LightMap(Game.WIDTH, Game.HEIGHT, new Color(0F, 0F, 0F, 0.3F));
		light = new FlickeringLight(Game.WIDTH / 2, 38, 60, 62, 5);
		
		username = new GuiTextField(this, (Game.WIDTH / 2) - (112 / 2) - 57, 60 - 4);
		username.setText(game.getUsername());
		serverIP = new GuiTextField(this, (Game.WIDTH / 2) - (112 / 2) - 57, 90 - 6);
		serverIP.setMaxLength(64);
		serverIP.setText(game.getServerIP());
		password = new GuiTextField(this, (Game.WIDTH / 2) - (112 / 2) - 57, 120 - 8);
		password.setText(game.getPassword());
		
		buttons.add(username);
		buttons.add(serverIP);
		buttons.add(password);
		
		joinButton = new GuiTextButton(this, 1, 150, 62, Assets.getFont(Assets.FONT_BLACK), "Join Server");
		
		keyTyped();
		
		buttons.add(joinButton);
		buttons.add(new GuiTextButton(this, 200, 150, 90, Assets.getFont(Assets.FONT_BLACK), "Back"));
	}
	
	@Override
	public void drawScreen(Texture renderTo)
	{
		lightmap.clear();
		light.renderCentered(lightmap);
		lightmap.patch();
		
		renderTo.draw(lightmap, 0, 0);
		
		nobleFont.renderCentered(renderTo, Game.WIDTH / 2, 15, "Join a Server");
		
		blackFont.renderCentered(renderTo, Game.WIDTH / 2 - 57, 50 - 4, "Username");
		blackFont.renderCentered(renderTo, Game.WIDTH / 2 - 57, 80 - 6, "Server IP");
		blackFont.renderCentered(renderTo, Game.WIDTH / 2 - 57, 110 - 8, "Server Pass");
		
		renderTo.draw(afroMan.getCurrentFrame(), (Game.WIDTH / 2) - 20, 30);
		renderTo.draw(player2.getCurrentFrame(), (Game.WIDTH / 2) + 4, 30);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		light.tick();
		afroMan.tick();
		player2.tick();
		
		if (Game.instance().input.tab.isPressedFiltered())
		{
			if (username.isFocussed())
			{
				serverIP.setFocussed();
			}
			else if (serverIP.isFocussed())
			{
				password.setFocussed();
			}
			else
			{
				password.setFocussed(false);
			}
		}
	}
	
	@Override
	public void pressAction(int buttonID)
	{
		switch (buttonID)
		{
			
		}
	}
	
	@Override
	public void releaseAction(int buttonID)
	{
		switch (buttonID)
		{
			case 1: // Join Server
				Game.instance().setCurrentScreen(new GuiConnectToServer(this));
				break;
			case 200:
				Game.instance().setCurrentScreen(this.parentScreen);
				break;
		}
	}
	
	@Override
	public void keyTyped()
	{
		if (!this.username.getText().isEmpty() && !this.serverIP.getText().isEmpty())
		{
			this.joinButton.setEnabled(true);
		}
		else
		{
			this.joinButton.setEnabled(false);
		}
		
		game.setUsername(this.username.getText());
		game.setServerIP(this.serverIP.getText());
		game.setPassword(this.password.getText());
	}
}