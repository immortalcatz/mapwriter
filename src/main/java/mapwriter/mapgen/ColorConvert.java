/*
 */
package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import java.util.concurrent.ConcurrentHashMap;
import mapwriter.Render;
import mapwriter.Texture;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * @author Two
 */
public class ColorConvert {

  static final int ALPHA_FULL = 0xFF000000;
  static final int BLACK = 0xFF000000;

  static ConcurrentHashMap<Integer, Integer> knownColors = new ConcurrentHashMap<Integer, Integer>();
  static volatile Texture terrainTexture = null;

  public void reset() {
    terrainTexture = null;
    knownColors.clear();
  }

  public static int compressBlockIdAndMeta(final Block block, final int metadata) {
    return compressBlockIdAndMeta(Block.getIdFromBlock(block), metadata);
  }

  public static int compressBlockIdAndMeta(final int blockID, final int metadata) {
    return ((blockID << 0xF) | metadata);
  }

  public static Texture getTerrainTexture() {
    if (terrainTexture == null) {
      final int terrainTextureId = Minecraft.getMinecraft().renderEngine.getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
      if (terrainTextureId != 0) {
        terrainTexture = new Texture(terrainTextureId);
      } else {
        FMLLog.warning("Unable to get texture data");
      }
    }
    return terrainTexture;
  }

  public static int averageBlockColor(final World world, final int x, final int y, final int z) {
    final Block block = world.getBlock(x, y, z);
    final int metadata = world.getBlockMetadata(x, y, z);
    final int mapID = compressBlockIdAndMeta(block, metadata);
    Integer result = knownColors.get(mapID);
    if (result == null) {
      result = averageColor(block.getIcon(1, metadata));
      if (result != BLACK) {
        knownColors.put(mapID, result);
      }
    }
    return result;
  }

  public static int averageColor(final IIcon icon) {
    final Texture textureTerrain = getTerrainTexture();
    if (textureTerrain != null) {
      final int iconX = (int) Math.round(((float) terrainTexture.w) * Math.min(icon.getMinU(), icon.getMaxU()));
      final int iconY = (int) Math.round(((float) terrainTexture.h) * Math.min(icon.getMinV(), icon.getMaxV()));
      final int iconWidth = (int) Math.round(((float) terrainTexture.w) * Math.abs(icon.getMaxU() - icon.getMinU()));
      final int iconHeight = (int) Math.round(((float) terrainTexture.h) * Math.abs(icon.getMaxV() - icon.getMinV()));

      final int[] pixels = new int[iconWidth * iconHeight];
      terrainTexture.getRGB(iconX, iconY, iconWidth, iconHeight, pixels, 0, iconWidth);

      return calculateAverageColor(pixels);
    } else {
      return BLACK; // if nothing else, the average color is black  for the moment
    }
  }

  public static int calculateAverageColor(final int[] pixels) {
    int red = 0;
    int green = 0;
    int blue = 0;

    int pixelCount = 0;
    for (final int pixel : pixels) {
      if ((pixel & ALPHA_FULL) > 0) { // ignore fully transparent pixels
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
}
