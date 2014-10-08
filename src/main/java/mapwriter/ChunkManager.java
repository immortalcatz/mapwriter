package mapwriter;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;

import mapwriter.region.MwChunk;
import mapwriter.tasks.SaveChunkTask;
import mapwriter.tasks.UpdateSurfaceChunksTask;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ChunkManager {

  public Mw mw;
  private boolean closed = false;
  private final ConcurrentSkipListSet<ChunkEntry> chunkList = new ConcurrentSkipListSet<ChunkEntry>();

  private final ScheduledFuture updateTask; // in ms

  public ChunkManager(final Mw mw) {
    this.mw = mw;
    this.updateTask = mw.executor.addScheduledTask(new UpdateSurfaceChunksTask(this.mw, chunkList), 1);
  }

  public void close() {
    this.updateTask.cancel(false);
    this.chunkList.clear();
    this.closed = true;
  }

  public void addChunk(final Chunk chunk) {
    if (!this.closed && (chunk != null)) {
      final ChunkEntry entry = new ChunkEntry(chunk);
      this.chunkList.add(entry);
    }
  }

  public void removeChunk(final Chunk chunk) {
    if (chunk != null) {
      final ChunkEntry entry = new ChunkEntry(chunk);
      this.chunkList.remove(entry);
    }
  }

  public void updateUndergroundChunks() {
//    int chunkArrayX = (this.mw.playerXInt >> 4) - 1;
//    int chunkArrayZ = (this.mw.playerZInt >> 4) - 1;
//    MwChunk[] chunkArray = new MwChunk[9];
//    for (int z = 0; z < 3; z++) {
//      for (int x = 0; x < 3; x++) {
//        Chunk chunk = this.mw.mc.theWorld.getChunkFromChunkCoords(
//                chunkArrayX + x,
//                chunkArrayZ + z
//        );
//        if (!chunk.isEmpty()) {
//          chunkArray[(z * 3) + x] = copyToMwChunk(chunk);
//        }
//      }
//    }
  }

  public void updateSurfaceChunks() {
  }

  public void onTick() {
    if (!this.closed) {
      if ((this.mw.tickCounter & 0xf) == 0) {
        this.updateUndergroundChunks();
      } else {
        this.updateSurfaceChunks();
      }
    }
  }
}
