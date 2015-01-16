/*
 */
package mapwriter.gui;

/**
 * @author Two
 */
public class MiniMap extends MapDisplay {

  public MiniMap() {
    super(new AreaMap());
    this.mapAnchor = MapAnchor.topRight;
  }

  @Override
  protected void translateToCenter() {
    this.centerMapOnPlayer();
    super.translateToCenter();
  }

  public void setSize(final int size, final int margin) {
    this.setPercentualDimension(size, size, margin);
  }

  public int getSize() {
    return this.position.getWidth(); // witdth and height are the same
  }

  public void toggleRotating() {
    this.setRotating(!this.isRotating());
  }

}
