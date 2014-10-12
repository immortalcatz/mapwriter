package mapwriter.mapgen;

import java.util.concurrent.ConcurrentSkipListSet;
import mapwriter.ChunkEntry;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class ChunkManager {

  final ConcurrentSkipListSet<ChunkEntry> loadedChunks = new ConcurrentSkipListSet<ChunkEntry>();

  public void addChunk(final Chunk chunk) {
    this.loadedChunks.add(new ChunkEntry(chunk));
  }

  public void removeChunk(final Chunk chunk) {
    this.loadedChunks.remove(new ChunkEntry(chunk));
  }

  public void removeAll() {
    this.loadedChunks.clear();
  }
}
