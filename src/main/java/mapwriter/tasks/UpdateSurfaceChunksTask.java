package mapwriter.tasks;

import java.util.concurrent.ConcurrentSkipListSet;
import mapwriter.ChunkEntry;
import mapwriter.ChunkManager;
import mapwriter.Mw;
import mapwriter.map.MapTexture;
import mapwriter.region.ChunkRender;
import mapwriter.region.MwChunk;
import mapwriter.region.RegionManager;

public class UpdateSurfaceChunksTask extends Task {
  private static final int CHUNK_SIZE = 16;

  final ConcurrentSkipListSet<ChunkEntry> chunkArray;
  final RegionManager regionManager;
  final int[] pixels = new int[CHUNK_SIZE * CHUNK_SIZE];
  final MapTexture mapTexture;

  public UpdateSurfaceChunksTask(Mw mw, final ConcurrentSkipListSet<ChunkEntry> chunkArray) {
    this.mapTexture = mw.mapTexture;
    this.regionManager = mw.regionManager;
    this.chunkArray = chunkArray;
  }

  @Override
  public void run() {
    final ChunkEntry chunkEntry = this.chunkArray.pollFirst();
    if (chunkEntry != null) {
      ChunkRender.renderSurface(this.regionManager.blockColours, chunkEntry.chunk, pixels, 0, CHUNK_SIZE, !chunkEntry.chunk.worldObj.provider.hasNoSky);
      final MwChunk chunk = ChunkManager.copyToMwChunk(chunkEntry.chunk);
      // update the chunk in the region pixels
      this.regionManager.updateChunk(chunk);
      // copy updated region pixels to maptexture
      this.mapTexture.updateArea(
              this.regionManager,
              chunk.x << 4, chunk.z << 4,
              MwChunk.SIZE, MwChunk.SIZE, chunk.dimension
      );
      chunkEntry.lastUpdate = System.currentTimeMillis();
      this.chunkArray.add(chunkEntry);
    }
  }

  @Override
  public void onComplete() {
  }
}
