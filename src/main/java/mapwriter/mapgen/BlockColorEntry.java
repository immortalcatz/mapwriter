/*
 */
package mapwriter.mapgen;

/**
 * @author Two
 */
public class BlockColorEntry {

  static int generateHash(final String textureName, final int colorMultiplier) {
    int hash = 7;
    hash = 59 * hash + textureName.hashCode();
    hash = 59 * hash + colorMultiplier;
    return hash;
  }

  public final String textureName;
  public final int colorMultiplier;
  protected final int hash;

  public BlockColorEntry(final String textureName, final int colorMultiplier) {
    this.textureName = textureName;
    this.colorMultiplier = ((colorMultiplier & 0xFF000000) == 0) ? 0xFF000000 | colorMultiplier : colorMultiplier;
    this.hash = generateHash(textureName, colorMultiplier);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BlockColorEntry other = (BlockColorEntry) obj;
    return ((this.colorMultiplier == other.colorMultiplier) && this.textureName.equals(other.textureName));
  }

}
