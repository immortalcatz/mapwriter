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
public class MinecraftUtil {

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
