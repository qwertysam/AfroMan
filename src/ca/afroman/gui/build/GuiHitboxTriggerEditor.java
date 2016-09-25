package ca.afroman.gui.build;

import java.util.ArrayList;
import java.util.List;

import ca.afroman.assets.Texture;
import ca.afroman.client.ClientGame;
import ca.afroman.events.HitboxTrigger;
import ca.afroman.events.TriggerType;
import ca.afroman.gui.GuiScreen;
import ca.afroman.gui.GuiTextButton;
import ca.afroman.gui.GuiTextField;
import ca.afroman.input.TypingMode;
import ca.afroman.level.ClientLevel;
import ca.afroman.level.LevelObjectType;
import ca.afroman.packet.PacketEditTrigger;
import ca.afroman.packet.PacketRemoveLevelObject;
import ca.afroman.resource.Vector2DInt;
import ca.afroman.util.ArrayUtil;

public class GuiHitboxTriggerEditor extends GuiScreen
{
	private GuiTextField triggers;
	private GuiTextField inTriggers;
	private GuiTextField outTriggers;
	
	private GuiTextButton finish;
	private GuiTextButton cancel;
	private GuiTextButton delete;
	
	private ClientLevel level;
	private int triggerID;
	
	public GuiHitboxTriggerEditor(ClientLevel level, int triggerID)
	{
		super(null);
		
		this.level = level;
		this.triggerID = triggerID;
		
		int width = (ClientGame.WIDTH - 40);
		
		HitboxTrigger trigger = (HitboxTrigger) level.getScriptedEvent(triggerID);
		
		StringBuilder sb = new StringBuilder();
		
		for (TriggerType t : trigger.getTriggerTypes())
		{
			sb.append(t.ordinal());
			sb.append(',');
		}
		
		triggers = new GuiTextField(this, 20, 28, width);
		triggers.setFocussed();
		triggers.setText(sb.toString());
		triggers.setMaxLength(5000);
		triggers.setTypingMode(TypingMode.ONLY_NUMBERS_AND_COMMA);
		addButton(triggers);
		
		StringBuilder sb2 = new StringBuilder();
		
		for (int e : trigger.getInTriggers())
		{
			sb2.append(e);
			sb2.append(',');
		}
		
		inTriggers = new GuiTextField(this, 20, 58, width);
		inTriggers.setText(sb2.toString());
		inTriggers.setTypingMode(TypingMode.ONLY_NUMBERS_AND_COMMA);
		inTriggers.setMaxLength(5000);
		addButton(inTriggers);
		
		StringBuilder sb3 = new StringBuilder();
		
		for (int e : trigger.getOutTriggers())
		{
			sb3.append(e);
			sb3.append(',');
		}
		
		outTriggers = new GuiTextField(this, 20, 88, width);
		outTriggers.setText(sb3.toString());
		outTriggers.setTypingMode(TypingMode.ONLY_NUMBERS_AND_COMMA);
		outTriggers.setMaxLength(5000);
		addButton(outTriggers);
		
		cancel = new GuiTextButton(this, 201, (ClientGame.WIDTH / 2) + 8, 112, 84, blackFont, "Cancel");
		delete = new GuiTextButton(this, 202, (ClientGame.WIDTH / 2) + 46, 6, 54, blackFont, "Delete");
		finish = new GuiTextButton(this, 200, (ClientGame.WIDTH / 2) - 84 - 8, 112, 84, blackFont, "Finished");
		
		addButton(cancel);
		addButton(delete);
		addButton(finish);
		keyTyped();
	}
	
	@Override
	public void drawScreen(Texture renderTo)
	{
		nobleFont.render(renderTo, new Vector2DInt(36, 18), "Trigger Types");
		nobleFont.render(renderTo, new Vector2DInt(36, 48), "In Triggers");
		nobleFont.render(renderTo, new Vector2DInt(36, 78), "Out Triggers");
	}
	
	@Override
	public void keyTyped()
	{
		boolean finished = true;
		
		{
			String[] trigs = this.triggers.getText().split(",");
			
			if (!ArrayUtil.isEmpty(trigs))
			{
				for (String t : trigs)
				{
					try
					{
						int ord = Integer.parseInt(t);
						if (TriggerType.fromOrdinal(ord) == null)
						{
							finished = false;
							break;
						}
					}
					catch (NumberFormatException e)
					{
						finished = false;
						break;
					}
				}
			}
		}
		
		if (finished)
		{
			String[] trigs = this.inTriggers.getText().split(",");
			if (!ArrayUtil.isEmpty(trigs))
			{
				for (String t : trigs)
				{
					try
					{
						Integer.parseInt(t);
					}
					catch (NumberFormatException e)
					{
						finished = false;
						break;
					}
				}
			}
		}
		
		if (finished)
		{
			String[] trigs = this.outTriggers.getText().split(",");
			if (!ArrayUtil.isEmpty(trigs))
			{
				for (String t : trigs)
				{
					try
					{
						Integer.parseInt(t);
					}
					catch (NumberFormatException e)
					{
						finished = false;
						break;
					}
				}
			}
		}
		
		finish.setEnabled(finished);
	}
	
	@Override
	public void pressAction(int buttonID)
	{
		// Rids of the click so that the Level doesn't get it
		ClientGame.instance().input().mouseLeft.isPressedFiltered();
		
		switch (buttonID)
		{
			case 202:
				ClientGame.instance().sockets().sender().sendPacket(new PacketRemoveLevelObject(triggerID, level.getType(), LevelObjectType.HITBOX_TRIGGER));
			case 201:
				goToParentScreen();
				break;
			case 200:
				String[] trigs = this.triggers.getText().split(",");
				List<TriggerType> triggers = new ArrayList<TriggerType>(trigs.length);
				if (!ArrayUtil.isEmpty(trigs))
				{
					for (String t : trigs)
					{
						triggers.add(TriggerType.fromOrdinal(Integer.parseInt(t)));
					}
				}
				
				String[] inTrigs = this.inTriggers.getText().split(",");
				List<Integer> inTriggers = new ArrayList<Integer>(inTrigs.length);
				if (!ArrayUtil.isEmpty(inTrigs))
				{
					for (String t : inTrigs)
					{
						inTriggers.add(Integer.parseInt(t));
					}
				}
				
				String[] outTrigs = this.outTriggers.getText().split(",");
				List<Integer> outTriggers = new ArrayList<Integer>(outTrigs.length);
				if (!ArrayUtil.isEmpty(outTrigs))
				{
					for (String t : outTrigs)
					{
						outTriggers.add(Integer.parseInt(t));
					}
				}
				
				PacketEditTrigger pack = new PacketEditTrigger(level.getType(), triggerID, triggers, inTriggers, outTriggers);
				ClientGame.instance().sockets().sender().sendPacket(pack);
				goToParentScreen();
				break;
		}
	}
	
	@Override
	public void releaseAction(int buttonID)
	{
		
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if (ClientGame.instance().input().escape.isPressedFiltered())
		{
			goToParentScreen();
		}
		
		if (ClientGame.instance().input().tab.isPressedFiltered())
		{
			if (triggers.isFocussed())
			{
				inTriggers.setFocussed();
			}
			else if (inTriggers.isFocussed())
			{
				outTriggers.setFocussed();
			}
			else
			{
				triggers.setFocussed();
			}
		}
	}
}
