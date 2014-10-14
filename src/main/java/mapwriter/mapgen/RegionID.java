/*
 */
package mapwriter.mapgen;

/**
 * @author Two
 */
public class RegionID implements Comparable<RegionID> {

  static final int CHUNK_TO_REGION_DIVISOR = Region.REGION_SIZE / Region.CHUNK_SIZE;

  public final int x, z; // world coordinates of the top-left corner of this region
  public final int regionX, regionZ; // internal coordinates
  protected final int hash;

  public static RegionID byCoordinates(final double x, final double z) {
    return new RegionID(Math.floorDiv(((int) x), Region.REGION_SIZE), Math.floorDiv(((int) z), Region.REGION_SIZE));
  }

  public static RegionID byChunk(final int chunkX, final int chunkZ) {
    return new RegionID(Math.floorDiv(chunkX, CHUNK_TO_REGION_DIVISOR), Math.floorDiv(chunkZ, CHUNK_TO_REGION_DIVISOR));
  }

  public RegionID(final int regionX, final int regionZ) {
    this.regionX = regionX;
    this.regionZ = regionZ;
    this.x = regionX * Region.REGION_SIZE;
    this.z = regionZ * Region.REGION_SIZE;
    this.hash = ((this.regionX & 0xFFFF) << 16) | (this.regionZ & 0xFFFF);
  }

  public String toFilename() {
    return regionX + "." + regionZ + "." + Region.IMAGE_TYPE;
  }

  @Override
  public String toString() {
    return "{" + regionX + ", " + regionZ + "}";
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
    return ((this.regionX == other.regionX) && (this.regionZ == other.regionZ));
  }

  @Override
  public int compareTo(final RegionID other) {
    final int compareX = Integer.compare(this.regionX, other.regionX);
    if (compareX == 0) {
      return Integer.compare(this.regionZ, other.regionZ);
    } else {
      return compareX;
    }
  }
}
