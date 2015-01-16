/*
 */
package mapwriter.gui;

import mapwriter.Config;
import mapwriter.Mw;
import mapwriter.Render;
import mapwriter.util.MwUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

/**
 * @author Two
 */
public class MapDisplay {

  protected final AreaMap areaMap;
  protected final Rectangle position = new Rectangle(0, 0, 1, 1);
  protected boolean circular = false;
  protected boolean rotating = false;
  protected int zoomLevel = 0;
  protected MapAnchor mapAnchor = MapAnchor.center;
  protected int margin = 0;

  public MapDisplay(final AreaMap areaMap) {
    this.areaMap = areaMap;
  }

  public void centerMapOnPlayer() {
    this.centerMapOn(Mw.instance.player.x, Mw.instance.player.z);
  }

  public void centerMapOn(final double x, final double z) {
    areaMap.setCenter(x, z);
  }

  public double getMapCenterX() {
    return areaMap.getCenterX();
  }

  public double getMapCenterZ() {
    return areaMap.getCenterZ();
  }

  public int getBlockCoordinateX(final int screenX) {
    return Math.round((screenX - position.getX()) * ((float) areaMap.getWidth() / (float) position.getWidth()));
  }

  public int getBlockCoordinateY(final int screenY) {
    return Math.round((screenY - position.getY()) * ((float) areaMap.getHeight() / (float) position.getHeight()));
  }

  public void setMapAnchor(final MapAnchor newMapAnchor) {
    if (newMapAnchor != mapAnchor) {
      this.mapAnchor = newMapAnchor;
      this.updateRelativePosition();
    }
  }

  public MapAnchor getMapAnchor() {
    return this.mapAnchor;
  }

  public void updateRelativePosition() {
    final int screenWidth = Minecraft.getMinecraft().displayWidth;
    final int screenHeight = Minecraft.getMinecraft().displayHeight;

    switch (this.mapAnchor) {
      case topLeft:
        this.setCenter(0, screenHeight);
        break;
      case topRight:
        this.setCenter(screenWidth, screenHeight);
        break;
      case bottomLeft:
        this.setCenter(0, 0);
        break;
      case bottomRight:
        this.setCenter(screenWidth, 0);
        break;
      case center:
        this.setCenter(0, 0);
        break;
    }
  }

  public void setCenter(final int x, final int y) {
    this.setCenter(x, y, this.margin);
  }

  public void setCenter(final int x, final int y, final int margin) {
    this.margin = margin;
    this.setX(x - position.getWidth() / 2);
    this.setY(y - position.getHeight() / 2);
  }

  public int getCenterX() {
    return position.getX() + position.getWidth() / 2;
  }

  public int getCenterY() {
    return position.getY() + position.getHeight() / 2;
  }

  public void setSize(final int newWidth, final int newHeight, final int margin) {
    if ((position.getWidth() != newWidth) || (position.getHeight() != newHeight) || (this.margin != margin)) {
      this.margin = margin;
      final int centerX = getCenterX();
      final int centerY = getCenterY();
      position.setWidth(newWidth);
      position.setHeight(newHeight);
      setCenter(centerX, centerY, margin);

      this.updateAreaMapSize();
    }
  }

  public int getHeight() {
    return position.getHeight();
  }

  public int getWidth() {
    return position.getWidth();
  }

  protected void setX(final int x) {
    final int newX = MwUtil.withinBounds(x, margin, Minecraft.getMinecraft().displayWidth - position.getWidth() - this.margin);
    position.setX(newX);
  }

  protected void setY(final int y) {
    final int newY = MwUtil.withinBounds(y, margin, Minecraft.getMinecraft().displayHeight - position.getHeight() - this.margin);
    position.setY(newY);
  }

  public void setDimensionID(final int dimensionID) {
    this.areaMap.setDimensionID(dimensionID);
  }

  public int getDimensionID() {
    return this.areaMap.getDimensionID();
  }

  public void draw() {
    GL11.glPushMatrix();
    this.updateRelativePosition();
    this.translateToCenter();
    // draw background, the map texture, and enabled overlays
    this.drawMap();
    this.drawMarkers();
    this.drawCoords();

    GL11.glPopMatrix();
  }

  protected void translateToCenter() {
//    GL11.glTranslated(this.position.getX() + this.position.getWidth() / 2, this.position.getY() + this.position.getHeight() / 2, -2000.0);
  }

  protected void rotate() {
    if (this.isRotating()) {
      GL11.glRotated(Mw.instance.getMapRotationDegrees(), 0.0f, 0.0f, 1.0f);
    }
  }

  protected void setStencil(final boolean enabled) {
    if (enabled) {
      if (this.isCircular()) {
        Render.setCircularStencil(0, 0, this.position.getWidth() / 2.0);
      } else {
        Render.setRectangularStencil(-this.position.getWidth() / 2, -this.position.getHeight() / 2, this.position.getWidth(), this.position.getHeight());
      }
    } else {
      Render.disableStencil();
    }
  }

  protected void drawMap() {
    GL11.glPushMatrix();

//    this.rotate();
//    this.setStencil(true);
    areaMap.renderMap();

//    this.setStencil(false);
    GL11.glPopMatrix();
  }

  protected void drawPlayerArrow() {
//    GL11.glPushMatrix();
//    double scale = 1.0;
//    Point.Double p = this.mapMode.getClampedScreenXY(this.mapView, Mw.instance.playerX * scale, Mw.instance.playerZ * scale);
//    this.playerArrowScreenPos.setLocation(p.x + this.mapMode.xTranslation, p.y + this.mapMode.yTranslation);
//
//    // the arrow only needs to be rotated if the map is NOT rotated
//    GL11.glTranslated(p.x, p.y, 0.0);
//    if (this.mapDisplay.isRotating() == false) {
//      GL11.glRotated(-Mw.instance.mapRotationDegrees, 0.0f, 0.0f, 1.0f);
//    }
//
//    double arrowSize = this.mapMode.playerArrowSize;
//    Render.setColour(0xffffffff);
//    Mw.instance.mc.renderEngine.bindTexture(this.playerArrowTexture);
//    Render.drawTexturedRect(
//            -arrowSize, -arrowSize, arrowSize * 2, arrowSize * 2,
//            0.0, 0.0, 1.0, 1.0
//    );
//    GL11.glPopMatrix();
  }

  protected void drawMarkers() {
//    GL11.glPushMatrix();
//
//    if (this.mapMode.rotate) {
//      GL11.glRotated(Mw.instance.mapRotationDegrees, 0.0f, 0.0f, 1.0f);
//    }
//
//    // draw markers
//    Mw.instance.markerManager.drawMarkers(this.mapMode, this.mapView);
//
//    // draw player trail
//    if (Mw.instance.playerTrail.enabled) {
//      Mw.instance.playerTrail.draw(this.mapMode, this.mapView);
//    }
//
//    // draw north arrow
//    if (this.mapMode.rotate) {
//      double y = this.mapMode.h / 2.0;
//      double arrowSize = this.mapMode.playerArrowSize;
//      Render.setColour(0xffffffff);
//      Mw.instance.mc.renderEngine.bindTexture(this.northArrowTexture);
//      Render.drawTexturedRect(
//              -arrowSize, -y - (arrowSize * 2), arrowSize * 2, arrowSize * 2,
//              0.0, 0.0, 1.0, 1.0
//      );
//    }
//
//    GL11.glPopMatrix();
//
//    // outside of the matrix pop as theplayer arrow
//    // needs to be drawn without rotation
//    this.drawPlayerArrow();
  }

  protected void drawCoords() {
//    // draw coordinates
//    if (this.mapMode.coordsEnabled) {
//      GL11.glPushMatrix();
//      GL11.glTranslatef(this.mapMode.textX, this.mapMode.textY, 0);
//      if (Config.instance.coordsMode != 2) {
//        GL11.glScalef(0.5f, 0.5f, 1.0f);
//      }
//      int offset = 0;
//      if (Config.instance.coordsMode > 0) {
//        Render.drawCentredString(0, 0, this.mapMode.textColour,
//                "%d, %d, %d",
//                Mw.instance.playerXInt,
//                Mw.instance.playerYInt,
//                Mw.instance.playerZInt
//        );
//        offset += 12;
//      }
//      GL11.glPopMatrix();
//    }
  }

  public boolean isCircular() {
    return circular;
  }

  public void setCircular(final boolean circular) {
    this.circular = circular;
    if (circular == false) {
      this.setRotating(false);
    }
  }

  public boolean isRotating() {
    return rotating;
  }

  public void setRotating(final boolean rotating) {
    this.rotating = rotating;
    if (rotating) {
      setCircular(true);
    }
  }

  public int getZoomLevel() {
    return zoomLevel;
  }

  public void setZoomLevel(final int zoomLevel) {
    if (this.zoomLevel != zoomLevel) {
      this.zoomLevel = zoomLevel;
      this.updateAreaMapSize();
    }
  }

  protected void updateAreaMapSize() {
    this.areaMap.setWidth(this.getWidth() * (zoomLevel + 1));
    this.areaMap.setHeight(this.getHeight() * (zoomLevel + 1));
  }

  public void modifyZoomLevel(final int amount) {
    this.setZoomLevel(this.getZoomLevel() + amount);
  }

}
