/*
 */
package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

/**
 * @author Two
 */
public class BlockColorEntry {

  static final int BLOCK_SIDE_TOP = 1; // Minecraft says so

  public static BlockColorEntry fromWorld(final World world, final int x, final int y, final int z) {
    final Block block = world.getBlock(x, y, z);
    final int metadata = world.getBlockMetadata(x, y, z);
    try {
      final IIcon icon = block.getIcon(BLOCK_SIDE_TOP, metadata); // this will return a default texture for some blocks like flowers, but that is ok
      if (icon != null) {
        final int colorMultiplier = block.colorMultiplier(world, x, y, z);
        return new BlockColorEntry(icon.getIconName(), colorMultiplier);
      }
    } catch (Throwable t) {
      FMLLog.log(Level.ERROR, t, "Unable to calculate color for block %s at {%d, %d, %d}", block.getUnlocalizedName(), x, y, z);
    }
    return null;
  }

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
