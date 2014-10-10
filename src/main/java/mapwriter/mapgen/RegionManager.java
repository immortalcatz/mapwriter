/*
 */
package mapwriter.mapgen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class RegionManager {

  static int getRegionIDByChunkPosition(final int chunkX, final int chunkZ) {
    return (((chunkX / Region.REGION_DIVISOR) & 0xFFFF) << 16) | ((chunkZ / Region.REGION_DIVISOR) & 0xFFFF);
  }

  static String getFilenameByRegionID(final int regionID) {
    return ((regionID >> 16) & 0xFFFF) + '.' + (regionID & 0xFFFF) + '.' + Region.IMAGE_TYPE;
  }

  // Class variables
  final ConcurrentHashMap<Integer, Region> cachedRegions = new ConcurrentHashMap<Integer, Region>();
  final ConcurrentHashMap<Integer, Region> modifiedRegions = new ConcurrentHashMap<Integer, Region>();
  final String saveDir;
  final Function<Integer, Region> regionGenerator;
  final Consumer<Map.Entry<Integer, Region>> regionSaver;

  public RegionManager(final String saveDir) {
    this.saveDir = saveDir;

    this.regionGenerator = new Function<Integer, Region>() {

      @Override
      public Region apply(final Integer regionID) {
        final Path filepath = Paths.get(saveDir, getFilenameByRegionID(regionID));
        return Region.fromFile(filepath); // this returns a previously saved region or an empty one
      }
    };

    this.regionSaver = new Consumer<Map.Entry<Integer, Region>>() {

      @Override
      public void accept(final Map.Entry<Integer, Region> entry) {
        entry.getValue().saveAs(Paths.get(saveDir, getFilenameByRegionID(entry.getKey())));
      }
    };
  }

  protected Region getOrCreateRegion(final int regionID) {
    return cachedRegions.computeIfAbsent(regionID, regionGenerator);
  }

  public void updateChunk(final Chunk chunk, final int[] newPixels) {
    final int regionID = getRegionIDByChunkPosition(chunk.xPosition, chunk.zPosition);
    final Region region = getOrCreateRegion(regionID);
    region.setChunk(chunk, newPixels);
    modifiedRegions.put(regionID, region);
  }

  public void saveAll() {
    final Set<Map.Entry<Integer, Region>> mapEntries = modifiedRegions.entrySet();
    final ArrayList<Map.Entry<Integer, Region>> entries = new ArrayList<Map.Entry<Integer, Region>>(mapEntries);
    mapEntries.removeAll(entries);

    entries.forEach(regionSaver);
  }

}
