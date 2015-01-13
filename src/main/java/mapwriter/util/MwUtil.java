/*
 */
package mapwriter.util;

import cpw.mods.fml.common.FMLLog;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

/**
 * @author Two
 */
public class MwUtil {

  public static double withinBounds(final double d, final double min, final double max) {
    return Math.max(Math.min(d, max), min);
  }

  public static float withinBounds(final float f, final float min, final float max) {
    return Math.max(Math.min(f, max), min);
  }

  public static long withinBounds(final long l, final long min, final long max) {
    return Math.max(Math.min(l, max), min);
  }

  public static int withinBounds(final int i, final int min, final int max) {
    return Math.max(Math.min(i, max), min);
  }

  public static int[] loadTexture(final String blockTexture) {
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
}
