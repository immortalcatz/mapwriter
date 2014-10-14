package mapwriter;

import mapwriter.util.PixelData;
import cpw.mods.fml.common.FMLLog;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class Texture extends PixelData {

  final IntBuffer pixelBuf;
  int id;

  public static Texture fromMemory(final int id) {
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    final Texture result = new Texture(Render.getTextureWidth(), Render.getTextureWidth(), id);
    result.fetchTextureData();
    return result;
  }

  // allocate new texture and fill from IntBuffer
  public Texture(final int width, final int height) {
    this(width, height, -1);
  }

  // create from existing texture
  public Texture(final int width, final int height, final int id) {
    super(width, height);

    final int maxSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
    if (size > maxSize) {
      throw new IllegalArgumentException("Texture size is too big. Must be <= " + maxSize + ", but got " + size);
    }
    this.pixelBuf = MwUtil.newDirectIntBuffer(width * height);

    if (id < 0) {
      this.id = GL11.glGenTextures();
      if (this.id <= 0) {
        FMLLog.severe("Unable to generate new texture");
        throw new IllegalStateException("Unable to create new texture");
      }
    } else {
      this.id = id;
    }
    
    if (this.id > 0) {
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
  }

  // free up the resources used by the GL texture
  public void dispose() {
    if (this.id > 0) {
      GL11.glDeleteTextures(this.id);
      this.id = -1;
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
    if (hasChanged.compareAndSet(true, false)) {
      synchronized (this.pixels) {
        this.pixelBuf.clear();
        this.pixelBuf.put(this.pixels);
        this.pixelBuf.flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, this.width, this.height, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, this.pixelBuf);
      }
    }
  }

  public void fetchTextureData() {
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
    synchronized (this.pixels) {
      this.pixelBuf.clear();
      GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, this.pixelBuf);
      // getTexImage does not seem to advance the buffer position, so flip does not work here
      this.pixelBuf.limit(this.width * this.height);
      this.pixelBuf.position(0);
      this.pixelBuf.get(this.pixels);
    }
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
  }
}
