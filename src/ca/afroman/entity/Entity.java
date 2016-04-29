package ca.afroman.entity;

import java.awt.Rectangle;

public abstract class Entity extends LevelObject
{
	protected int speed;
	protected final int originalSpeed;
	protected int numSteps = 0;
	protected Direction direction = Direction.NONE;
	protected Direction lastDirection = direction;
	protected boolean cameraFollow = false;
	
	public Entity(int x, int y, int speed, Rectangle... hitboxes)
	{
		super(x, y, hitboxes);
		this.speed = speed;
		this.originalSpeed = speed;
	}
	
	/**
	 * Makes the camera in the level follow this Entity.
	 * 
	 * @param follow
	 */
	public void setCameraToFollow(boolean follow)
	{
		cameraFollow = follow;
	}
	
	@Override
	public void tick()
	{
		if (cameraFollow)
		{
			level.setCameraCenterInWorld(x + (width / 2), y + (height / 2));
		}
	}
	
	@Override
	public void addToLevel(Level level)
	{
		this.level = level;
		level.addEntity(this);
	}
	
	public void move(int xa, int ya)
	{
		/*
		 * s
		 * Does each component separately
		 * if (xa != 0 && ya != 0)
		 * {
		 * move(xa, 0);
		 * move(0, ya);
		 * }
		 */
		
		if (xa == 0 && ya == 0)
		{
			direction = Direction.NONE;
			return;
		}
		
		numSteps++;
		
		// Moves the obejct
		int deltaX = xa * speed;
		int deltaY = ya * speed;
		
		// Tests if it can move in the x
		{
			x += deltaX;
			
			// Tests if it's allowed to move or not
			boolean canMove = true;
			for (LevelObject object : level.getEntities())
			{
				// Don't let it collide with itself
				if (object != this && this.isColliding(object))
				{
					canMove = false;
					break;
				}
			}
			
			if (canMove) // Only do the next calculations if it has not yet determined that this Entity can't move
			{
				for (LevelObject object : level.getTiles())
				{
					// Don't let it collide with itself
					if (object != this && this.isColliding(object))
					{
						canMove = false;
						break;
					}
				}
			}
			
			if (canMove) // Only do the next calculations if it has not yet determined that this Entity can't move
			{
				for (Rectangle hitbox : level.getLevelHitboxes())
				{
					if (this.isColliding(hitbox))
					{
						canMove = false;
						break;
					}
				}
			}
			
			if (!canMove)
			{
				x -= deltaX;
				deltaX = 0;
			}
		}
		
		// Tests if it can move Y
		{
			y += deltaY;
			
			// Tests if it's allowed to move or not
			boolean canMove = true;
			for (LevelObject object : level.getEntities())
			{
				// Don't let it collide with itself
				if (object != this && this.isColliding(object))
				{
					canMove = false;
					break;
				}
			}
			
			if (canMove) // Only do the next calculations if it has not yet determined that this Entity can't move
			{
				for (LevelObject object : level.getTiles())
				{
					// Don't let it collide with itself
					if (object != this && this.isColliding(object))
					{
						canMove = false;
						break;
					}
				}
			}
			
			if (canMove) // Only do the next calculations if it has not yet determined that this Entity can't move
			{
				for (Rectangle hitbox : level.getLevelHitboxes())
				{
					if (this.isColliding(hitbox))
					{
						canMove = false;
						break;
					}
				}
			}
			
			if (!canMove)
			{
				y -= deltaY;
				deltaY = 0;
			}
		}
		
		if (direction != Direction.NONE) lastDirection = direction;
		
		// If not allowed to move
		if (deltaX == 0 && deltaY == 0)
		{
			// Change the last direction so this entity faces in the direction that it tried to move in
			
			direction = Direction.NONE;
			
			if (ya < 0) lastDirection = Direction.UP;
			if (ya > 0) lastDirection = Direction.DOWN;
			if (xa < 0) lastDirection = Direction.LEFT;
			if (xa > 0) lastDirection = Direction.RIGHT;
		}
		else
		{
			if (deltaY < 0) direction = Direction.UP;
			if (deltaY > 0) direction = Direction.DOWN;
			if (deltaX < 0) direction = Direction.LEFT;
			if (deltaX > 0) direction = Direction.RIGHT;
		}
	}
	
	public void setSpeed(int speed)
	{
		this.speed = speed;
	}
	
	public void resetSpeed()
	{
		speed = originalSpeed;
	}
	
	public boolean isMoving()
	{
		return direction != Direction.NONE;
	}
}