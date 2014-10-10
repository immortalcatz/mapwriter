/*
 */
package mapwriter.mapgen;

import cpw.mods.fml.common.FMLLog;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Two
 */
public class Region {

  public static final int CHUNK_SIZE = 16; // chunk size in Minecraft
  public static final int REGION_SIZE = 1024; // must be a power of 2
  public static final int CHUNKPOS_MASK = REGION_SIZE / CHUNK_SIZE - 1;
  public static final int REGION_DIVISOR = Region.REGION_SIZE / Region.CHUNK_SIZE;
  public static final String IMAGE_TYPE = "png";

  final int[] pixels = new int[REGION_SIZE * REGION_SIZE]; // access needs to be thread-safe

  public static Region fromFile(final Path filepath) {
    if (Files.exists(filepath)) {
      InputStream in = null;
      try {
        in = Files.newInputStream(filepath, StandardOpenOption.READ);
        final BufferedImage image = ImageIO.read(in);
        if ((image.getWidth() != REGION_SIZE) || (image.getHeight() != REGION_SIZE)) {
          Files.delete(filepath); // kill it with fire!
          throw new IOException("Image data is of wrong size. Expected " + REGION_SIZE + "x" + REGION_SIZE + " but got " + image.getWidth() + "x" + image.getHeight());
        }
        return new Region(image.getRGB(0, 0, REGION_SIZE, REGION_SIZE, null, 0, REGION_SIZE));
      } catch (Exception e) {
        FMLLog.warning("Unable to read map data file '%s': %s", filepath.getFileName().toString(), e.toString());
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (Exception e) {
          }
        }
      }
    }
    return new Region();
  }

  public Region() {
  }

  public Region(final int[] newPixels) {
    Objects.requireNonNull(newPixels);
    if (newPixels.length != this.pixels.length) {
      throw new IllegalArgumentException("Pixel size does not match. Expected " + this.pixels.length + " but got " + newPixels.length);
    }
    System.arraycopy(newPixels, 0, this.pixels, 0, this.pixels.length);
  }

  public void setChunk(final Chunk chunk, final int[] newPixels) {
    setChunk(chunk.xPosition, chunk.zPosition, newPixels);
  }

  public void setChunk(final int chunkX, final int chunkZ, final int[] newPixels) {
    modifyPixels((chunkX * CHUNK_SIZE) & CHUNKPOS_MASK, (chunkZ * CHUNK_SIZE) & CHUNKPOS_MASK, CHUNK_SIZE, CHUNK_SIZE, newPixels);
  }

  public void modifyPixels(final int offsetX, final int offsetZ, final int width, final int height, final int[] newPixels) {
    synchronized (pixels) {
      for (int line = 0; line < height; ++line) {
        System.arraycopy(newPixels, line * width, this.pixels, (offsetZ + line) * REGION_SIZE + offsetX, width);
      }
    }
  }

  public boolean saveAs(final Path filepath) {
    final BufferedImage image = this.asImage();
    OutputStream out = null;
    try {
      out = Files.newOutputStream(filepath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      ImageIO.write(image, IMAGE_TYPE, out);
      return true;
    } catch (Exception e) {
      FMLLog.severe("Unable to write map data file '%s': %s", filepath.getFileName().toString(), e.toString());
      return false;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
        }
      }
    }
  }

  public BufferedImage asImage() {
    final BufferedImage result = new BufferedImage(REGION_SIZE, REGION_SIZE, BufferedImage.TYPE_INT_ARGB);
    synchronized (pixels) {
      result.setRGB(0, 0, REGION_SIZE, REGION_SIZE, pixels, 0, REGION_SIZE);
    }
    return result;
  }

}
