package mapwriter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

/*
 MwRender contains most of the code for drawing the overlay.
 This includes:
 - loading textures from images
 - saving textures to images
 - allocating and setting up GL textures
 - drawing coloured and textured quads (using minecraft Tesselator class)
 
 */
public class Render {

  public static double zDepth = 0.0D;
  public static final double circleSteps = 30.0;

  public static void setColourWithAlphaPercent(int colour, int alphaPercent) {
    setColor(((((alphaPercent * 0xff) / 100) & 0xff) << 24) | (colour & 0xffffff));
  }

  public static void setColor(int colour) {
    GL11.glColor4f(
            (float) ((colour >> 16) & 0xff) / 255.0f,
            (float) ((colour >> 8) & 0xff) / 255.0f,
            (float) ((colour) & 0xff) / 255.0f,
            (float) ((colour >> 24) & 0xff) / 255.0f);
  }

  public static void resetColour() {
    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
  }

  public static int getTextureWidth() {
    return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
  }

  public static int getTextureHeight() {
    return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
  }

  public static int getBoundTextureId() {
    return GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
  }

  public static int getMaxTextureSize() {
    return GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
  }

  /*
   Drawing Methods
	
   Note that EntityRenderer.setupOverlayRendering must be called before drawing for the scene
   to appear correctly on the overlay.
   If these functions are called from the hookUpdateCameraAndRender method of Mw this
   will have already been done.
   */
  // draw rectangle with texture stretched to fill the shape
  public static void drawTexturedRect(double x, double y, double w, double h) {
    drawTexturedRect(x, y, w, h, 0.0D, 0.0D, 1.0D, 1.0D);
  }

  // draw rectangle with texture UV coordinates specified (so only part of the texture fills the rectangle).
  public static void drawTexturedRect(double x, double y, double w, double h, double u1, double v1, double u2, double v2) {
    try {
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      Tessellator tes = Tessellator.instance;
      tes.startDrawingQuads();
      tes.addVertexWithUV(x + w, y, zDepth, u2, v1);
      tes.addVertexWithUV(x, y, zDepth, u1, v1);
      tes.addVertexWithUV(x, y + h, zDepth, u1, v2);
      tes.addVertexWithUV(x + w, y + h, zDepth, u2, v2);
      tes.draw();
      GL11.glDisable(GL11.GL_BLEND);
    } catch (NullPointerException e) {
      Mw.log.error("MwRender.drawTexturedRect", e);
    }
  }

  public static void drawArrow(double x, double y, double angle, double length) {
    // angle the back corners will be drawn at relative to the pointing angle
    double arrowBackAngle = 0.75D * Math.PI;
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    Tessellator tes = Tessellator.instance;
    tes.startDrawing(GL11.GL_TRIANGLE_FAN);
    tes.addVertex(x + (length * Math.cos(angle)), y + (length * Math.sin(angle)), zDepth);
    tes.addVertex(x + (length * 0.5D * Math.cos(angle - arrowBackAngle)), y + (length * 0.5D * Math.sin(angle - arrowBackAngle)), zDepth);
    tes.addVertex(x + (length * 0.3D * Math.cos(angle + Math.PI)), y + (length * 0.3D * Math.sin(angle + Math.PI)), zDepth);
    tes.addVertex(x + (length * 0.5D * Math.cos(angle + arrowBackAngle)), y + (length * 0.5D * Math.sin(angle + arrowBackAngle)), zDepth);
    tes.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_BLEND);
  }

  public static void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    Tessellator tes = Tessellator.instance;
    tes.startDrawing(GL11.GL_TRIANGLES);
    tes.addVertex(x1, y1, zDepth);
    tes.addVertex(x2, y2, zDepth);
    tes.addVertex(x3, y3, zDepth);
    tes.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_BLEND);
  }

  public static void drawRect(double x, double y, double w, double h) {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    Tessellator tes = Tessellator.instance;
    tes.startDrawingQuads();
    tes.addVertex(x + w, y, zDepth);
    tes.addVertex(x, y, zDepth);
    tes.addVertex(x, y + h, zDepth);
    tes.addVertex(x + w, y + h, zDepth);
    tes.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_BLEND);
  }

  public static void drawCircle(double x, double y, double r) {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    Tessellator tes = Tessellator.instance;
    tes.startDrawing(GL11.GL_TRIANGLE_FAN);
    tes.addVertex(x, y, zDepth);
    // for some the circle is only drawn if theta is decreasing rather than ascending
    double end = Math.PI * 2.0;
    double incr = end / circleSteps;
    for (double theta = -incr; theta < end; theta += incr) {
      tes.addVertex(x + (r * Math.cos(-theta)), y + (r * Math.sin(-theta)), zDepth);
    }
    tes.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_BLEND);
  }

  public static void drawCircleBorder(double x, double y, double r, double width) {
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    Tessellator tes = Tessellator.instance;
    tes.startDrawing(GL11.GL_TRIANGLE_STRIP);
    // for some the circle is only drawn if theta is decreasing rather than ascending
    double end = Math.PI * 2.0;
    double incr = end / circleSteps;
    double r2 = r + width;
    for (double theta = -incr; theta < end; theta += incr) {
      tes.addVertex(x + (r * Math.cos(-theta)), y + (r * Math.sin(-theta)), zDepth);
      tes.addVertex(x + (r2 * Math.cos(-theta)), y + (r2 * Math.sin(-theta)), zDepth);
    }
    tes.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_BLEND);
  }

  public static void drawRectBorder(double x, double y, double w, double h, double bw) {
    // top border
    Render.drawRect(x - bw, y - bw, w + bw + bw, bw);
    // bottom border
    Render.drawRect(x - bw, y + h, w + bw + bw, bw);
    // left border
    Render.drawRect(x - bw, y, bw, h);
    // right border
    Render.drawRect(x + w, y, bw, h);
  }

  public static void drawString(int x, int y, int colour, String formatString, Object... args) {
    Minecraft mc = Minecraft.getMinecraft();
    //mc.renderEngine.resetBoundTexture();
    FontRenderer fr = mc.fontRenderer;
    String s = String.format(formatString, args);
    fr.drawStringWithShadow(s, x, y, colour);
  }

  public static void drawCentredString(int x, int y, int colour, String formatString, Object... args) {
    Minecraft mc = Minecraft.getMinecraft();
    //mc.renderEngine.resetBoundTexture();
    FontRenderer fr = mc.fontRenderer;
    String s = String.format(formatString, args);
    int w = fr.getStringWidth(s);
    fr.drawStringWithShadow(s, x - (w / 2), y, colour);
  }
  
  protected static void stencilBegin() {
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    // disable drawing to the color buffer.
    // circle will only be drawn to depth buffer.
    GL11.glColorMask(false, false, false, false);
    // enable writing to depth buffer
    GL11.glDepthMask(true);

    // Clearing the depth buffer causes problems with shader mods.
    // I guess we just have to hope that the rest of the depth buffer
    // contains z values greater than 2000 at this stage in the frame
    // render.
    // It would be much easier to use the stencil buffer instead, but it is
    // not specifically requested in the Minecraft LWJGL display setup code.
    // So the stencil buffer is only available on GL implementations that
    // set it up by default.
    // clear depth buffer to z = 3000.0
    //GL11.glClearDepth(3000.0);
    //GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    // always write to depth buffer
    GL11.glDepthFunc(GL11.GL_ALWAYS);

    // draw stencil pattern (filled circle at z = 1000.0)
    Render.setColor(0xffffffff);
    Render.zDepth = 1000.0;
  }
  
  protected static void stencilEnd() {
    Render.zDepth = 0.0;

    // re-enable drawing to colour buffer
    GL11.glColorMask(true, true, true, true);
    // disable drawing to depth buffer
    GL11.glDepthMask(false);
    // only draw pixels with z values that are greater
    // than the value in the depth buffer.
    // The overlay is drawn at 2000 so this will pass inside
    // the circle (2000 > 1000) but not outside (2000 <= 3000).
    GL11.glDepthFunc(GL11.GL_GREATER);
  }

  public static void setRectangularStencil(final double x, final double y, final double width, final double height) {
    stencilBegin();
    Render.drawRect(x, y, width, height);
    stencilEnd();
  }

  public static void setCircularStencil(final double x, final double y, final double r) {
    stencilBegin();
    Render.drawCircle(x, y, r);
    stencilEnd();
  }

  public static void disableStencil() {
    GL11.glDepthMask(true);
    GL11.glDepthFunc(GL11.GL_LEQUAL);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
  }

  // A better implementation of a circular stencil using the stencil buffer
  // rather than the depth buffer can be found below. It works only on GL
  // implementations that attach a stencil buffer by default (e.g. Intel, but
  // not on Nvidia).
  //
  // To fix this we would need to change the display create line in
  // 'Minecraft.java' file from:
  //   Display.create((new PixelFormat()).withDepthBits(24));
  // to:
  //   Display.create((new PixelFormat()).withDepthBits(24).withStencilBits(8));
  //
  // Then we could use the stencil buffer and the the circular map would have
  // far less problems.
  //
  // I suppose it would also be possible to detect the number of stencil bits
  // available at runtime using GL11.glGetInteger(GL11.GL_STENCIL_BITS) and
  // only use the depth buffer stencil algorithm if it returns 0. But this
  // doesn't solve the problem of the stencil buffer not being initialized by
  // default on some systems.
  /*public static void setCircularStencil(double x, double y, double r) {
   GL11.glEnable(GL11.GL_STENCIL_TEST);
   // disable drawing to the color and depth buffers.
   // circle will only be drawn to stencil buffer.
   GL11.glColorMask(false, false, false, false);
   GL11.glDepthMask(false);
   // set up stencil func and op so that a 1 is always written to the stencil buffer
   // whenever a pixel is drawn.
   GL11.glStencilFunc(GL11.GL_NEVER, 1, 0x01);
   // replace stencil buffer value with 1 whenever stencil test fails.
   // keep stencil buffer value otherwise.
   GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
   // enable writing to 8 bits of the stencil buffer
   GL11.glStencilMask(0x01);
   // clear stencil buffer, with mask 0xff
   GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
   // draw stencil pattern
   Render.setColour(0xffffffff);
   Render.drawCircle(x, y, r);
		
   // re-enable drawing to colour and depth buffers
   GL11.glColorMask(true, true, true, true);
   // probably shouldn't enable? -> GL11.glDepthMask(true);
   // disable writing to stencil buffer
   GL11.glStencilMask(0x00);
   // draw only when stencil buffer value == 1 (inside circle)
   GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0x01);
   }
	
   public static void disableStencil() {
   GL11.glDisable(GL11.GL_STENCIL_TEST);
   }*/
}
