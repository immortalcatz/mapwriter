/*
 */
package mapwriter.mapgen;

/**
 * @author Two
 */
public class RegionID {

  static final int REGION_DIVISOR = Region.REGION_SIZE / Region.CHUNK_SIZE;

  public final int x, z; // world coordinates of the top-left corner of this region
  public final int regionX, regionZ; // internal coordinates
  protected final int hash;

  public static RegionID byCoordinates(final float x, final float z) {
    return new RegionID(Math.floorDiv((int) x, Region.REGION_SIZE), Math.floorDiv((int) z, Region.REGION_SIZE));
  }

  public static RegionID byChunk(final int chunkX, final int chunkZ) {
    return new RegionID(chunkX / REGION_DIVISOR, chunkZ / REGION_DIVISOR);
  }

  public RegionID(final int regionX, final int regionZ) {
    this.regionX = regionX;
    this.regionZ = regionZ;
    this.x = this.regionX * REGION_DIVISOR;
    this.z = this.regionZ * REGION_DIVISOR;
    this.hash = (this.regionX << 16) | this.regionZ;
  }

  public boolean isChunkWithin(final int chunkX, final int chunkZ) {
    return (((chunkX / REGION_DIVISOR) == this.x) && ((chunkZ / REGION_DIVISOR) == this.z));
  }

  public String toFilename() {
    return regionX + "." + regionZ + "." + Region.IMAGE_TYPE;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RegionID other = (RegionID) obj;
    return (this.hash == other.hash);
  }

}
