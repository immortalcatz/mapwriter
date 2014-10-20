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
    int newDimension = this.getInputAsInt();
    if (this.inputValid) {
      this.mapView.setDimensionID(newDimension);
//      Mw.instance.miniMap.view.setDimensionID(newDimension);
      Mw.instance.addDimension(newDimension);
      return true;
    }
    return false;
  }
}
