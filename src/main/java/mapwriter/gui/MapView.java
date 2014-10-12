/*
 */
package mapwriter.gui;

import java.util.List;
import mapwriter.Mw;
import mapwriter.mapgen.Region;
import mapwriter.mapgen.RegionManager;
import org.lwjgl.opengl.GL11;

/**
 * @author Two
 */
public class MapView {

  protected int dimensionID;
  protected int top;
  protected int left;
  protected int width;
  protected int height;
  protected float centerX;
  protected float centerZ;
  protected int zoom;
  protected float coordLeft, coordRight, coordTop, coordBottom;
  protected List<Region> regions = null;
  protected boolean requiresUpdate = true;

  public MapView() {
  }

  protected void updateView() {
    if (this.requiresUpdate) {
      this.coordLeft = centerX - width / 2;
      this.coordRight = coordLeft + width;
      this.coordTop = centerZ - height / 2;
      this.coordBottom = coordLeft + height;
      
      final RegionManager regionManager = Mw.instance.getRegionManager(dimensionID);
      regions = regionManager.getAllExistingRegionsInArea(coordLeft, coordTop, coordRight, coordBottom);
      this.requiresUpdate = false;
    }
  }

  public void renderMap() {
    this.updateView();
    GL11.glPushMatrix();

    GL11.glTranslatef(-coordLeft, -coordTop, -2000.0f); // z is -2000 so that it is drawn above the 3D world, but below GUI
    for (final Region region : regions) {
      region.draw();
    }

    GL11.glPopMatrix();
  }

  public int getDimensionID() {
    return dimensionID;
  }

  public void setDimensionID(final int dimensionID) {
    this.dimensionID = dimensionID;
    this.requiresUpdate = true;
  }

  public int getTop() {
    return top;
  }

  public void setTop(final int top) {
    this.top = top;
    this.requiresUpdate = true;
  }

  public int getLeft() {
    return left;
  }

  public void setLeft(final int left) {
    this.left = left;
    this.requiresUpdate = true;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(final int width) {
    this.width = width;
    this.requiresUpdate = true;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(final int height) {
    this.height = height;
    this.requiresUpdate = true;
  }

  public float getCenterX() {
    return centerX;
  }

  public void setCenterX(final float centerX) {
    this.centerX = centerX;
    this.requiresUpdate = true;
  }

  public float getCenterZ() {
    return centerZ;
  }

  public void setCenterZ(final float centerZ) {
    this.centerZ = centerZ;
    this.requiresUpdate = true;
  }

  public int getZoom() {
    return zoom;
  }

  public void setZoom(final int zoom) {
    this.zoom = zoom;
    this.requiresUpdate = true;
  }

}
