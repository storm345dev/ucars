package com.useful.ucars;

import org.bukkit.block.BlockFace;

public class ClosestFace {
	public static BlockFace getClosestFace(float direction) {
		// direction is the players location.getYaw();
		direction = direction % 360;

		if (direction < 0)
			direction += 360;
		direction = Math.round(direction / 45);
		switch ((int) direction) {

		case 0:
			return BlockFace.SOUTH;
		case 1:
			return BlockFace.SOUTH_WEST;
		case 2:
			return BlockFace.WEST;
		case 3:
			return BlockFace.NORTH_WEST;
		case 4:
			return BlockFace.NORTH;
		case 5:
			return BlockFace.NORTH_EAST;
		case 6:
			return BlockFace.EAST;
		case 7:
			return BlockFace.SOUTH_EAST;
		case 8:
			return BlockFace.SOUTH;
		default:
			return BlockFace.SOUTH;

		}

	}
}
