/*
 */
package mapwriter.map;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.imageio.ImageIO;
import mapwriter.Mw;
import mapwriter.forge.MapWriter;
import static mapwriter.map.Region.REGION_SIZE;
import mapwriter.map.RegionManager.RegionData;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class RegionManager {

  class RegionData implements Comparable<RegionData> {

    RegionID regionID;
    int[] pixels;

    public RegionData(final RegionID regionID, final int[] pixels) {
      this.regionID = regionID;
      this.pixels = pixels;
    }

    @Override
    public int compareTo(final RegionData other) {
      return this.regionID.compareTo(other.regionID);
    }

    @Override
    public int hashCode() {
      return regionID.hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final RegionData other = (RegionData) obj;
      return (this.regionID.equals(other.regionID));
    }

  }

  // Class variables
  final ConcurrentHashMap<RegionID, Region> cachedRegions = new ConcurrentHashMap<RegionID, Region>();
  final ConcurrentHashMap<RegionID, Region> modifiedRegions = new ConcurrentHashMap<RegionID, Region>();
  final ConcurrentSkipListSet<RegionData> regionsToCreate = new ConcurrentSkipListSet<RegionData>();
  final String saveDir;
  ScheduledFuture<?> scheduledSafeTask = null;

  public RegionManager(final String saveDir) {
    this.saveDir = saveDir;
  }

  public void start() {
    if (this.scheduledSafeTask == null) {
      this.scheduledSafeTask = Mw.backgroundExecutor.scheduleAtFixedRate(regionSaveTask, 1, 1, TimeUnit.MINUTES);
    }
  }

  public void stop() {
    if (this.scheduledSafeTask != null) {
      this.scheduledSafeTask.cancel(false);
      this.scheduledSafeTask = null;
    }
  }

  public void updateRegionTextureData() {
    if (regionsToCreate.isEmpty() == false) {
      for (final Iterator<RegionData> it = regionsToCreate.iterator(); it.hasNext();) {
        final RegionData regionData = it.next();
        final Region newRegion = new Region(regionData.regionID); // Regions need to be created in the main thread, as they require the GL object for texture creation
        if (regionData.pixels != null) {
          newRegion.setRGB(regionData.pixels);
        }
        this.cachedRegions.putIfAbsent(newRegion.regionID, newRegion);
        it.remove();
        MapWriter.log.info("Created Region %s", regionData.regionID.toString());
      }
    }
  }

  protected Region getOrCreateRegion(final RegionID regionID) {
    return cachedRegions.computeIfAbsent(regionID, regionGenerator);
  }

  public List<Region> getAllExistingRegionsInArea(final double coordLeft, final double coordTop, final double coordRight, final double coordBottom) {
    final RegionID topLeft = RegionID.byCoordinates(coordLeft, coordTop);
    final RegionID bottomRight = RegionID.byCoordinates(coordRight + 0.5f, coordBottom + 0.5f);
    final int minX = Math.min(topLeft.regionX, bottomRight.regionX);
    final int maxX = Math.max(topLeft.regionX, bottomRight.regionX);
    final int minZ = Math.min(topLeft.regionZ, bottomRight.regionZ);
    final int maxZ = Math.max(topLeft.regionZ, bottomRight.regionZ);

    final ArrayList<Region> result = new ArrayList<Region>();

    Region region;
    for (int regionX = minX; regionX <= maxX; ++regionX) {
      for (int regionZ = minZ; regionZ <= maxZ; ++regionZ) {
        region = cachedRegions.get(new RegionID(regionX, regionZ));
        if (region != null) {
          result.add(region);
        }
      }
    }

    return result;
  }

  public void updateChunk(final Chunk chunk, final int[] newPixels) {
    final RegionID regionID = RegionID.byChunk(chunk.xPosition, chunk.zPosition);
    final Region region = getOrCreateRegion(regionID);
    if (region != null) {
      region.updateChunk(chunk, newPixels);
      modifiedRegions.put(regionID, region);
    }
  }

  public void saveAll() {
    Mw.backgroundExecutor.execute(regionSaveTask);
  }

  public void dispose() {
    final ArrayList<Map.Entry<RegionID, Region>> entries = new ArrayList<Map.Entry<RegionID, Region>>(cachedRegions.entrySet());
    cachedRegions.clear();
    regionSaveTask.run();
    entries.forEach(disposeAll);
  }

  protected static int[] loadSavedRegion(final Path filepath) {
    if (Files.exists(filepath)) {
      InputStream in = null;
      try {
        in = Files.newInputStream(filepath, StandardOpenOption.READ);
        final BufferedImage image = ImageIO.read(in);
        if ((image.getWidth() != REGION_SIZE) || (image.getHeight() != REGION_SIZE)) {
          Files.delete(filepath); // kill it with fire!
          throw new IOException("Image data is of wrong size. Expected " + REGION_SIZE + "x" + REGION_SIZE + " but got " + image.getWidth() + "x" + image.getHeight());
        }
        return image.getRGB(0, 0, REGION_SIZE, REGION_SIZE, null, 0, REGION_SIZE);
      } catch (Exception e) {
        MapWriter.log.warn("Unable to read map data file '%s': %s", filepath.getFileName().toString(), e.toString());
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (Exception e) {
          }
        }
      }
    }
    return null;
  }
  //--- Tasks ------------------------------------------------------------------
  final Function<RegionID, Region> regionGenerator = new Function<RegionID, Region>() {

    @Override
    public Region apply(final RegionID regionID) {
      final RegionData regionData = new RegionData(regionID, null);
      if (regionsToCreate.contains(regionData) == false) {
        regionData.pixels = loadSavedRegion(Paths.get(saveDir, regionID.toFilename()));
        regionsToCreate.add(regionData);
      }

      return null;
    }
  };

  final Consumer<Map.Entry<RegionID, Region>> regionSaver = new Consumer<Map.Entry<RegionID, Region>>() {

    @Override
    public void accept(final Map.Entry<RegionID, Region> entry) {
      entry.getValue().save(saveDir);
    }
  };

  final Consumer<Map.Entry<RegionID, Region>> disposeAll = new Consumer<Map.Entry<RegionID, Region>>() {

    @Override
    public void accept(final Map.Entry<RegionID, Region> entry) {
      entry.getValue().dispose();
    }
  };

  final Runnable regionSaveTask = new Runnable() {

    @Override
    public void run() {
      final Set<Map.Entry<RegionID, Region>> mapEntries = modifiedRegions.entrySet();
      final ArrayList<Map.Entry<RegionID, Region>> entries = new ArrayList<Map.Entry<RegionID, Region>>(mapEntries);
      mapEntries.removeAll(entries);

      if (entries.isEmpty() == false) {
        entries.forEach(regionSaver);
        MapWriter.log.info("Map data saved");
      }
    }
  };
}
