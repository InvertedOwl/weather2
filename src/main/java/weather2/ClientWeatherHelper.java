package weather2;

import com.corosus.coroutil.util.CULog;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.config.ConfigMisc;
import weather2.config.ConfigStorm;
import weather2.weathersystem.storm.CloudDefinition;
import weather2.weathersystem.storm.StormObject;
import net.minecraft.network.chat.Component;
import weather2.weathersystem.storm.WeatherObject;

import java.awt.*;
import java.util.List;

import static weather2.util.WeatherUtilRender.map;

/**
 * Moving client weather logic to here from scene enhancer.
 * Also when LT isnt in control of the weather, logic redirects to here for weather2s more special rules
 */
public final class ClientWeatherHelper {
	private static ClientWeatherHelper instance;

	private float curPrecipStr = 0F;
	private float curPrecipStrTarget = 0F;

	private float curOvercastStr = 0F;
	private float curOvercastStrTarget = 0F;

	private ClientWeatherHelper() {
	}

	public static ClientWeatherHelper get() {
		if (instance == null) {
			instance = new ClientWeatherHelper();
		}
		return instance;
	}

	public void reset() {
		instance.curPrecipStr = 0F;
		instance.curPrecipStrTarget = 0F;

		instance.curOvercastStr = 0F;
		instance.curOvercastStrTarget = 0F;
	}

	public void tick() {
		tickRainRates();
	}

	public float getPrecipitationStrength(Player entP) {
		return getPrecipitationStrength(entP, false);
	}

	/**
	 * returns 0 to 1 of storm strength
	 *
	 * @param entP
	 * @param forOvercast
	 * @return
	 */
	// TODO: Edit this so that it grabs the correct precip strength
	// TODO: for now just proportional to the storms strength (I know this isn't necessarily true, but for simplicity its fine)
	public float getPrecipitationStrength(Player entP, boolean forOvercast) {

		if (entP == null) return 0;
		double maxStormDist = 512 / 4 * 3;
		Vec3 plPos = new Vec3(entP.getX(), StormObject.static_YPos_layer0, entP.getZ());
		WeatherObject storm;

		ClientTickHandler.getClientWeather();

		storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, -1, -1, false);

		boolean closeEnough = false;
		double stormDist;
		float tempAdj = 1F;

		float overcastModeMinPrecip;
		overcastModeMinPrecip = ClientTickHandler.weatherManager.vanillaRainAmountOnServer;

		//evaluate if player is even under storm
		if (storm != null) {

			stormDist = storm.distanceToEdge(plPos);
			if (stormDist < 0) {
				closeEnough = true;
			}
		}

		if (closeEnough) {
			//max of 1 if at center of storm, subtract player xz distance out of the size to act like its a weaker storm

//			double stormIntensity = 1-Math.exp(-Math.abs(stormDist)/10);
//			double stormIntensity = (sizeToUse - stormDist) / sizeToUse;
			List<CloudDefinition> cloudDefinitions = storm.getRectanglesContainingPoint(entP.position().x - storm.pos.x, entP.position().z - storm.pos.z);

			double stormIntensity = 0;
			for (CloudDefinition cloud : cloudDefinitions) {
				if (cloud.intensity > stormIntensity) {
					stormIntensity = cloud.intensity;
				}
			}

			if (stormIntensity > 20) {
//				map(stormIntensity, 20, 100, 0, 100);
			} else {
				stormIntensity = 0;
			}

			stormIntensity /= 100;

			//why is this not a -1 or 1 anymore?!

            // TODO: Set intensity stage based on max intensity
			// TODO: Also this code specifically here doesn't need to stay
			//limit plain rain clouds to light intensity
//			if (storm.levelCurIntensityStage == StormObject.STATE_NORMAL) {
//				if (stormIntensity > 0.3) stormIntensity = 0.3;
//			}

			if (ConfigStorm.Storm_NoRainVisual) {
				stormIntensity = 0;
			}

			//TODO: verify this if statement was added correctly
			if (forOvercast) {
				if (stormIntensity < overcastModeMinPrecip) {
					stormIntensity = overcastModeMinPrecip;
				}
			}
			if (forOvercast) {
				curOvercastStrTarget = (float) stormIntensity;
			} else {
				curPrecipStrTarget = (float) stormIntensity;
			}
		} else {
			if (!ClientTickHandler.clientConfigData.overcastMode) {
				if (forOvercast) {
					curOvercastStrTarget = 0;
				} else {
					curPrecipStrTarget = 0;
				}
			} else {
				if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
					if (forOvercast) {
						curOvercastStrTarget = overcastModeMinPrecip;
					} else {
						curPrecipStrTarget = overcastModeMinPrecip;
					}
				} else {
					if (forOvercast) {
						curOvercastStrTarget = 0;
					} else {
						curPrecipStrTarget = 0;
					}
				}
			}
		}

		if (forOvercast) {
			if (curOvercastStr < 0.002 && curOvercastStr > -0.002F) {
				return 0;
			} else {
				return curOvercastStr * tempAdj;
			}
		} else {
			if (curPrecipStr < 0.002 && curPrecipStr > -0.002F) {
				return 0;
			} else {
				return curPrecipStr * tempAdj;
			}
		}
	}

	public void controlVisuals(boolean precipitating) {
		Minecraft mc = Minecraft.getInstance();
		ClientTickHandler.getClientWeather();
		ClientWeatherProxy weather = ClientWeatherProxy.get();
		float rainAmount = weather.getVanillaRainAmount();
		float visualDarknessAmplifier = 0.5F;
		//using 1F to make shaders happy
		visualDarknessAmplifier = 1F;
		if (!ConfigMisc.Aesthetic_Only_Mode) {
			if (precipitating) {
				mc.level.getLevelData().setRaining(rainAmount > 0);
				mc.level.setRainLevel(rainAmount * visualDarknessAmplifier);
				mc.level.setThunderLevel(rainAmount * visualDarknessAmplifier);

			} else {
				//TODO: i think these glitch out and trigger on world load if it was already raining, will think its false for a sec and lock sky visual to off
				if (!ClientTickHandler.clientConfigData.overcastMode) {
					mc.level.getLevelData().setRaining(false);
					mc.level.setRainLevel(0);
					mc.level.setThunderLevel(0);
				} else {
					if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
						mc.level.getLevelData().setRaining(true);
						mc.level.setRainLevel(rainAmount * visualDarknessAmplifier);
						mc.level.setThunderLevel(rainAmount * visualDarknessAmplifier);
					} else {

					}
				}
			}
		}
	}

	public void tickRainRates() {

		float rateChange = 0.0015F;

		if (curOvercastStr > curOvercastStrTarget) {
			curOvercastStr -= rateChange;
		} else if (curOvercastStr < curOvercastStrTarget) {
			curOvercastStr += rateChange;
		}

		if (curPrecipStr > curPrecipStrTarget) {
			curPrecipStr -= rateChange;
		} else if (curPrecipStr < curPrecipStrTarget) {
			curPrecipStr += rateChange;
		}
	}
}
