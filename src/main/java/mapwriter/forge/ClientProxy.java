package mapwriter.forge;

import java.io.File;

import mapwriter.Mw;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import mapwriter.Config;

public class ClientProxy extends CommonProxy {

  @Override
  public void preInit(final File configFile) {
    Config.instance.setConfigFile(new MwConfig(configFile));
    Config.instance.load();
  }

  @Override
  public void load() {
    MinecraftForge.EVENT_BUS.register(new EventHandler(Mw.instance));
    FMLCommonHandler.instance().bus().register(new MwKeyHandler());
    // temporary workaround for user defined key bindings not being loaded
    // at game start. see https://github.com/MinecraftForge/FML/issues/378
    // for more info.
    Minecraft.getMinecraft().gameSettings.loadOptions();
  }

}
