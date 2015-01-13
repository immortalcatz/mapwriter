/*
 */
package mapwriter;

import net.minecraft.client.Minecraft;

/**
 * @author Two
 */
public class PlayerStatus {

  public double x = 0.0;
  public double y = 0.0;
  public double z = 0.0;
  public int xInt = 0;
  public int yInt = 0;
  public int zInt = 0;
  public double heading = 0.0;
  public int dimensionID = 0;

  public void update() {
    final Minecraft mc = Minecraft.getMinecraft();
    this.setPosition(mc.thePlayer.posX, mc.thePlayer.posX, mc.thePlayer.posX);

    // rotationYaw of 0 points to north, we want it to point to east instead
    // so add pi/2 radians (90 degrees)
    this.heading = Math.toRadians(mc.thePlayer.rotationYaw) + (Math.PI / 2.0D);
  }

  public void setPosition(final double x, final double y, final double z) {
    this.setX(x);
    this.setY(y);
    this.setZ(z);
  }

  public void setX(final double x) {
    this.x = x;
    this.xInt = (int) Math.floor(x);
  }

  public void setY(final double y) {
    this.y = y;
    this.yInt = (int) Math.floor(y);
  }

  public void setZ(final double z) {
    this.z = z;
    this.zInt = (int) Math.floor(z);
  }

}
