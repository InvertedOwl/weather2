package weather2.ltcompat;

import weather2.datatypes.PrecipitationType;

public final class ClientWeatherIntegration {
	private static ClientWeatherIntegration instance = new ClientWeatherIntegration();

	private ClientWeatherIntegration() {
	}

	public static ClientWeatherIntegration get() {
		return instance;
	}

	public static void reset() {
		instance = new ClientWeatherIntegration();
	}

	public float getRainAmount() {
		return 0;
	}

	public float getVanillaRainAmount() {
		return 0;
	}

	public PrecipitationType getPrecipitationType() {
		return PrecipitationType.VALUES[0];
	}

	public float getWindSpeed() {
		return 0;
	}

	public boolean isHeatwave() {
		return false;
	}

	public boolean isSandstorm() {
		return false;
	}

	public boolean isSnowstorm() {
		return false;
	}

	public boolean hasWeather() {
		return false;
	}

}
