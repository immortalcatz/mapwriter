package mapwriter;

import cpw.mods.fml.common.FMLLog;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class Texture {

  public final int width;
  public final int height;
  final IntBuffer pixelBuf;
  final AtomicBoolean requiresUpdate = new AtomicBoolean(false);
  int id = -1;

  // allocate new texture and fill from IntBuffer
  public Texture(final int width, final int height) {
    this.width = width;
    this.height = height;
    this.pixelBuf = MwUtil.allocateDirectIntBuffer(width * height);

    this.id = GL11.glGenTextures();
    if (this.id > 0) {
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    } else {
      FMLLog.severe("Unable to generate new texture");
      throw new IllegalStateException("Unable to create new texture");
    }
  }

  // create from existing texture
  public Texture(final int id) {
    this.id = id;
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
    this.width = Render.getTextureWidth();
    this.height = Render.getTextureHeight();
    this.pixelBuf = MwUtil.allocateDirectIntBuffer(this.width * this.height);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
  }

  // free up the resources used by the GL texture
  public void dispose() {
    if (this.id > 0) {
      GL11.glDeleteTextures(this.id);
      this.id = -1;
    }
  }

  // Copy a rectangular sub-region of dimensions 'w' x 'h' from the pixel buffer to the array 'pixels'.
  public void getRGB(final int x, final int y, final int width, final int height, final int[] pixels, final int offset) {
    final int bufOffset = (y * this.width) + x;
    synchronized (this.pixelBuf) {
      for (int i = 0; i < height; ++i) {
        this.pixelBuf.position(bufOffset + (i * this.width));
        this.pixelBuf.get(pixels, offset + (i * width), width);
      }
    }
  }

  // Copy a rectangular sub-region of dimensions 'w' x 'h' from the array 'pixels' to the pixel buffer.
  public void setRGB(final int x, final int y, final int width, final int height, final int[] pixels, final int offset) {
    final int bufferOffset = (y * this.width) + x;
    synchronized (this.pixelBuf) {
      for (int line = 0; line < height; ++line) {
        this.pixelBuf.position(bufferOffset + (line * this.width));
        this.pixelBuf.put(pixels, offset + (line * width), width);
      }
      requiresUpdate.set(true);
    }
  }

  public void bind() {
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
    this.updateTextureData();
  }

  public void unbind() {
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
  }

  protected void updateTextureData() {
    if (requiresUpdate.compareAndSet(true, false)) {
      synchronized (this.pixelBuf) {
        this.pixelBuf.rewind();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, this.width, this.height, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, this.pixelBuf);
      }
    }
  }

// copy pixels from GL texture to pixelBuf
  public void fetchTextureData() {
    this.bind();
    synchronized (this.pixelBuf) {
      this.pixelBuf.clear();
      GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, this.pixelBuf);
      // getTexImage does not seem to advance the buffer position, so flip does not work here
      this.pixelBuf.limit(this.width * this.height);
    }
    this.unbind();
  }
}
