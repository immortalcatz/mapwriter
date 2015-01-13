/*
 */
package mapwriter.gui;

import java.util.List;
import mapwriter.Mw;
import mapwriter.map.Region;
import mapwriter.map.RegionManager;
import org.lwjgl.opengl.GL11;

/**
 * @author Two
 */
public class MapView {

  protected int dimensionID;
  protected int width;
  protected int height;
  protected double centerX;
  protected double centerZ;
  protected double worldCoordLeft, worldCoordRight, worldCoordTop, worldCoordBottom;
  protected List<Region> regions = null;
  protected boolean requiresUpdate = true;

  public MapView() {
  }

  protected void updateView() {
    if (this.requiresUpdate) {
      this.worldCoordLeft = centerX - width / 2;
      this.worldCoordRight = worldCoordLeft + width;
      this.worldCoordTop = centerZ - height / 2;
      this.worldCoordBottom = worldCoordLeft + height;

      final RegionManager regionManager = Mw.instance.getRegionManager(dimensionID);
      regions = regionManager.getAllExistingRegionsInArea(worldCoordLeft, worldCoordTop, worldCoordRight, worldCoordBottom);
      this.requiresUpdate = false;
    }
  }

  public void renderMap() {
    this.updateView();
    GL11.glPushMatrix();

    GL11.glTranslated(-worldCoordLeft, -worldCoordTop, 0.0);
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

  public boolean isBlockWithin(final double x, final double z) {
    return ((x >= this.worldCoordLeft) && (x <= this.worldCoordRight)
            && (z >= this.worldCoordTop) && (z <= this.worldCoordBottom));
  }

  public boolean isBlockWithin(final double x, final double z, final double radius) {
    final double distX = Math.abs(this.centerX - x);
    final double distZ = Math.abs(this.centerZ - z);
    return ((distX <= width / 2) && (distZ <= height / 2)
            && (Math.sqrt(distX * distX + distZ * distZ) <= radius));
  }

}
