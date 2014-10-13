/*
 */
package mapwriter.mapgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import mapwriter.Mw;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class RegionManager {

  // Class variables
  final ConcurrentHashMap<RegionID, Region> cachedRegions = new ConcurrentHashMap<RegionID, Region>();
  final ConcurrentHashMap<RegionID, Region> modifiedRegions = new ConcurrentHashMap<RegionID, Region>();
  final String saveDir;

  public RegionManager(final String saveDir) {
    this.saveDir = saveDir;
  }

  protected Region getOrCreateRegion(final RegionID regionID) {
    return cachedRegions.computeIfAbsent(regionID, regionGenerator);
  }

  public List<Region> getAllExistingRegionsInArea(final double coordLeft, final double coordTop, final double coordRight, final double coordBottom) {
    final RegionID topLeft = RegionID.byCoordinates(coordLeft, coordTop);
    final RegionID bottomRight = RegionID.byCoordinates(coordRight + 0.5f, coordBottom + 0.5f);

    final ArrayList<Region> result = new ArrayList<Region>();

    Region region;
    for (int regionX = topLeft.regionX; regionX <= bottomRight.regionX; ++regionX) {
      for (int regionZ = topLeft.regionZ; regionZ <= bottomRight.regionZ; ++regionZ) {
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
    region.updateChunk(chunk, newPixels);
    modifiedRegions.put(regionID, region);
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

  //--- Tasks ------------------------------------------------------------------
  final Function<RegionID, Region> regionGenerator = new Function<RegionID, Region>() {

    @Override
    public Region apply(final RegionID regionID) {
      return Region.fromFile(saveDir, regionID); // this returns a previously saved region or an empty one
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

      entries.forEach(regionSaver);
    }
  };
}
