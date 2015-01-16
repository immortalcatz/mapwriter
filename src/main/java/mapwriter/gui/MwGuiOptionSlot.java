package mapwriter.gui;

import mapwriter.Config;
import mapwriter.Mw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class MwGuiOptionSlot extends GuiSlot {

  //private GuiScreen parentScreen;
  private final Minecraft mc;

  protected enum MinimapPosition {

    unchanged, top_right, top_left, bottom_right, bottom_left;

    public MinimapPosition next() {
      final int resultIndex = this.ordinal() + 1;
      final MinimapPosition[] values = MinimapPosition.values();
      if (resultIndex >= values.length) {
        return values[1];
      } else {
        return values[resultIndex];
      }
    }

    @Override
    public String toString() {
      return (this.name().replaceAll("_", " "));
    }
  }

  protected MinimapPosition miniMapPositionIndex = MinimapPosition.unchanged;
  private static final String[] coordsModeStringArray = {
    "disabled",
    "small",
    "large"
  };
  private static final String[] backgroundModeStringArray = {
    "none",
    "static",
    "panning"
  };

  private final GuiButton[] buttons = new GuiButton[12];

  static final ResourceLocation WIDGET_TEXTURE_LOC = new ResourceLocation("textures/gui/widgets.png");

  public void updateButtonLabel(int i) {
    switch (i) {
      case 0:
        this.buttons[i].displayString = "Draw coords: " + coordsModeStringArray[Config.instance.coordsMode];
        break;
      case 1:
        this.buttons[i].displayString = "Circular mode: " + Mw.instance.miniMap.isCircular();
        break;
      case 2:
        this.buttons[i].displayString = "Texture size: " + Config.instance.configTextureSize;
        break;
      case 3:
        this.buttons[i].displayString = "Texture scaling: " + (Config.instance.linearTextureScalingEnabled ? "linear" : "nearest");
        break;
      case 4:
        this.buttons[i].displayString = "Trail markers: " + false;
        break;
      case 5:
        this.buttons[i].displayString = "Map colours: " + (Config.instance.useSavedBlockColours ? "frozen" : "auto");
        break;
      case 6:
        this.buttons[i].displayString = "Max draw distance: " + Math.round(Math.sqrt(Config.instance.maxChunkSaveDistSq));
        break;
      case 7:
        this.buttons[i].displayString = "Mini map size: " + Mw.instance.miniMap.getSize();
        break;
      case 8:
        this.buttons[i].displayString = "Mini map position: " + this.miniMapPositionIndex.toString();
        break;
      case 9:
        this.buttons[i].displayString = "Map pixel snapping: " + (Config.instance.mapPixelSnapEnabled ? "enabled" : "disabled");
        break;
      case 10:
        this.buttons[i].displayString = "Max death markers: " + Config.instance.maxDeathMarkers;
        break;
      case 11:
        this.buttons[i].displayString = "Background mode: " + backgroundModeStringArray[Config.instance.backgroundTextureMode];
        break;
      //case 11:
      //	this.buttons[i].displayString = "Map Lighting: " + (Mw.instance.lightingEnabled ? "enabled" : "disabled");
      //	break;	
      default:
        break;
    }
  }

  public MwGuiOptionSlot(GuiScreen parentScreen, Minecraft mc) {
    // GuiSlot(minecraft, width, height, top, bottom, slotHeight)
    super(mc, parentScreen.width, parentScreen.height, 16, parentScreen.height - 32, 25);
    //this.parentScreen = parentScreen;
    this.mc = mc;
    for (int i = 0; i < this.buttons.length; i++) {
      this.buttons[i] = new GuiButton(300 + i, 0, 0, "");
      this.updateButtonLabel(i);
    }
  }

  protected boolean keyTyped(char c, int k) {
    return false;
  }

  @Override
  protected int getSize() {
    // number of slots
    return this.buttons.length;
  }

  @Override
  protected void elementClicked(int i, boolean doubleClicked, int x, int y) {
    switch (i) {
      case 0:
        // toggle coords
        Mw.instance.toggleCoords();
        break;
      case 1:
        // toggle circular
        Mw.instance.miniMap.toggleRotating();
        break;
      case 2:
        // toggle texture size
        Config.instance.configTextureSize *= 2;
        if (Config.instance.configTextureSize > 4096) {
          Config.instance.configTextureSize = 1024;
        }
        break;
      case 3:
        // linear scaling
        Config.instance.linearTextureScalingEnabled = !Config.instance.linearTextureScalingEnabled;
        //Mw.instance.undergroundMapTexture.setLinearScaling(Mw.instance.linearTextureScalingEnabled);
        break;
//      case 4:
//        // player trail
//        Mw.instance.playerTrail.enabled = !Mw.instance.playerTrail.enabled;
//        break;
      case 5:
        // map colours
        Config.instance.useSavedBlockColours = !Config.instance.useSavedBlockColours;
        break;
      case 6:
        // toggle max chunk save dist
        int d = Math.round((float) Math.sqrt(Config.instance.maxChunkSaveDistSq));
        d += 32;
        if (d > 256) {
          d = 64;
        }
        Config.instance.maxChunkSaveDistSq = d * d;
        break;
      case 7:
//        Mw.instance.miniMap.mapMode.toggleHeightPercent();
        break;
      case 8:
        this.miniMapPositionIndex = this.miniMapPositionIndex.next();
        switch (this.miniMapPositionIndex) {
          case top_right:
            Mw.instance.miniMap.setCenter(mc.displayWidth, mc.displayHeight, Config.instance.mapMargin);
            break;
          case top_left:
            Mw.instance.miniMap.setCenter(0, mc.displayHeight, Config.instance.mapMargin);
            break;
          case bottom_right:
            Mw.instance.miniMap.setCenter(mc.displayWidth, 0, Config.instance.mapMargin);
            break;
          case bottom_left:
            Mw.instance.miniMap.setCenter(0, 0, Config.instance.mapMargin);
            break;
          default:
            break;
        }
      case 9:
        // map scroll pixel snapping
        Config.instance.mapPixelSnapEnabled = !Config.instance.mapPixelSnapEnabled;
        break;
      case 10:
        // max death markers
        Config.instance.maxDeathMarkers++;
        if (Config.instance.maxDeathMarkers > 10) {
          Config.instance.maxDeathMarkers = 0;
        }
        break;
      case 11:
        // background texture mode
        Config.instance.backgroundTextureMode = (Config.instance.backgroundTextureMode + 1) % 3;
        break;
      //case 11:
      //	// lighting
      //	Mw.instance.lightingEnabled = !Mw.instance.lightingEnabled;
      //	break;
      default:
        break;
    }
    this.updateButtonLabel(i);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float f) {
    this.mouseX = mouseX;
    this.mouseY = mouseY;
    super.drawScreen(mouseX, mouseY, f);
  }

  @Override
  protected boolean isSelected(int i) {
    return false;
  }

  @Override
  protected void drawBackground() {
  }

  @Override
  protected void drawSlot(int i, int x, int y, int i4, Tessellator tessellator, int i5, int i6) {
    GuiButton button = buttons[i];
    button.xPosition = x;
    button.yPosition = y;
    button.drawButton(this.mc, this.mouseX, this.mouseY);
  }
}
