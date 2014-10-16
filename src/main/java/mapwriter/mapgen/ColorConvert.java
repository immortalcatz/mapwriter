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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

/**
 * @author Two
 */
public class ColorConvert {

  static final int ALPHA_FULL = 0xFF000000;
  static final int BLACK = 0xFF000000;
  static final int BLOCK_SIDE_TOP = 1; // Minecraft says so

  static final ConcurrentHashMap<String, Integer> knownColors = new ConcurrentHashMap<String, Integer>();

  public static void reset() {
    knownColors.clear();
  }

  public static IIcon getBlockTopIcon(final World world, final int x, int y, final int z) {
    return world.getBlock(x, y, z).getIcon(BLOCK_SIDE_TOP, world.getBlockMetadata(x, y, z));
  }

  public static int averageBlockColor(final World world, final int x, int y, final int z) {
    IIcon icon = null;
    while ((y > 0) && ((icon = getBlockTopIcon(world, x, y, z)) == null)) {
      --y;
    }
    if (icon != null) {
      return knownColors.computeIfAbsent(icon.getIconName(), generateAverageBlockColor);
    } else {
//      FMLLog.warning("Unable to find any blocks at %d, %d", x, z);
    }
    return BLACK;
  }

  public static int calculateAverageColor(final int[] pixels) {
    int red = 0;
    int green = 0;
    int blue = 0;

    int pixelCount = 0;
    for (final int pixel : pixels) {
      if ((pixel & ALPHA_FULL) != 0) { // ignore fully transparent pixels
        ++pixelCount;
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

  static final Function<String, Integer> generateAverageBlockColor = new Function<String, Integer>() {

    @Override
    public Integer apply(final String blockTexture) {
      final int[] pixels = loadTexture(blockTexture);
      if (pixels != null) {
        return calculateAverageColor(pixels);
      }
      return BLACK; // otherwise the calculation will be repeated many times
    }
  };
}
