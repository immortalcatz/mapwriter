/*
 */
package mapwriter.gui;

import mapwriter.Mw;
import mapwriter.Render;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

/**
 * @author Two
 */
public class MapDisplay {

  protected final MapView mapView;
  protected boolean circular = false;
  protected boolean rotating = false;
  protected int zoomLevel;
  protected Rectangle position;

  public MapDisplay(final MapView mapView) {
    this.mapView = mapView;
  }

  public void centerOnPlayer() {

  }

  public void centerOn(final double x, final double z) {

  }
  
  public void setDimensionID(final int dimensionID) {
    this.mapView.setDimensionID(dimensionID);
  }
  
  public int getDimensionID() {
    return this.mapView.getDimensionID();
  }

  public void draw() {
//    this.mapDisplay.setScreenRes();

    GL11.glPushMatrix();
    GL11.glLoadIdentity();

    this.translateToPosition();

    // draw background, the map texture, and enabled overlays
    this.drawMap();
    this.drawMarkers();
    this.drawCoords();

    GL11.glPopMatrix();
  }

  protected void translateToPosition() {
    GL11.glTranslated(this.position.getX(), this.position.getY(), -2000.0);
  }

  protected void rotate() {
    if (this.isRotating()) {
      GL11.glRotated(Mw.instance.mapRotationDegrees, 0.0f, 0.0f, 1.0f);
    }
  }

  protected void setStencil(final boolean enabled) {
    if (this.isCircular()) {
      if (enabled) {
        Render.setCircularStencil(0, 0, this.position.getWidth() / 2.0);
      } else {
        Render.disableStencil();
      }
    }
  }

  protected void drawMap() {
    GL11.glPushMatrix();

    this.rotate();
    this.setStencil(true);

    mapView.renderMap();

    this.setStencil(false);

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
    this.zoomLevel = zoomLevel;
  }

  public void modifyZoomLevel(final int amount) {
    this.setZoomLevel(this.getZoomLevel() + amount);
  }

}
