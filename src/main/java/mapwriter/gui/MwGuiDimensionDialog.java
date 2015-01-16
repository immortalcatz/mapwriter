package mapwriter.gui;

import mapwriter.Mw;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MwGuiDimensionDialog extends MwGuiTextDialog {

  final AreaMap areaMap;
  final int dimension;

  public MwGuiDimensionDialog(GuiScreen parentScreen, AreaMap areaMap, int dimension) {
    super(parentScreen, "Set dimension to:", "" + dimension, "invalid dimension");
    this.areaMap = areaMap;
    this.dimension = dimension;
  }

  @Override
  public boolean submit() {
    int newDimension = this.getInputAsInt();
    if (this.inputValid) {
      this.areaMap.setDimensionID(newDimension);
//      Mw.instance.miniMap.view.setDimensionID(newDimension);
      Mw.instance.addDimension(newDimension);
      return true;
    }
    return false;
  }
}
