/*
 */
package mapwriter.mapgen;

import mapwriter.map.Region;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import mapwriter.util.MinecraftUtil;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class ColorConvert {

  static final int ALPHA_FULL = 0xFF000000;
  static final int BLACK = 0xFF000000;
  static final int TOPO_SMOOTH = ~3;

  static final ConcurrentHashMap<BlockColorEntry, Integer> knownColors = new ConcurrentHashMap<BlockColorEntry, Integer>();

  public static void reset() {
    knownColors.clear();
  }

  public static int[] getChunkSurfaceAsPixels(final Chunk chunk) {
    final int[] result = new int[Region.CHUNK_SIZE * Region.CHUNK_SIZE];
    final int x0 = chunk.xPosition * Region.CHUNK_SIZE;
    final int z0 = chunk.zPosition * Region.CHUNK_SIZE;
    int y, avgColor;
    for (int x = 0; x < Region.CHUNK_SIZE; ++x) {
      for (int z = 0; z < Region.CHUNK_SIZE; ++z) {
        y = chunk.getHeightValue(x, z); // first non-opaque block above ground. Usually air, but can be grass so include that for the looks.
        if (y > 0) {
          avgColor = getAverageBlockColor(chunk.worldObj, x0 + x, y, z0 + z);
          result[x + (Region.CHUNK_SIZE - z - 1) * Region.CHUNK_SIZE] = avgColor; // 0,0 is left-bottom in chunk space, but left-top in pixel space
        }
      }
    }
    return result;
  }

  public static int getAverageBlockColor(final World world, final int x, int y, final int z) {
    BlockColorEntry blockColorEntry = null;
    while ((y > 0) && ((blockColorEntry = BlockColorEntry.fromWorld(world, x, y, z)) == null)) { // move down until there is a block with a valid texture
      --y;
    }
    if (blockColorEntry != null) { // if the world is not yet fully loaded, chunks may not yet have any blocks
      final int averageColor = knownColors.computeIfAbsent(blockColorEntry, generateAverageBlockColor);
      final float depthMultiplier = Math.max(Math.min((y & TOPO_SMOOTH) / 64.0f, 2.0f), 0.1f);
      return changeBrightness(averageColor, depthMultiplier); // changing brightness for a topological effect
    }
    return BLACK;
  }

  public static int calculateAverageColor(final int[] pixels, final int multiplier) {
    int red = 0;
    int green = 0;
    int blue = 0;

    int pixelCount = 0;
    for (int pixel : pixels) {
      if ((pixel & ALPHA_FULL) != 0) { // ignore fully transparent pixels
        ++pixelCount;
        pixel = colorMultiply(pixel, multiplier);
        red += (pixel >>> 16) & 0xFF;
        green += (pixel >>> 8) & 0xFF;
        blue += (pixel) & 0xFF;
      }
    }
    if (pixelCount == 0) {
      return BLACK;
    } else {
      red = (red / pixelCount) & 0xFF;
      green = (green / pixelCount) & 0xFF;
      blue = (blue / pixelCount) & 0xFF;
      return (ALPHA_FULL | (red << 16) | (green << 8) | blue);
    }
  }

  public static int colorMultiply(final int color1, final int color2) {
    final double alpha1 = ((color1 >>> 24) & 0xFF) / 255.0;
    final double red1 = alpha1 * ((color1 >>> 16) & 0xFF) / 255.0;
    final double green1 = alpha1 * ((color1 >>> 8) & 0xFF) / 255.0;
    final double blue1 = alpha1 * ((color1) & 0xFF) / 255.0;

    final double alpha2 = ((color1 >>> 24) & 0xFF) / 255.0;
    final double red2 = alpha2 * ((color2 >>> 16) & 0xFF) / 255.0;
    final double green2 = alpha2 * ((color2 >>> 8) & 0xFF) / 255.0;
    final double blue2 = alpha2 * ((color2) & 0xFF) / 255.0;

    final int red = (int) Math.round(red1 * red2 * 255.0) & 0xFF;
    final int green = (int) Math.round(green1 * green2 * 255.0) & 0xFF;
    final int blue = (int) Math.round(blue1 * blue2 * 255.0) & 0xFF;

    return 0xFF000000 | (red << 16) | (green << 8) | blue;
  }

  public static int changeBrightness(final int color, final float multiplier) {
    final int red = (int) Math.min(Math.round(((color >>> 16) & 0xFF) * multiplier), 255) & 0xFF;
    final int green = (int) Math.min(Math.round(((color >>> 8) & 0xFF) * multiplier), 255) & 0xFF;
    final int blue = (int) Math.min(Math.round((color & 0xFF) * multiplier), 255) & 0xFF;

    return 0xFF000000 | (red << 16) | (green << 8) | blue;
  }

  static final Function<BlockColorEntry, Integer> generateAverageBlockColor = new Function<BlockColorEntry, Integer>() {

    @Override
    public Integer apply(final BlockColorEntry blockColorEntry) {
      final int[] pixels = MinecraftUtil.loadTexture(blockColorEntry.textureName);
      if (pixels != null) {
        return calculateAverageColor(pixels, blockColorEntry.colorMultiplier);
      }
      return BLACK; // otherwise the calculation will be repeated many times
    }

  };
}
