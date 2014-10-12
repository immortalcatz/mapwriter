package mapwriter.forge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mapwriter.Mw;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {

  Mw mw;

  public EventHandler(Mw mw) {
    this.mw = mw;
  }

  @SubscribeEvent
  public void eventChunkLoad(ChunkEvent.Load event) {
    if (event.world.isRemote) {
      this.mw.onChunkLoad(event.getChunk());
    }
  }

  @SubscribeEvent
  public void eventChunkUnload(ChunkEvent.Unload event) {
    if (event.world.isRemote) {
      this.mw.onChunkUnload(event.getChunk());
    }
  }

  @SubscribeEvent
  public void eventWorldLoad(WorldEvent.Load event) {
    if (event.world.isRemote) {
      this.mw.onWorldLoad(event.world);
    }
  }

  @SubscribeEvent
  public void eventWorldSave(WorldEvent.Save event) {
    if (event.world.isRemote) {
      this.mw.onWorldSave(event.world);
    }
  }

  @SubscribeEvent
  public void eventWorldUnload(WorldEvent.Unload event) {
    if (event.world.isRemote) {
      this.mw.onWorldUnload(event.world);
    }
  }

  @SubscribeEvent
  public void onClientChat(ClientChatReceivedEvent event) {

  }
}
