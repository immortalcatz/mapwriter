package mapwriter.forge;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;

@Mod(modid = "MapWriter", name = "MapWriter", version = "1710.3.0")
public class MapWriter {

  @Mod.Instance("MapWriter")
  public static MapWriter instance;

  @SidedProxy(clientSide = "mapwriter.forge.ClientProxy", serverSide = "mapwriter.forge.CommonProxy")
  public static CommonProxy proxy;

  public static final Logger log = LogManager.getLogger(MapWriter.class.getSimpleName(), new StringFormatterMessageFactory());

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    proxy.preInit(event.getSuggestedConfigurationFile());
  }

  @Mod.EventHandler
  public void load(FMLInitializationEvent event) {
    proxy.load();
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    proxy.postInit();
  }  
}
