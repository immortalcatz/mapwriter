/*
 */
package mapwriter;

import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class ChunkEntry implements Comparable<ChunkEntry> {

  public final Chunk chunk;
  public final int x, z, dimension;
  public final int hashCode;
  public volatile long lastUpdate = 0;

  protected static int generateHashCode(final int x, final int z, final int dimension) {
    int hash = 7;
    hash = 17 * hash + x;
    hash = 17 * hash + z;
    hash = 17 * hash + dimension;
    return hash;
  }

  public ChunkEntry(final Chunk chunk) {
    this.chunk = chunk;
    this.x = chunk.xPosition;
    this.z = chunk.zPosition;
    this.dimension = chunk.worldObj.provider.dimensionId;
    this.hashCode = generateHashCode(this.x, this.z, this.dimension);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ChunkEntry other = (ChunkEntry) obj;
    return (this.x == other.x) && (this.z == other.z) && (this.dimension == other.dimension);
  }

  @Override
  public int compareTo(final ChunkEntry other) {
    return Long.compare(this.lastUpdate, other.lastUpdate);
  }

  @Override
  public String toString() {
    return "ChunkEntry{" + x + ", " + z + ", " + dimension + '}';
  }

}
