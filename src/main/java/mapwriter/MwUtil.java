package mapwriter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.chunk.Chunk;

public class MwUtil {

  public final static Pattern patternInvalidChars = Pattern.compile("[^\\p{IsAlphabetic}\\p{Digit}_]");

  public static final ToIntFunction<Integer> convertIntegerToInt = new ToIntFunction<Integer>() {

    @Override
    public int applyAsInt(final Integer value) {
      return value;
    }
  };

  public static final Supplier<ArrayList<Integer>> arrayListOfIntegersSupplier = new Supplier<ArrayList<Integer>>() {

    @Override
    public ArrayList<Integer> get() {
      return new ArrayList<Integer>();
    }
  };

  public static final Function<Object, String> convertObjectToString = new Function<Object, String>() {

    @Override
    public String apply(final Object o) {
      return String.valueOf(o);
    }
  };

  public static String mungeString(String s) {
    s = s.replace('.', '_');
    s = s.replace('-', '_');
    s = s.replace(' ', '_');
    s = s.replace('/', '_');
    s = s.replace('\\', '_');
    return patternInvalidChars.matcher(s).replaceAll("");
  }

  public static File getFreeFilename(File dir, String baseName, String ext) {
    int i = 0;
    File outputFile;
    if (dir != null) {
      outputFile = new File(dir, baseName + "." + ext);
    } else {
      outputFile = new File(baseName + "." + ext);
    }
    while (outputFile.exists() && (i < 1000)) {
      if (dir != null) {
        outputFile = new File(dir, baseName + "." + i + "." + ext);
      } else {
        outputFile = new File(baseName + "." + i + "." + ext);
      }
      i++;
    }
    return (i < 1000) ? outputFile : null;
  }

  public static void printBoth(String msg) {
    EntityClientPlayerMP thePlayer = Minecraft.getMinecraft().thePlayer;
    if (thePlayer != null) {
      thePlayer.addChatMessage(new ChatComponentText(msg));
    }
    Mw.log.info(msg);
  }

  public static File getDimensionDir(File worldDir, int dimension) {
    File dimDir;
    if (dimension != 0) {
      dimDir = new File(worldDir, "DIM" + dimension);
    } else {
      dimDir = worldDir;
    }
    return dimDir;
  }

  public static IntBuffer newIntBuffer(int size) {
    return ByteBuffer.allocate(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
  }

  public static IntBuffer newDirectIntBuffer(int size) {
    return ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
  }

  public static String getCurrentDateString() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
    return dateFormat.format(new Date());
  }

  public static int distToChunkSq(int x, int z, Chunk chunk) {
    int dx = (chunk.xPosition << 4) + 8 - x;
    int dz = (chunk.zPosition << 4) + 8 - z;
    return (dx * dx) + (dz * dz);
  }
}
