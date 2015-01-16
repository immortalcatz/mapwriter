/*
 */
package mapwriter;

import java.util.Objects;
import static mapwriter.Mw.catOptions;
import mapwriter.forge.MwConfig;
import mapwriter.gui.MapAnchor;

/**
 * @author Two
 */
public class Config {

  public static final Config instance = new Config();

  public boolean linearTextureScalingEnabled = true;
  public int coordsMode = 0;
  public boolean teleportEnabled = true;
  public String teleportCommand = "tp";
  public int defaultTeleportHeight = 80;
  public int maxZoom = 5;
  public int minZoom = -5;
  public boolean useSavedBlockColours = false;
  public int maxChunkSaveDistSq = 128 * 128;
  public boolean mapPixelSnapEnabled = true;
  public int textureSize = 2048;
  public int configTextureSize = 2048;
  public int maxDeathMarkers = 3;
  public int chunksPerTick = 5;
  public boolean portNumberInWorldNameEnabled = true;
  public String saveDirOverride = "";
  public boolean regionFileOutputEnabledSP = true;
  public boolean regionFileOutputEnabledMP = true;
  public int backgroundTextureMode = 0;
  public int mapMargin = 4;
  public int miniMapSize = 300;
  public MapAnchor miniMapAnchor = MapAnchor.center;

  protected MwConfig configFile;

  protected Config() {
  }

  public void setConfigFile(final MwConfig config) {
    Objects.requireNonNull(config);
    this.configFile = config;
    this.load();
  }

  public MwConfig getConfigFile() {
    return this.configFile;
  }

  public void load() {
    this.configFile.load();
    this.linearTextureScalingEnabled = this.configFile.getOrSetBoolean(catOptions, "linearTextureScaling", this.linearTextureScalingEnabled);
    this.useSavedBlockColours = this.configFile.getOrSetBoolean(catOptions, "useSavedBlockColours", this.useSavedBlockColours);
    this.teleportEnabled = this.configFile.getOrSetBoolean(catOptions, "teleportEnabled", this.teleportEnabled);
    this.teleportCommand = this.configFile.get(catOptions, "teleportCommand", this.teleportCommand).getString();
    this.coordsMode = this.configFile.getOrSetInt(catOptions, "coordsMode", this.coordsMode, 0, 2);
    this.maxChunkSaveDistSq = this.configFile.getOrSetInt(catOptions, "maxChunkSaveDistSq", this.maxChunkSaveDistSq, 1, 256 * 256);
    this.mapPixelSnapEnabled = this.configFile.getOrSetBoolean(catOptions, "mapPixelSnapEnabled", this.mapPixelSnapEnabled);
    this.maxDeathMarkers = this.configFile.getOrSetInt(catOptions, "maxDeathMarkers", this.maxDeathMarkers, 0, 1000);
    this.chunksPerTick = this.configFile.getOrSetInt(catOptions, "chunksPerTick", this.chunksPerTick, 1, 500);
    this.saveDirOverride = this.configFile.get(catOptions, "saveDirOverride", this.saveDirOverride).getString();
    this.portNumberInWorldNameEnabled = configFile.getOrSetBoolean(catOptions, "portNumberInWorldNameEnabled", this.portNumberInWorldNameEnabled);
    this.regionFileOutputEnabledSP = this.configFile.getOrSetBoolean(catOptions, "regionFileOutputEnabledSP", this.regionFileOutputEnabledSP);
    this.regionFileOutputEnabledMP = this.configFile.getOrSetBoolean(catOptions, "regionFileOutputEnabledMP", this.regionFileOutputEnabledMP);
    this.backgroundTextureMode = this.configFile.getOrSetInt(catOptions, "backgroundTextureMode", this.backgroundTextureMode, 0, 1);
    //this.lightingEnabled = this.config.getOrSetBoolean(catOptions, "lightingEnabled", this.lightingEnabled);

    this.maxZoom = this.configFile.getOrSetInt(catOptions, "zoomOutLevels", this.maxZoom, 1, 256);
    this.minZoom = -this.configFile.getOrSetInt(catOptions, "zoomInLevels", -this.minZoom, 1, 256);

    this.configTextureSize = this.configFile.getOrSetInt(catOptions, "textureSize", this.configTextureSize, 1024, 8192);
  }

  public void save() {
    this.configFile.setBoolean(catOptions, "linearTextureScaling", this.linearTextureScalingEnabled);
    this.configFile.setBoolean(catOptions, "useSavedBlockColours", this.useSavedBlockColours);
    this.configFile.setInt(catOptions, "textureSize", this.configTextureSize);
    this.configFile.setInt(catOptions, "coordsMode", this.coordsMode);
    this.configFile.setInt(catOptions, "maxChunkSaveDistSq", this.maxChunkSaveDistSq);
    this.configFile.setBoolean(catOptions, "mapPixelSnapEnabled", this.mapPixelSnapEnabled);
    this.configFile.setInt(catOptions, "maxDeathMarkers", this.maxDeathMarkers);
    this.configFile.setInt(catOptions, "chunksPerTick", this.chunksPerTick);
    this.configFile.setInt(catOptions, "backgroundTextureMode", this.backgroundTextureMode);
    //this.config.setBoolean(catOptions, "lightingEnabled", this.lightingEnabled);

    this.configFile.save();
  }

}
