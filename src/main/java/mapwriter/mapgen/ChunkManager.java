package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
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

  ScheduledFuture<?> updaterTask = null;

  public ChunkManager() {
  }

  public void start() {
    if (updaterTask == null) {
      updaterTask = Mw.backgroundExecutor.scheduleAtFixedRate(chunkUpdater, 1000, 5, TimeUnit.MILLISECONDS);
    }
  }

  public void stop() {
    if (updaterTask != null) {
      updaterTask.cancel(false);
      updaterTask = null;
    }
  }

  public void addChunk(final Chunk chunk) {
    if (this.loadedChunks.add(new ChunkEntry(chunk))) {
//      MapWriter.log.info("Added chunk [%3d, %3d]", chunk.xPosition, chunk.zPosition);
    }
  }

  public void removeChunk(final Chunk chunk) {
    if (this.loadedChunks.remove(new ChunkEntry(chunk))) {
//      MapWriter.log.info("Removed chunk [%3d, %3d]", chunk.xPosition, chunk.zPosition);
    }
  }

  public void removeAll() {
    this.loadedChunks.clear();
//    FMLLog.info("Removed all chunks");
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
//          MapWriter.log.info("Updating chunk [%3d, %3d]", next.chunk.xPosition, next.chunk.zPosition);
          if (next.chunk.isEmpty() == false) {
            Mw.instance.regionManager.updateChunk(next.chunk, ColorConvert.getChunkSurfaceAsPixels(next.chunk));
          }
        }
      } catch (Throwable t) {
        FMLLog.log(Level.ERROR, t, "Unable to update chunk:", t.toString());
      }
    }
  };
}
