/*
 */
package mapwriter.gui;

import mapwriter.Mw;
import mapwriter.Render;
import mapwriter.util.MwUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

/**
 * @author Two
 */
public class MapDisplay {

  protected final AreaMap areaMap;
  protected final Rectangle position = new Rectangle(0, 0, 1, 1);
  protected int widthPercent, heightPercent, marginPercent;
  protected boolean circular = false;
  protected boolean rotating = false;
  protected int zoomLevel = 0;
  protected MapAnchor mapAnchor = MapAnchor.center;

  public MapDisplay(final AreaMap areaMap) {
    this.areaMap = areaMap;
    this.widthPercent = 30;
    this.heightPercent = 30;
    this.marginPercent = 1;
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

  public void setAnchor(final MapAnchor newMapAnchor) {
    if (newMapAnchor != mapAnchor) {
      this.mapAnchor = newMapAnchor;
    }
  }

  public MapAnchor getMapAnchor() {
    return this.mapAnchor;
  }

  public void updateRelativePosition(final int screenWidth, final int screenHeight) {
    switch (this.mapAnchor) {
      case topLeft:
        this.setCenter(0, 0, screenWidth, screenHeight); // MC uses top-left coordinate system for GUI drawing
        break;
      case topRight:
        this.setCenter(1, 0, screenWidth, screenHeight);
        break;
      case bottomLeft:
        this.setCenter(0, 1, screenWidth, screenHeight);
        break;
      case bottomRight:
        this.setCenter(1, 1, screenWidth, screenHeight);
        break;
      case center:
        this.setCenter(0.5, 0.5, screenWidth, screenHeight);
        break;
    }
  }

  protected void setCenter(final double x, final double y, final int screenWidth, final int screenHeight) {
    final int adjustSize = Math.min(screenWidth, screenHeight);
    final int newMargin = adjustSize * this.marginPercent / 100;
    final int newWidth = adjustSize * this.widthPercent / 100;
    final int newHeight = adjustSize * this.heightPercent / 100;
    
    final int newX = MwUtil.withinBounds((int) (screenWidth * x), newMargin, screenWidth - newWidth - newMargin);
    final int newY = MwUtil.withinBounds((int) (screenHeight * y), newMargin, screenHeight - newHeight - newMargin);

    if ((newWidth != position.getWidth()) || (newHeight != position.getHeight()) || (newX != position.getX()) || (newY != position.getY())) {
      position.setBounds(newX, newY, newWidth, newHeight);
      this.updateAreaMapSize();
    }
  }

  public void setPercentualDimension(final int width, final int height, final int margin) {
    this.widthPercent = width;
    this.heightPercent = height;
    this.marginPercent = margin;
  }

  public int getHeight() {
    return position.getHeight();
  }

  public int getWidth() {
    return position.getWidth();
  }

  public void setDimensionID(final int dimensionID) {
    this.areaMap.setDimensionID(dimensionID);
  }

  public int getDimensionID() {
    return this.areaMap.getDimensionID();
  }

  public void draw(final int screenWidth, final int screenHeight) {
    GL11.glPushMatrix();
    this.updateRelativePosition(screenWidth, screenHeight);
    this.translateToCenter();
    // draw background, the map texture, and enabled overlays
    this.drawMap();
    this.drawMarkers();
    this.drawCoords();

    GL11.glPopMatrix();
  }

  protected void translateToCenter() {
    GL11.glTranslated(this.position.getX() + this.position.getWidth() / 2, this.position.getY() + this.position.getHeight() / 2, 0.0);
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
    this.setStencil(true);
    areaMap.renderMap();

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
