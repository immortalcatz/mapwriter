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
  protected double centerX;
  protected double centerZ;
  protected int zoom;
  protected double coordLeft, coordRight, coordTop, coordBottom;
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

    GL11.glTranslated(-coordLeft, -coordTop, -2000.0); // z is -2000 so that it is drawn above the 3D world, but below GUI
    for (final Region region : regions) {
      region.draw();
    }

    GL11.glPopMatrix();
  }

  public int getDimensionID() {
    return dimensionID;
  }

  public void setDimensionID(final int dimensionID) {
    if (this.dimensionID != dimensionID) {
      this.dimensionID = dimensionID;
      this.requiresUpdate = true;
    }
  }

  public int getTop() {
    return top;
  }

  public void setTop(final int top) {
    if (this.top != top) {
      this.top = top;
      this.requiresUpdate = true;
    }
  }

  public int getLeft() {
    return left;
  }

  public void setLeft(final int left) {
    if (this.left != left) {
      this.left = left;
      this.requiresUpdate = true;
    }
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(final int width) {
    if (this.width != width) {
      this.width = width;
      this.requiresUpdate = true;
    }
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(final int height) {
    if (this.height != height) {
      this.height = height;
      this.requiresUpdate = true;
    }
  }

  public double getCenterX() {
    return centerX;
  }

  public void setCenterX(final double centerX) {
    if (this.centerX != centerX) {
      this.centerX = centerX;
      this.requiresUpdate = true;
    }
  }

  public double getCenterZ() {
    return centerZ;
  }

  public void setCenterZ(final double centerZ) {
    if (this.centerZ != centerZ) {
      this.centerZ = centerZ;
      this.requiresUpdate = true;
    }
  }

  public void setCenter(final double centerX, final double centerZ) {
    if ((this.centerX != centerX) || (this.centerZ != centerZ)) {
      this.centerX = centerX;
      this.centerZ = centerZ;
      this.requiresUpdate = true;
    }
  }

  public void moveCenter(final double moveX, final double moveZ) {
    this.setCenter(this.getCenterX() + moveX, this.getCenterZ() + moveZ);
  }

  public int getZoom() {
    return zoom;
  }

  public void setZoom(final int zoom) {
    if (this.centerX != zoom) {
      this.zoom = zoom;
      this.requiresUpdate = true;
    }
  }

  public void modifyZoom(final int mod) {
    this.setZoom(this.getZoom() + mod);
  }

  public boolean isBlockWithin(final double x, final double z) {
    return ((x >= this.coordLeft) && (x <= this.coordRight)
            && (z >= this.coordTop) && (z <= this.coordBottom));
  }

  public boolean isBlockWithin(final double x, final double z, final double radius) {
    final double distX = Math.abs(this.centerX - x);
    final double distZ = Math.abs(this.centerZ - z);
    return ((distX <= width / 2) && (distZ <= height / 2)
            && (Math.sqrt(distX * distX + distZ * distZ) <= radius));
  }

}
