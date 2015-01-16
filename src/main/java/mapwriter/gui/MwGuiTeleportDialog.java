package mapwriter.gui;

import mapwriter.Mw;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mapwriter.Config;

@SideOnly(Side.CLIENT)
public class MwGuiTeleportDialog extends MwGuiTextDialog {

  final AreaMap areaMap;
  final int teleportX, teleportZ;

  public MwGuiTeleportDialog(GuiScreen parentScreen, AreaMap areaMap, int x, int y, int z) {
    super(parentScreen, "Teleport Height:", "" + y, "invalid height");
    this.areaMap = areaMap;
    this.teleportX = x;
    this.teleportZ = z;
    this.backToGameOnSubmit = true;
  }

  @Override
  public boolean submit() {
    boolean done = false;
    int height = this.getInputAsInt();
    if (this.inputValid) {
      height = Math.min(Math.max(0, height), 255);
      Config.instance.defaultTeleportHeight = height;
      Mw.instance.teleportToMapPos(this.areaMap, this.teleportX, height, this.teleportZ);
      done = true;
    }
    return done;
  }
}
