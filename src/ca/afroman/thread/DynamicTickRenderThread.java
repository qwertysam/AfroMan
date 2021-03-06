package ca.afroman.thread;

public abstract class DynamicTickRenderThread extends DynamicTickThread
{
	protected int frames;
	protected int fps;
	
	private boolean tickSync = true;
	
	public DynamicTickRenderThread(boolean isServerSide, ThreadGroup group, String name, double ticksPerSecond)
	{
		super(isServerSide, group, name, ticksPerSecond);
	}
	
	public int getFramesPerSecond()
	{
		return fps;
	}
	
	/**
	 * Runs every time that the thread loops.
	 */
	// DO NOT INVOKE super.onRun()
	@Override
	public void onRun()
	{
		long now = System.nanoTime();
		delta += (now - lastTime) / nsPerTick;
		lastTime = now;
		boolean shouldRender = tickSync; // true for unlimited frames, false for limited to tick rate
		
		while (delta >= 1)
		{
			ticks++;
			tickCount++;
			tick();
			delta--;
			shouldRender = true;
		}
		
		// Only render when something has been updated
		if (!isServerSide() && shouldRender)
		{
			frames++;
			render();
		}
		
		// If current time - the last time we updated is >= 1 second
		if (System.currentTimeMillis() - lastTimer >= 1000)
		{
			tps = ticks;
			fps = frames;
			lastTimer += 1000;
			frames = 0;
			ticks = 0;
		}
	}
	
	public abstract void render();
	
	/**
	 * Tick sync syncs the render rate with the tick rate.
	 * 
	 * @param sync
	 */
	public void setTickSync(boolean sync)
	{
		tickSync = !sync;
	}
	
	@Override
	public void startThis()
	{
		frames = 0;
		fps = 0;
		
		super.startThis();
	}
}
