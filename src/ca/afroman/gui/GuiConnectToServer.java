package ca.afroman.gui;

import ca.afroman.assets.Texture;
import ca.afroman.client.ClientGame;
import ca.afroman.resource.Vector2DInt;
import ca.afroman.server.ServerSocketManager;

public class GuiConnectToServer extends GuiScreen
{
	private long startTime;
	private int millsPassed;
	
	public GuiConnectToServer(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void drawScreen(Texture renderTo)
	{
		blackFont.renderCentered(renderTo, new Vector2DInt(ClientGame.WIDTH / 2, 20), "Connecting to Server: " + ClientGame.instance().getServerIP() + (ClientGame.instance().getPort().length() > 0 ? ":" + ClientGame.instance().getPort() : ""));
		
		blackFont.renderCentered(renderTo, new Vector2DInt(ClientGame.WIDTH / 2, 45), "Waiting for server response");
		blackFont.renderCentered(renderTo, new Vector2DInt(ClientGame.WIDTH / 2, 55), "for " + (millsPassed / 1000) + " seconds...");
		
		blackFont.renderCentered(renderTo, new Vector2DInt(ClientGame.WIDTH / 2, 80), "If nothing happens for a while,");
		blackFont.renderCentered(renderTo, new Vector2DInt(ClientGame.WIDTH / 2, 90), "cancel and try rejoining.");
	}
	
	@Override
	public void init()
	{
		buttons.add(new GuiTextButton(this, 0, (ClientGame.WIDTH / 2) - (72 / 2), 110, 72, blackFont, "Cancel"));
		
		startTime = System.currentTimeMillis();
		millsPassed = 0;
	}
	
	@Override
	public void keyTyped()
	{
		
	}
	
	@Override
	public void pressAction(int buttonID)
	{
		
	}
	
	@Override
	public void releaseAction(int buttonID)
	{
		switch (buttonID)
		{
			case 0:
				goToParentScreen();
				ClientGame.instance().sockets().setServerIP(null, ServerSocketManager.DEFAULT_PORT);
				break;
		}
	}
	
	@Override
	public void tick()
	{
		millsPassed = (int) (System.currentTimeMillis() - startTime);
		
		super.tick();
	}
}
