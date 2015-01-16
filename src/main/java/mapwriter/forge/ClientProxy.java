package mapwriter.forge;

import java.io.File;

import mapwriter.Mw;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import java.net.InetSocketAddress;
import mapwriter.Config;
import mapwriter.mapgen.ColorConvert;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class ClientProxy extends CommonProxy {

  @Override
  public void preInit(final File configFile) {
    Config.instance.setConfigFile(new MwConfig(configFile));
    Config.instance.load();
  }

  @Override
  public void load() {
    MinecraftForge.EVENT_BUS.register(this);
    FMLCommonHandler.instance().bus().register(this);
    FMLCommonHandler.instance().bus().register(new MwKeyHandler());
    // temporary workaround for user defined key bindings not being loaded
    // at game start. see https://github.com/MinecraftForge/FML/issues/378
    // for more info.
    Minecraft.getMinecraft().gameSettings.loadOptions();
  }

  @SubscribeEvent
  public void eventChunkLoad(ChunkEvent.Load event) {
    if (event.world.isRemote) {
      Mw.instance.onChunkLoad(event.getChunk());
    }
  }

  @SubscribeEvent
  public void eventChunkUnload(ChunkEvent.Unload event) {
    if (event.world.isRemote) {
      Mw.instance.onChunkUnload(event.getChunk());
    }
  }

  @SubscribeEvent
  public void eventWorldLoad(WorldEvent.Load event) {
    MapWriter.log.info("Event: WorldEvent.Load (%s, name %s, dimension %d)",
            event.world,
            event.world.getWorldInfo().getWorldName(),
            event.world.provider.dimensionId);
    if (event.world.isRemote) {
      Mw.instance.onWorldLoad(event.world);
    }
  }

  @SubscribeEvent
  public void eventWorldSave(WorldEvent.Save event) {
    MapWriter.log.info("Event: WorldEvent.Save (%s, name %s, dimension %d)",
            event.world,
            event.world.getWorldInfo().getWorldName(),
            event.world.provider.dimensionId);
    if (event.world.isRemote) {
      Mw.instance.onWorldSave(event.world);
    }
  }

  @SubscribeEvent
  public void eventWorldUnload(WorldEvent.Unload event) {
    MapWriter.log.info("Event: WorldEvent.Unload");
    if (event.world.isRemote) {
      Mw.instance.onWorldUnload(event.world);
    }
  }

  @SubscribeEvent
  public void onClientChat(ClientChatReceivedEvent event) {

  }

  @SubscribeEvent
  public void onTextureStichPost(final TextureStitchEvent.Post event) {
    MapWriter.log.info("Event: TextureStitchEvent.Post");
    ColorConvert.reset();
  }

  @SubscribeEvent
  public void renderGameOverlay(final RenderGameOverlayEvent.Post event) {
    if (event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
      Mw.instance.renderMiniMap(event.resolution);
    }
  }

  @SubscribeEvent
  public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    MapWriter.log.info("Event: ClientConnectedToServerEvent local=%s", Boolean.toString(event.isLocal));
    if (!event.isLocal) {
      InetSocketAddress address = (InetSocketAddress) event.manager.getSocketAddress();
      Mw.instance.setServerDetails(address.getHostName(), address.getPort());
    }
  }

  @SubscribeEvent
  public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    MapWriter.log.info("Event: PlayerLoggedInEvent");
  }

  @SubscribeEvent
  public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    MapWriter.log.info("Event: PlayerLoggedOutEvent");
    Mw.instance.close();
  }

  @SubscribeEvent
  public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    MapWriter.log.info("Event: PlayerChangedDimensionEvent");
  }

  //Called when a new frame is displayed (See fps) 
  @SubscribeEvent
  public void onRenderTick(TickEvent.RenderTickEvent event) {
//    MapWriter.log.info("Event: RenderTickEvent (%s %s)", event.side, event.phase.toString());
    if (event.phase == TickEvent.Phase.END) {
      Mw.instance.onPostRenderTick();
    }
  }

  //Called when the world ticks
  @SubscribeEvent
  public void onWorldTick(TickEvent.WorldTickEvent event) {
//    MapWriter.log.info("Event: WorldTickEvent (%s %s for %s)", event.side, event.phase.toString(), event.world.getWorldInfo().getWorldName());
  }
}
