package mapwriter.map;

import java.awt.Point;

import mapwriter.Render;
import org.lwjgl.opengl.GL11;

public class Marker {

  public final String name;
  public final String groupName;
  public int x;
  public int y;
  public int z;
  public int dimension;
  public int colour;
  public int borderColor;

  public Point.Double screenPos = new Point.Double(0, 0);

  private static final int[] colours = new int[]{
    0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff,
    0xff8000, 0x8000ff};
  // static so that current index is shared between all markers
  private static int colourIndex = 0;

  public Marker(String name, String groupName, int x, int y, int z, int dimension, int colour) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.z = z;
    this.dimension = dimension;
    this.colour = colour;
    this.groupName = groupName;
  }

  public String getString() {
    return String.format("%s %s (%d, %d, %d) %d %06x",
            this.name, this.groupName, this.x, this.y, this.z, this.dimension, this.colour & 0xffffff);
  }

  public static int getCurrentColour() {
    return 0xff000000 | colours[colourIndex];
  }

  public void colourNext() {
    colourIndex = (colourIndex + 1) % colours.length;
    this.colour = getCurrentColour();
  }

  public void colourPrev() {
    colourIndex = (colourIndex + colours.length - 1) % colours.length;
    this.colour = getCurrentColour();
  }

  public void draw() {
    // draw a coloured rectangle centered on the calculated (x, y)
    final double mSize = 16.0;
    final double halfMSize = mSize / 2.0;
    GL11.glPushMatrix();
    GL11.glTranslated(x, z, 0.0);
    Render.setColor(this.borderColor);
    Render.drawRect(-halfMSize, -halfMSize, mSize, mSize);
    Render.setColor(this.colour);
    Render.drawRect(-halfMSize + 0.5, -halfMSize + 0.5, mSize - 1.0, mSize - 1.0);
    GL11.glPopMatrix();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Marker) {
      final Marker other = (Marker) obj;
      return (name.equals(other.name) && groupName.equals(other.groupName) && (x == other.x) && (y == other.y) && (z == other.z) && (dimension == other.dimension));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 41 * hash + (this.groupName != null ? this.groupName.hashCode() : 0);
    hash = 41 * hash + this.x;
    hash = 41 * hash + this.y;
    hash = 41 * hash + this.z;
    hash = 41 * hash + this.dimension;
    return hash;
  }
}
