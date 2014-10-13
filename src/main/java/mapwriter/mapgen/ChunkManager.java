package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import mapwriter.ChunkEntry;
import mapwriter.Mw;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;

/**
 * @author Two
 */
public class ChunkManager {

  final ConcurrentSkipListSet<ChunkEntry> loadedChunks = new ConcurrentSkipListSet<ChunkEntry>();

  public ChunkManager() {
    Mw.backgroundExecutor.scheduleAtFixedRate(chunkUpdater, 1000, 5, TimeUnit.MILLISECONDS);
  }

  public void addChunk(final Chunk chunk) {
    if (this.loadedChunks.add(new ChunkEntry(chunk))) {
//      FMLLog.info("Added chunk [%3d, %3d]", chunk.xPosition, chunk.zPosition);
    }
  }

  public void removeChunk(final Chunk chunk) {
    if (this.loadedChunks.remove(new ChunkEntry(chunk))) {
//      FMLLog.info("Removed chunk [%3d, %3d]", chunk.xPosition, chunk.zPosition);
    }
  }

  public void removeAll() {
    this.loadedChunks.clear();
//    FMLLog.info("Removed all chunks");
  }

  public static int[] getChunkSurfaceAsPixels(final Chunk chunk) {
    final int[] result = new int[Region.CHUNK_SIZE * Region.CHUNK_SIZE];
    if (chunk.isEmpty() == false) {
      int y;
      for (int x = 0; x < Region.CHUNK_SIZE; ++x) {
        for (int z = 0; z < Region.CHUNK_SIZE; ++z) {
          y = chunk.getHeightValue(x, z) - 1; // heightmap is last air block before ground, we want the ground
          if (y > 0) {
            result[x + z * Region.CHUNK_SIZE] = ColorConvert.averageBlockColor(chunk.worldObj, x, y, z);
          }
        }
      }
    }
    return result;
  }

  final Runnable chunkUpdater = new Runnable() {

    volatile Iterator<ChunkEntry> iterator = null;

    @Override
    public void run() {
      try {
        if ((iterator == null) || (iterator.hasNext() == false)) {
          iterator = loadedChunks.iterator();
        }
        if (iterator.hasNext()) {
          final ChunkEntry next = iterator.next();
//            FMLLog.info("Updating chunk [%3d, %3d]", next.chunk.xPosition, next.chunk.zPosition);
          if (next.chunk.isEmpty() == false) {
            Mw.instance.regionManager.updateChunk(next.chunk, getChunkSurfaceAsPixels(next.chunk));
          }
        }
      } catch (Throwable t) {
        FMLLog.log(Level.ERROR, t, "Unable to update chunk:", t.toString());
      }
    }
  };
}
