package weather2.weathersystem.storm;

import java.awt.geom.Rectangle2D;

public class CloudDefinition {
    public Rectangle2D bounds;
    public int intensity;

    public CloudDefinition(Rectangle2D bounds, int intensity) {
        this.bounds = bounds;
        this.intensity = intensity;
    }
}
