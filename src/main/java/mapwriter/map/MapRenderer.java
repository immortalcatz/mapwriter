package mapwriter.map;

import java.awt.Point;
import mapwriter.Config;
import mapwriter.Mw;
import mapwriter.Render;
import mapwriter.gui.MapView;
import mapwriter.map.mapmode.MapMode;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class MapRenderer {
 
  private final MapMode mapMode;
  private final MapView mapView;
  // accessed by the MwGui to check whether the mouse cursor is near the
  // player arrow on the rendered map
  public Point.Double playerArrowScreenPos = new Point.Double(0, 0);

  private final ResourceLocation backgroundTexture = new ResourceLocation("mapwriter", "textures/map/background.png");
  private final ResourceLocation roundMapTexture = new ResourceLocation("mapwriter", "textures/map/border_round.png");
  private final ResourceLocation squareMapTexture = new ResourceLocation("mapwriter", "textures/map/border_square.png");
  private final ResourceLocation playerArrowTexture = new ResourceLocation("mapwriter", "textures/map/arrow_player.png");
  private final ResourceLocation northArrowTexture = new ResourceLocation("mapwriter", "textures/map/arrow_north.png");

  public MapRenderer(final MapMode mapMode, final MapView mapView) {
    this.mapMode = mapMode;
    this.mapView = mapView;
  }

  private void drawMap() {
    GL11.glPushMatrix();

    if (this.mapMode.rotate) {
      GL11.glRotated(Mw.instance.mapRotationDegrees, 0.0f, 0.0f, 1.0f);
    }
    if (this.mapMode.circular) {
      Render.setCircularStencil(0, 0, this.mapMode.h / 2.0);
    }

    Mw.instance.getRegionManager(mapView.getDimensionID());

    if (this.mapMode.circular) {
      Render.disableStencil();
    }

    GL11.glPopMatrix();
  }

  private void drawPlayerArrow() {
    GL11.glPushMatrix();
    double scale = 1.0;
    Point.Double p = this.mapMode.getClampedScreenXY(this.mapView, Mw.instance.playerX * scale, Mw.instance.playerZ * scale);
    this.playerArrowScreenPos.setLocation(p.x + this.mapMode.xTranslation, p.y + this.mapMode.yTranslation);

    // the arrow only needs to be rotated if the map is NOT rotated
    GL11.glTranslated(p.x, p.y, 0.0);
    if (!this.mapMode.rotate) {
      GL11.glRotated(-Mw.instance.mapRotationDegrees, 0.0f, 0.0f, 1.0f);
    }

    double arrowSize = this.mapMode.playerArrowSize;
    Render.setColour(0xffffffff);
    Mw.instance.mc.renderEngine.bindTexture(this.playerArrowTexture);
    Render.drawTexturedRect(
            -arrowSize, -arrowSize, arrowSize * 2, arrowSize * 2,
            0.0, 0.0, 1.0, 1.0
    );
    GL11.glPopMatrix();
  }

  private void drawIcons() {
    GL11.glPushMatrix();

    if (this.mapMode.rotate) {
      GL11.glRotated(Mw.instance.mapRotationDegrees, 0.0f, 0.0f, 1.0f);
    }

    // draw markers
    Mw.instance.markerManager.drawMarkers(this.mapMode, this.mapView);

    // draw player trail
    if (Mw.instance.playerTrail.enabled) {
      Mw.instance.playerTrail.draw(this.mapMode, this.mapView);
    }

    // draw north arrow
    if (this.mapMode.rotate) {
      double y = this.mapMode.h / 2.0;
      double arrowSize = this.mapMode.playerArrowSize;
      Render.setColour(0xffffffff);
      Mw.instance.mc.renderEngine.bindTexture(this.northArrowTexture);
      Render.drawTexturedRect(
              -arrowSize, -y - (arrowSize * 2), arrowSize * 2, arrowSize * 2,
              0.0, 0.0, 1.0, 1.0
      );
    }

    GL11.glPopMatrix();

    // outside of the matrix pop as theplayer arrow
    // needs to be drawn without rotation
    this.drawPlayerArrow();
  }

  private void drawCoords() {
    // draw coordinates
    if (this.mapMode.coordsEnabled) {
      GL11.glPushMatrix();
      GL11.glTranslatef(this.mapMode.textX, this.mapMode.textY, 0);
      if (Config.instance.coordsMode != 2) {
        GL11.glScalef(0.5f, 0.5f, 1.0f);
      }
      int offset = 0;
      if (Config.instance.coordsMode > 0) {
        Render.drawCentredString(0, 0, this.mapMode.textColour,
                "%d, %d, %d",
                Mw.instance.playerXInt,
                Mw.instance.playerYInt,
                Mw.instance.playerZInt
        );
        offset += 12;
      }
      if (Config.instance.undergroundMode) {
        Render.drawCentredString(
                0, offset, this.mapMode.textColour, "underground mode"
        );
      }
      GL11.glPopMatrix();
    }
  }

  public void draw() {

    this.mapMode.setScreenRes();

    GL11.glPushMatrix();
    GL11.glLoadIdentity();

    // translate to center of minimap
    // z is -2000 so that it is drawn above the 3D world, but below GUI
    // elements which are typically at -3000
    GL11.glTranslated(this.mapMode.xTranslation, this.mapMode.yTranslation, -2000.0);

    // draw background, the map texture, and enabled overlays
    this.drawMap();
    this.drawIcons();
    this.drawCoords();

    // some shader mods seem to need depth testing re-enabled
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glPopMatrix();
  }
}
