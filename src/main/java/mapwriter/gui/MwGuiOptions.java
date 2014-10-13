package mapwriter.gui;

import mapwriter.Mw;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class MwGuiOptions extends GuiScreen {

  private final GuiScreen parentScreen;
  private MwGuiOptionSlot optionSlot = null;

  public MwGuiOptions(GuiScreen parentScreen) {
    this.parentScreen = parentScreen;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initGui() {
    this.optionSlot = new MwGuiOptionSlot(this, this.mc);
    this.optionSlot.registerScrollButtons(7, 8);

    final GuiButton button = new GuiButton(200, (this.width / 2) - 50, this.height - 28, 100, 20, "Done");
    this.buttonList.add(button);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id == 200) {
      // done
      // reconfigure texture size
      Mw.instance.setTextureSize();
      this.mc.displayGuiScreen(this.parentScreen);
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float f) {
    this.drawDefaultBackground();
    this.optionSlot.drawScreen(mouseX, mouseY, f);
    this.drawCenteredString(this.fontRendererObj, "MapWriter Options", this.width / 2, 10, 0xffffff);
    super.drawScreen(mouseX, mouseY, f);
  }

  @Override
  protected void mouseClicked(int x, int y, int button) {
    super.mouseClicked(x, y, button);
  }

  @Override
  protected void keyTyped(char c, int k) {
    if (this.optionSlot.keyTyped(c, k)) {
      super.keyTyped(c, k);
    }
  }
}
