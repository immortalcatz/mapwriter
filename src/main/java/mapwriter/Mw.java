package mapwriter;

import cpw.mods.fml.common.FMLLog;
import mapwriter.forge.MwConfig;
import mapwriter.forge.MwKeyHandler;
import mapwriter.gui.MwGui;
import mapwriter.gui.MwGuiMarkerDialog;
import mapwriter.map.*;
import outdated.mapwriter.region.BlockColours;
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
import mapwriter.mapgen.ChunkManager;
import mapwriter.mapgen.RegionManager;
import mapwriter.util.PriorityThreadFactory;

public class Mw {

  public static final ScheduledExecutorService backgroundExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new PriorityThreadFactory(Thread.MIN_PRIORITY));

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
  public long tickCounter = 0;

  // list of available dimensions
  public List<Integer> dimensionList = new ArrayList<Integer>();

  // player position and heading
  public double playerX = 0.0;
  public double playerZ = 0.0;
  public double playerY = 0.0;
  public int playerXInt = 0;
  public int playerYInt = 0;
  public int playerZInt = 0;
  public double playerHeading = 0.0;
  public int playerDimension = 0;
  public double mapRotationDegrees = 0.0;

  // constants
  public final static String catWorld = "world";
  public final static String catMarkers = "markers";
  public final static String catOptions = "options";
  public final static String worldDirConfigName = "mapwriter.cfg";
  public final static String blockColourSaveFileName = "MapWriterBlockColours.txt";
  public final static String blockColourOverridesFileName = "MapWriterBlockColourOverrides.txt";

  // instances of components
  public MiniMap miniMap;
  public MarkerManager markerManager;
  public BlockColours blockColours;
  public RegionManager regionManager;
  public ChunkManager chunkManager = new ChunkManager();
  public Trail playerTrail;

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

    MwUtil.log("GL reported max texture size = %d", maxTextureSize);
    MwUtil.log("texture size from config = %d", Config.instance.configTextureSize);
    MwUtil.log("setting map texture size to = %d", newTextureSize);

    Config.instance.configTextureSize = newTextureSize;
    if (this.initialized) {
      // if we are already up and running need to close and reinitialize the map texture and
      // region manager.
      this.reloadMapTexture();
    }
  }

  // update the saved player position and orientation
  // called every tick
  public void updatePlayer() {
    // get player pos
    this.playerX = (double) this.mc.thePlayer.posX;
    this.playerY = (double) this.mc.thePlayer.posY;
    this.playerZ = (double) this.mc.thePlayer.posZ;
    this.playerXInt = (int) Math.floor(this.playerX);
    this.playerYInt = (int) Math.floor(this.playerY);
    this.playerZInt = (int) Math.floor(this.playerZ);

    // rotationYaw of 0 points due north, we want it to point due east instead
    // so add pi/2 radians (90 degrees)
    this.playerHeading = Math.toRadians(this.mc.thePlayer.rotationYaw) + (Math.PI / 2.0D);
    this.mapRotationDegrees = -this.mc.thePlayer.rotationYaw + 180;

    // set by onWorldLoad
    //this.playerDimension = this.mc.theWorld.provider.dimensionId;
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
    } else if (marker.dimension == this.playerDimension) {
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
  public void load() {
    if ((this.mc.theWorld == null) || (this.mc.thePlayer == null)) {
      MwUtil.log("Mw.load: world or player is null, cannot load yet");
      return;
    }

    MwUtil.log("Mw.load: loading...");

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
        MwUtil.log("error: no such directory %s", Config.instance.saveDirOverride);
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
      MwUtil.log("Mapwriter: ERROR: could not create images directory '%s'", this.imageDir.getPath());
    }

    this.tickCounter = 0;
    this.onPlayerDeathAlreadyFired = false;

    //this.multiplayer = !this.mc.isIntegratedServerRunning();
    // marker manager only depends on the config being loaded
    this.markerManager = new MarkerManager(this.worldConfig, catMarkers);
    this.markerManager.load();

    this.playerTrail = new Trail("player");

    // region manager depends on config, mapTexture, and block colours
    this.regionManager = new RegionManager(this.imageDir.toString());
    // overlay manager depends on mapTexture
    this.miniMap = new MiniMap();
    this.miniMap.view.setDimensionID(this.mc.thePlayer.dimension);

    this.chunkManager = new ChunkManager();

    this.initialized = true;

    //if (!zoomLevelsExist) {
    //printBoth("recreating zoom levels");
    //this.regionManager.recreateAllZoomLevels();
    //}
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

    MwUtil.log("Mw.close: closing...");

    if (this.initialized) {
      this.initialized = false;

      this.chunkManager.removeAll();

      if (this.regionManager != null) {
        regionManager.dispose();
      }
      this.regionManager = null;

      terminateExecutor();

      this.playerTrail.close();

      this.markerManager.save();
      this.markerManager = null;

      // close overlay
      this.miniMap.close();
      this.miniMap = null;

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

    this.playerDimension = world.provider.dimensionId;
    if (this.initialized) {
      this.addDimension(this.playerDimension);
      this.miniMap.view.setDimensionID(this.playerDimension);
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
    }

    if (this.mc.thePlayer != null) {

      this.updatePlayer();

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
        // if in game (no gui screen) center the minimap on the player and render it.
        this.miniMap.view.setCenter(this.playerX, this.playerZ);
        this.miniMap.view.setDimensionID(this.playerDimension);
        this.miniMap.drawCurrentMap();
      }

      // let the renderEngine know we have changed the bound texture.
      //this.mc.renderEngine.resetBoundTexture();
      //if (this.tickCounter % 100 == 0) {
      //	MwUtil.log("tick %d", this.tickCounter);
      //}
      this.playerTrail.onTick();

      this.tickCounter++;
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
      this.updatePlayer();
      int deleteCount = this.markerManager.countMarkersInGroup("playerDeaths") - Config.instance.maxDeathMarkers + 1;
      for (int i = 0; i < deleteCount; i++) {
        // delete the first marker found in the group "playerDeaths".
        // as new markers are only ever appended to the marker list this will delete the
        // earliest death marker added.
        this.markerManager.delMarker(null, "playerDeaths");
      }
      this.markerManager.addMarker(MwUtil.getCurrentDateString(), "playerDeaths", this.playerXInt, this.playerYInt, this.playerZInt, this.playerDimension, 0xffff0000);
      this.markerManager.setVisibleGroupName("playerDeaths");
      this.markerManager.update();
    }
  }

  public void onKeyDown(KeyBinding kb) {
    // make sure not in GUI element (e.g. chat box)
    if ((this.mc.currentScreen == null) && (this.initialized)) {
      //Mw.log("client tick: %s key pressed", kb.keyDescription);

      if (kb == MwKeyHandler.keyMapMode) {
        // map mode toggle
        this.miniMap.nextOverlayMode(1);

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
                        this.playerXInt,
                        this.playerYInt,
                        this.playerZInt,
                        this.playerDimension
                )
        );

      } else if (kb == MwKeyHandler.keyNextGroup) {
        // toggle marker mode
        this.markerManager.nextGroup();
        this.markerManager.update();
        this.mc.thePlayer.addChatMessage(new ChatComponentText("group " + this.markerManager.getVisibleGroupName() + " selected"));

      } else if (kb == MwKeyHandler.keyTeleport) {
        // set or remove marker
        Marker marker = this.markerManager.getNearestMarkerInDirection(
                this.playerXInt,
                this.playerZInt,
                this.playerHeading);
        if (marker != null) {
          this.teleportToMarker(marker);
        }
      } else if (kb == MwKeyHandler.keyZoomIn) {
        // zoom in
        this.miniMap.view.modifyZoom(-1);
      } else if (kb == MwKeyHandler.keyZoomOut) {
        // zoom out
        this.miniMap.view.modifyZoom(1);
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
}
