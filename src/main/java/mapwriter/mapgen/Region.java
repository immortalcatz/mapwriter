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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import mapwriter.Render;
import mapwriter.Texture;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

/**
 * @author Two
 */
public class Region {

  public static final int CHUNK_SIZE = 16; // chunk size in Minecraft
  public static final int REGION_SIZE = 1024; // must be a power of 2
  public static final int CHUNKS_IN_REGION = REGION_SIZE / CHUNK_SIZE;
  public static final String IMAGE_TYPE = "png";

  public final RegionID regionID;
  final Texture texture;
  final AtomicBoolean requireTextureUpdate = new AtomicBoolean(true);

  public Region(final RegionID regionID) {
    Objects.requireNonNull(regionID);
    this.regionID = regionID;
    this.texture = new Texture(REGION_SIZE, REGION_SIZE);
  }

  public void updateChunk(final Chunk chunk, final int[] newPixels) {
    updateChunk(chunk.xPosition, chunk.zPosition, newPixels);
  }

  public void updateChunk(final int chunkX, final int chunkZ, final int[] newPixels) {
    this.texture.setRGB(Math.floorMod(chunkX, CHUNKS_IN_REGION) * CHUNK_SIZE, (CHUNKS_IN_REGION - Math.floorMod(chunkZ, CHUNKS_IN_REGION) - 1) * CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, newPixels, 0);
  }

  public void setRGB(final int[] newPixels) {
    this.texture.setRGB(newPixels);
  }

  public boolean save(final String saveDir) {
    return saveAs(Paths.get(saveDir, regionID.toFilename()));
  }

  public boolean saveAs(final Path filepath) {
    final BufferedImage image = this.texture.asImage();
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

  public void dispose() {
    this.texture.dispose();
  }

  public void draw() {
    GL11.glPushMatrix();
    this.translateToTile();
    this.texture.bind();
    Render.drawTexturedRect(0, 0, this.texture.width, this.texture.height);
    this.texture.unbind();
    GL11.glPopMatrix();
  }

  protected void translateToTile() {
    GL11.glTranslatef(regionID.x, regionID.z, 0.0f);
  }

}
