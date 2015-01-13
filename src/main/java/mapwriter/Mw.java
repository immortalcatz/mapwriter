package mapwriter;

import cpw.mods.fml.common.FMLLog;
import mapwriter.forge.MwConfig;
import mapwriter.forge.MwKeyHandler;
import mapwriter.gui.MwGui;
import mapwriter.gui.MwGuiMarkerDialog;
import mapwriter.map.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import mapwriter.gui.MapView;
import mapwriter.gui.MiniMap;
import mapwriter.mapgen.ChunkManager;
import mapwriter.map.RegionManager;
import mapwriter.util.PriorityThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mw {

  public static final ScheduledExecutorService backgroundExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new PriorityThreadFactory(Thread.MIN_PRIORITY));
  public static final Logger log = LogManager.getLogger("MapWriter");

  public Minecraft mc = null;

  // server information
  public String worldName = "default";
  private String serverName = "default";
  private int serverPort = 0;

  // configuration files (global and world specific)
  public MwConfig worldConfig = null;

  // directories
  public File worldDir = null;
  public File imageDir = null;

  //public boolean lightingEnabled = false;
  // flags and counters
  private boolean onPlayerDeathAlreadyFired = false;
  public boolean initialized = false;
  public boolean multiplayer = false;
  protected long tickCounter = 0;

  // list of available dimensions
  public List<Integer> dimensionList = new ArrayList<Integer>();

  // player position and heading
  public final PlayerStatus player = new PlayerStatus();
  protected double mapRotationDegrees = 0.0;

  // constants
  public final static String catWorld = "world";
  public final static String catMarkers = "markers";
  public final static String catOptions = "options";
  public final static String worldDirConfigName = "mapwriter.cfg";
  public final static String blockColourSaveFileName = "MapWriterBlockColours.txt";
  public final static String blockColourOverridesFileName = "MapWriterBlockColourOverrides.txt";

  // instances of components
  public MarkerManager markerManager;
  public RegionManager regionManager;
  public ChunkManager chunkManager = new ChunkManager();
  public final MiniMap miniMap = new MiniMap();

  public static final Mw instance = new Mw();

  public Mw() {
    // client only initialization
    // oops, no idea why I was using a ModLoader method to get the Minecraft instance before
    this.mc = Minecraft.getMinecraft();

    this.initialized = false;
  }

  public String getWorldName() {
    String result;
    if (this.multiplayer) {
      if (Config.instance.portNumberInWorldNameEnabled) {
        result = String.format("%s_%d", this.serverName, this.serverPort);
      } else {
        result = String.format("%s", this.serverName);
      }
    } else {
      // cannot use this.mc.theWorld.getWorldInfo().getWorldName() as it
      // is set statically to "MpServer".
      IntegratedServer server = this.mc.getIntegratedServer();
      result = (server != null) ? server.getFolderName() : "sp_world";
    }

    // strip invalid characters from the server name so that it
    // can't be something malicious like '..\..\..\windows\'
    result = MwUtil.mungeString(result);

    // if something went wrong make sure the name is not blank
    // (causes crash on start up due to empty configuration section)
    if ("".equals(result)) {
      result = "default";
    }
    return result;
  }

  public void loadWorldConfig() {
    // load world specific config file
    File worldConfigFile = new File(this.worldDir, worldDirConfigName);
    this.worldConfig = new MwConfig(worldConfigFile);
    this.worldConfig.load();

    this.dimensionList.clear();
    this.worldConfig.getIntList(catWorld, "dimensionList", this.dimensionList);
    this.addDimension(0);
    this.cleanDimensionList();
  }

  public void saveWorldConfig() {
    this.worldConfig.setIntList(catWorld, "dimensionList", this.dimensionList);
    this.worldConfig.save();
  }

  public void setTextureSize() {
    final int maxTextureSize = Render.getMaxTextureSize();
    int newTextureSize = 1024;
    while ((newTextureSize <= maxTextureSize) && (newTextureSize <= Config.instance.configTextureSize)) {
      newTextureSize <<= 1;
    }
    newTextureSize >>= 1;

    Config.instance.configTextureSize = newTextureSize;
    if (this.initialized) {
      // if we are already up and running need to close and reinitialize the map texture and
      // region manager.
      this.reloadMapTexture();
    }
  }

  public void addDimension(int dimension) {
    int i = this.dimensionList.indexOf(dimension);
    if (i < 0) {
      this.dimensionList.add(dimension);
    }
  }

  public void cleanDimensionList() {
    List<Integer> dimensionListCopy = new ArrayList<Integer>(this.dimensionList);
    this.dimensionList.clear();
    for (int dimension : dimensionListCopy) {
      this.addDimension(dimension);
    }
  }

  public int nextDimension(final int index) {
    final int next = index + 1;
    if (next < this.dimensionList.size()) {
      return this.dimensionList.get(next);
    } else {
      return this.dimensionList.get(0);
    }
  }

  public void toggleMarkerMode() {
    this.markerManager.nextGroup();
    this.markerManager.update();
    this.mc.thePlayer.addChatMessage(new ChatComponentText("group " + this.markerManager.getVisibleGroupName() + " selected"));
  }

  // cheap and lazy way to teleport...
  public void teleportTo(int x, int y, int z) {
    if (Config.instance.teleportEnabled) {
      this.mc.thePlayer.sendChatMessage(String.format("/%s %d %d %d", Config.instance.teleportCommand, x, y, z));
    } else {
      MwUtil.printBoth("teleportation is disabled in mapwriter.cfg");
    }
  }

  public void warpTo(String name) {
    if (Config.instance.teleportEnabled) {
      //MwUtil.printBoth(String.format("warping to %s", name));
      this.mc.thePlayer.sendChatMessage(String.format("/warp %s", name));
    } else {
      MwUtil.printBoth("teleportation is disabled in mapwriter.cfg");
    }
  }

  public void teleportToMapPos(MapView mapView, int x, int y, int z) {
    if (!Config.instance.teleportCommand.equals("warp")) {
      double scale = 1.0;
      this.teleportTo((int) (x / scale), y, (int) (z / scale));
    } else {
      MwUtil.printBoth("teleport command is set to 'warp', can only warp to markers");
    }
  }

  public void teleportToMarker(Marker marker) {
    if (Config.instance.teleportCommand.equals("warp")) {
      this.warpTo(marker.name);
    } else if (marker.dimension == this.player.dimensionID) {
      this.teleportTo(marker.x, marker.y, marker.z);
    } else {
      MwUtil.printBoth("cannot teleport to marker in different dimension");
    }
  }

  public void reloadMapTexture() {
    if (this.regionManager != null) {
      regionManager.saveAll();
    }
  }

  public void setCoordsMode(int mode) {
    Config.instance.coordsMode = Math.min(Math.max(0, mode), 2);
  }

  public int toggleCoords() {
    this.setCoordsMode((Config.instance.coordsMode + 1) % 3);
    return Config.instance.coordsMode;
  }

  public void toggleUndergroundMode() {
  }

  public void setServerDetails(String hostname, int port) {
    this.serverName = hostname;
    this.serverPort = port;
  }

  ////////////////////////////////
  // Initialization and Cleanup
  ////////////////////////////////
  protected void load() {
    if ((this.mc.theWorld == null) || (this.mc.thePlayer == null)) {
      log.info("Mw.load: world or player is null, cannot load yet");
      return;
    }

    log.info("Mw.load: loading...");

    IntegratedServer server = this.mc.getIntegratedServer();
    this.multiplayer = (server == null);
    this.worldName = this.getWorldName();

    // get world and image directories
    File actualSaveDir = new File(this.mc.mcDataDir, "saves");
    if (Config.instance.saveDirOverride.length() > 0) {
      File d = new File(Config.instance.saveDirOverride);
      if (d.isDirectory()) {
        actualSaveDir = d;
      } else {
        log.error("Savedir override does not exist: " + Config.instance.saveDirOverride);
      }
    }

    if (this.multiplayer) {
      this.worldDir = new File(new File(actualSaveDir, "mapwriter_mp_worlds"), this.worldName);
    } else {
      this.worldDir = new File(new File(actualSaveDir, "mapwriter_sp_worlds"), this.worldName);
    }

    this.loadWorldConfig();

    // create directories
    this.imageDir = new File(this.worldDir, "images");
    if (!this.imageDir.exists()) {
      this.imageDir.mkdirs();
    }
    if (!this.imageDir.isDirectory()) {
      log.error("Could not create image directory: ", this.imageDir.getPath());
    }

    this.tickCounter = 0;
    this.onPlayerDeathAlreadyFired = false;

//    ColorConvert.reset(); // will as well fetch Minecraft block texture, which has to be done from the main thread
    //this.multiplayer = !this.mc.isIntegratedServerRunning();
    // marker manager only depends on the config being loaded
    this.markerManager = new MarkerManager(this.worldConfig, catMarkers);
    this.markerManager.load();

    // region manager depends on config, mapTexture, and block colours
    this.regionManager = new RegionManager(this.imageDir.toString());

    this.chunkManager = new ChunkManager();

    this.initialized = true;

    this.chunkManager.start();
    this.regionManager.start();
  }

  protected void terminateExecutor() {
    backgroundExecutor.shutdown();
    try {
      if (backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS) == false) {
        final List<Runnable> remainingTasks = backgroundExecutor.shutdownNow();
        FMLLog.bigWarning("Unable to terminate remaining " + remainingTasks.size() + " tasks. Data may be lost!");
      }
    } catch (InterruptedException e) {
      // whatever, do it yourself JVM!
    }
  }

  public void close() {

    log.info("Mw.close: closing...");

    if (this.initialized) {
      this.initialized = false;
      this.chunkManager.stop();
      this.regionManager.stop();

      this.chunkManager.removeAll();

      if (this.regionManager != null) {
        regionManager.dispose();
      }
      this.regionManager = null;

      this.markerManager.save();
      this.markerManager = null;

      this.saveWorldConfig();
      Config.instance.save();

      this.tickCounter = 0;
    }
  }

  ////////////////////////////////
  // Event handlers
  ////////////////////////////////
  public void onWorldLoad(final World world) {
    //MwUtil.log("onWorldLoad: %s, name %s, dimension %d",
    //		world,
    //		world.getWorldInfo().getWorldName(),
    //		world.provider.dimensionId);

    this.player.dimensionID = world.provider.dimensionId;
    if (this.initialized) {
      this.addDimension(this.player.dimensionID);
    }
  }

  public void onWorldUnload(World world) {
    //MwUtil.log("onWorldUnload: %s, name %s, dimension %d",
    //		world,
    //		world.getWorldInfo().getWorldName(),
    //		world.provider.dimensionId);
  }

  public void onTick() {
    if (this.initialized == false) {
      this.load();
      chunkManager.start();
      FMLLog.info("GUITick: initialized");
    }

    this.regionManager.tick();

    if (this.mc.thePlayer != null) {

      this.player.update();
      this.mapRotationDegrees = -this.mc.thePlayer.rotationYaw + 180;

      // check if the game over screen is being displayed and if so 
      // (thanks to Chrixian for this method of checking when the player is dead)
      if (this.mc.currentScreen instanceof GuiGameOver) {
        if (!this.onPlayerDeathAlreadyFired) {
          this.onPlayerDeath();
          this.onPlayerDeathAlreadyFired = true;
        }
      } else if (!(this.mc.currentScreen instanceof MwGui)) {
        // if the player is not dead
        this.onPlayerDeathAlreadyFired = false;
      }

      ++this.tickCounter;
    }
  }

  // add chunk to the set of loaded chunks
  public void onChunkLoad(Chunk chunk) {
    if (this.initialized == false) {
      this.load();
    }
    if ((chunk != null) && (chunk.worldObj instanceof net.minecraft.client.multiplayer.WorldClient)) {
      this.chunkManager.addChunk(chunk);
    }
  }

  // remove chunk from the set of loaded chunks.
  // convert to mwchunk and write chunk to region file if in multiplayer.
  public void onChunkUnload(Chunk chunk) {
    if (this.initialized && (chunk != null) && (chunk.worldObj instanceof net.minecraft.client.multiplayer.WorldClient)) {
      this.chunkManager.removeChunk(chunk);
    }
  }

  // from onTick when mc.currentScreen is an instance of GuiGameOver
  // it's the only option to detect death client side
  public void onPlayerDeath() {
    if (this.initialized && (Config.instance.maxDeathMarkers > 0)) {
      this.player.update();
      this.mapRotationDegrees = -this.mc.thePlayer.rotationYaw + 180;
      int deleteCount = this.markerManager.countMarkersInGroup("playerDeaths") - Config.instance.maxDeathMarkers + 1;
      for (int i = 0; i < deleteCount; i++) {
        // delete the first marker found in the group "playerDeaths".
        // as new markers are only ever appended to the marker list this will delete the
        // earliest death marker added.
        this.markerManager.delMarker(null, "playerDeaths");
      }
      this.markerManager.addMarker(MwUtil.getCurrentDateString(), "playerDeaths", this.player.xInt, this.player.yInt, this.player.zInt, this.player.dimensionID, 0xffff0000);
      this.markerManager.setVisibleGroupName("playerDeaths");
      this.markerManager.update();
    }
  }

  public void onKeyDown(KeyBinding kb) {
    // make sure not in GUI element (e.g. chat box)
    if ((this.mc.currentScreen == null) && (this.initialized)) {
      //Mw.log("client tick: %s key pressed", kb.keyDescription);

      if (kb == MwKeyHandler.keyMapMode) {
        // toggle map display from minimap to full or back
      } else if (kb == MwKeyHandler.keyMapGui) {
        // open map gui
        this.mc.displayGuiScreen(new MwGui());

      } else if (kb == MwKeyHandler.keyNewMarker) {
        // open new marker dialog
        String group = this.markerManager.getVisibleGroupName();
        if (group.equals("none")) {
          group = "group";
        }
        this.mc.displayGuiScreen(
                new MwGuiMarkerDialog(
                        null,
                        this.markerManager,
                        "",
                        group,
                        this.player
                )
        );

      } else if (kb == MwKeyHandler.keyNextGroup) {
        // toggle marker mode
        this.markerManager.nextGroup();
        this.markerManager.update();
        this.mc.thePlayer.addChatMessage(new ChatComponentText("group " + this.markerManager.getVisibleGroupName() + " selected"));

      } else if (kb == MwKeyHandler.keyTeleport) {
        // set or remove marker
        Marker marker = this.markerManager.getNearestMarkerInDirection(this.player);
        if (marker != null) {
          this.teleportToMarker(marker);
        }
      } else if (kb == MwKeyHandler.keyZoomIn) {
        // zoom in
      } else if (kb == MwKeyHandler.keyZoomOut) {
        // zoom out
      } else if (kb == MwKeyHandler.keyUndergroundMode) {
        this.toggleUndergroundMode();
      }
    }
  }

  public void onWorldSave(final World world) {
    if (this.regionManager != null) {
      regionManager.saveAll();
    }
  }

  public RegionManager getRegionManager(final World world) {
    return getRegionManager(world.provider.dimensionId);
  }

  public RegionManager getRegionManager(final int dimensionID) {
    return this.regionManager;
  }

  /**
   * @return the tickCounter
   */
  public long getTickCounter() {
    return tickCounter;
  }

  /**
   * @return the mapRotationDegrees
   */
  public double getMapRotationDegrees() {
    return mapRotationDegrees;
  }
}
