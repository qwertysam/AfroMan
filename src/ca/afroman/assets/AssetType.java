package ca.afroman.assets;

public enum AssetType
{
	INVALID,
	
	SPRITESHEET,
	FONTSHEET,
	
	FILTER,
	
	FONT_BLACK,
	FONT_WHITE,
	FONT_NOBLE,
	
	RAW_PLAYER_ONE,
	PLAYER_ONE_UP,
	PLAYER_ONE_DOWN,
	PLAYER_ONE_LEFT,
	PLAYER_ONE_RIGHT,
	PLAYER_ONE_IDLE_UP,
	PLAYER_ONE_IDLE_DOWN,
	PLAYER_ONE_IDLE_LEFT,
	PLAYER_ONE_IDLE_RIGHT,
	
	RAW_PLAYER_TWO,
	PLAYER_TWO_UP,
	PLAYER_TWO_DOWN,
	PLAYER_TWO_LEFT,
	PLAYER_TWO_RIGHT,
	PLAYER_TWO_IDLE_UP,
	PLAYER_TWO_IDLE_DOWN,
	PLAYER_TWO_IDLE_LEFT,
	PLAYER_TWO_IDLE_RIGHT,
	
	TILE_GRASS,
	TILE_GRASS_INNER_TOPLEFT,
	TILE_GRASS_INNER_TOPRIGHT,
	TILE_GRASS_INNER_BOTTOMLEFT,
	TILE_GRASS_INNER_BOTTOMRIGHT,
	TILE_GRASS_OUTER_TOPLEFT,
	TILE_GRASS_OUTER_TOPRIGHT,
	TILE_GRASS_OUTER_BOTTOMLEFT,
	TILE_GRASS_OUTER_BOTTOMRIGHT,
	TILE_GRASS_OUTER_RIGHT,
	TILE_GRASS_OUTER_LEFT,
	TILE_GRASS_OUTER_BOTTOM,
	TILE_GRASS_OUTER_TOP,
	
	TILE_DIRT,
	
	TILE_WATER,
	
	TILE_WALL,
	TILE_WALL_GRASS,
	
	BUTTON_PRESSED,
	BUTTON_HOVER,
	BUTTON_NORMAL,
	
	TEXT_FIELD;
	
	public static AssetType fromOrdinal(int ordinal)
	{
		return AssetType.values()[ordinal];
	}
}
