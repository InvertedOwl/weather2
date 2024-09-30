package weather2.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import weather2.Weather;

public class PlayerData {

	public static HashMap<String, CompoundTag> playerNBT = new HashMap<>();
	
	public static CompoundTag getPlayerNBT(String username) {
		if (!playerNBT.containsKey(username)) {
			playerNBT.put(username, new CompoundTag());
		}
		return playerNBT.get(username);
	}
}
