/*
 */
package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import java.util.concurrent.ConcurrentHashMap;
import mapwriter.Texture;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
  static final int BLOCK_SIDE_TOP = 1; // Minecraft says so

  static final ConcurrentHashMap<Integer, Integer> knownColors = new ConcurrentHashMap<Integer, Integer>();
  static Texture terrainTexture = null;

  public static void reset() {
    final int terrainTextureId = Minecraft.getMinecraft().renderEngine.getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
    if (terrainTextureId > 0) {
      terrainTexture = Texture.fromMemory(terrainTextureId);
      terrainTexture.fetchTextureData();
    } else {
      FMLLog.severe("Unable to get Minecraft terrain texture.");
    }
    knownColors.clear();
  }

  public static int compressBlockIdAndMeta(final Block block, final int metadata) {
    return compressBlockIdAndMeta(Block.getIdFromBlock(block), metadata);
  }

  public static int compressBlockIdAndMeta(final int blockID, final int metadata) {
    if ((metadata < 0) || (metadata > 0xF)) {
      throw new IllegalArgumentException("Metadata is invalid. Must be 0 <= meta <= 0xF, but got " + metadata);
    }
    if ((blockID < 0) || (blockID > 0x0FFFFFFF)) {
      throw new IllegalArgumentException("Metadata is invalid. Must be 0 <= block <= 0x0FFFFFFF, but got " + blockID);
    }
    return ((blockID << 0xF) | metadata);
  }

  public static Texture getTerrainTexture() {
    return terrainTexture;
  }

  public static int averageBlockColor(final World world, final int x, int y, final int z) {
    Block block = world.getBlock(x, y, z);
    while ((y > 0) && (block.getMaterial() == Material.air)) {
      --y;
      block = world.getBlock(x, y, z);
    }
    if (y > 0) {
      final int metadata = world.getBlockMetadata(x, y, z);
      final int mapID = compressBlockIdAndMeta(block, metadata);
      Integer result = knownColors.get(mapID);
      if (result == null) {
        final IIcon icon = block.getIcon(BLOCK_SIDE_TOP, metadata);
        if (icon == null) {
          FMLLog.warning("Block %16s has no top texture", block.getUnlocalizedName());
          result = BLACK;
        } else {
          result = averageColor(icon);
          if (result != BLACK) {
            knownColors.put(mapID, result);
          }
        }
      }
//      FMLLog.info("Average block color for %16s: %08X", block.getUnlocalizedName(), result);
      return result;
    } else {
//      FMLLog.warning("Unable to find any blocks at %d, %d", x, z);
    }
    return BLACK;
  }

  public static int averageColor(final IIcon icon) {
    final Texture textureTerrain = getTerrainTexture();
    if (textureTerrain != null) {
      final int iconX = (int) Math.round(((float) terrainTexture.width) * Math.min(icon.getMinU(), icon.getMaxU()));
      final int iconY = (int) Math.round(((float) terrainTexture.height) * Math.min(icon.getMinV(), icon.getMaxV()));
      final int iconWidth = (int) Math.round(((float) terrainTexture.width) * Math.abs(icon.getMaxU() - icon.getMinU()));
      final int iconHeight = (int) Math.round(((float) terrainTexture.height) * Math.abs(icon.getMaxV() - icon.getMinV()));

      final int[] pixels = new int[iconWidth * iconHeight];
      terrainTexture.getRGB(iconX, iconY, iconWidth, iconHeight, pixels, 0);

      return calculateAverageColor(pixels);
    } else {
      FMLLog.severe("Terrain texture not loaded!");
      return BLACK; // if nothing else, the average color is black  for the moment
    }
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
}
