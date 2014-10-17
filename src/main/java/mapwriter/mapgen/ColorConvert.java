/*
 */
package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.imageio.ImageIO;
import mapwriter.util.PixelData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;

/**
 * @author Two
 */
public class ColorConvert {

  static final int ALPHA_FULL = 0xFF000000;
  static final int BLACK = 0xFF000000;
  static final int BLOCK_SIDE_TOP = 1; // Minecraft says so

  static final ConcurrentHashMap<BlockColorEntry, Integer> knownColors = new ConcurrentHashMap<BlockColorEntry, Integer>();

  public static void reset() {
    knownColors.clear();
  }

  public static int[] getChunkSurfaceAsPixels(final Chunk chunk) {
    final int[] result = new int[Region.CHUNK_SIZE * Region.CHUNK_SIZE];
    final int x0 = chunk.xPosition * Region.CHUNK_SIZE;
    final int z0 = chunk.zPosition * Region.CHUNK_SIZE;
    int y, avgColor;
    float depthMultiplier;
    for (int x = 0; x < Region.CHUNK_SIZE; ++x) {
      for (int z = 0; z < Region.CHUNK_SIZE; ++z) {
        y = chunk.getHeightValue(x, z); // first non-opaque block above ground. Usually air, but can be grass so include that for the looks.
        if (y > 0) {
          avgColor = averageBlockColor(chunk.worldObj, x0 + x, y, z0 + z);
          depthMultiplier = Math.max(Math.min(y / 64.0f, 2.0f), 0.1f);
          avgColor = changeBrightness(avgColor, depthMultiplier);
          result[x + (Region.CHUNK_SIZE - z - 1) * Region.CHUNK_SIZE] = avgColor; // 0,0 is left-bottom in chunk space, but left-top in pixel space
        }
      }
    }
    return result;
  }

  public static BlockColorEntry getBlockColorEntry(final World world, final int x, int y, final int z) {
    final Block block = world.getBlock(x, y, z);
    final int metadata = world.getBlockMetadata(x, y, z);
    try {
      final IIcon icon = block.getIcon(BLOCK_SIDE_TOP, metadata);
      if (icon != null) {
        final int colorMultiplier = block.colorMultiplier(world, x, y, z);
        return new BlockColorEntry(icon.getIconName(), colorMultiplier);
      }
    } catch (Throwable t) {
      FMLLog.log(Level.ERROR, t, "Unable to calculate color for block %s at {%d, %d, %d}", block.getUnlocalizedName(), x, y, z);
    }
    return null;
  }

  public static int averageBlockColor(final World world, final int x, int y, final int z) {
    BlockColorEntry blockColorEntry = null;
    while ((y > 0) && ((blockColorEntry = getBlockColorEntry(world, x, y, z)) == null)) {
      --y;
    }
    if (blockColorEntry != null) {
      return knownColors.computeIfAbsent(blockColorEntry, generateAverageBlockColor);
    } else {
//      FMLLog.warning("Unable to find any blocks at %d, %d", x, z);
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

  static int[] loadTexture(final String blockTexture) {
    try {
      final ResourceLocation resourceLocation = getResourceLocation(blockTexture);
      final IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
      final IResource resource = resourceManager.getResource(resourceLocation);
      final InputStream in = resource.getInputStream();
      final BufferedImage image = ImageIO.read(in);
      return PixelData.getPixelsFromImage(image);
    } catch (Exception e) {
      FMLLog.log(Level.ERROR, e, "Unable to load texture '%s'", blockTexture);
    }
    return null;
  }

  static ResourceLocation getResourceLocation(final String blockTexture) {
    String domain = "minecraft";
    String path = blockTexture;
    final int domainSeparator = blockTexture.indexOf(':');

    if (domainSeparator >= 0) {
      path = blockTexture.substring(domainSeparator + 1);

      if (domainSeparator > 1) {
        domain = blockTexture.substring(0, domainSeparator);
      }
    }

    final String resourcePath = "textures/blocks/" + path + ".png";  // base path and PNG are hardcoded in Minecraft
    return new ResourceLocation(domain.toLowerCase(), resourcePath);
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
      final int[] pixels = loadTexture(blockColorEntry.textureName);
      if (pixels != null) {
        return calculateAverageColor(pixels, blockColorEntry.colorMultiplier);
      }
      return BLACK; // otherwise the calculation will be repeated many times
    }

  };
}
