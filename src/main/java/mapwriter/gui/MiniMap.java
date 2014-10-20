/*
 */
package mapwriter.gui;

/**
 * @author Two
 */
public class MiniMap extends MapDisplay {

  public MiniMap() {
    super(new MapView());
  }

  public int getSize() {
    throw new UnsupportedOperationException("Not supported yet."); // TODO: implement
  }

  public void toggleRotating() {
    this.setRotating(!this.isRotating());
  }

}
