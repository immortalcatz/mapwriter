package mapwriter.gui;

import mapwriter.Mw;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MwGuiDimensionDialog extends MwGuiTextDialog {

  final MapView mapView;
  final int dimension;

  public MwGuiDimensionDialog(GuiScreen parentScreen, MapView mapView, int dimension) {
    super(parentScreen, "Set dimension to:", "" + dimension, "invalid dimension");
    this.mapView = mapView;
    this.dimension = dimension;
  }

  @Override
  public boolean submit() {
    boolean done = false;
    int dimension = this.getInputAsInt();
    if (this.inputValid) {
      this.mapView.setDimensionID(dimension);
      Mw.instance.miniMap.view.setDimensionID(dimension);
      Mw.instance.addDimension(dimension);
      done = true;
    }
    return done;
  }
}
