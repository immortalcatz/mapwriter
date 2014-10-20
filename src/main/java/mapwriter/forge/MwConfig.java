package mapwriter.forge;

import cpw.mods.fml.common.FMLLog;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import mapwriter.Mw;

import mapwriter.MwUtil;
import net.minecraftforge.common.config.Configuration;

public class MwConfig extends Configuration {

  public MwConfig(File file) {
    super(file, true);
  }

  public boolean getOrSetBoolean(String category, String key, boolean defaultValue) {
    return this.get(category, key, defaultValue ? 1 : 0).getInt() != 0;
  }

  public void setBoolean(String category, String key, boolean value) {
    this.get(category, key, value).set(value ? 1 : 0);
  }

  public int getOrSetInt(String category, String key, int defaultValue, int minValue, int maxValue) {
    int value = this.get(category, key, defaultValue).getInt();
    return Math.min(Math.max(minValue, value), maxValue);
  }

  public void setInt(String category, String key, int value) {
    this.get(category, key, value).set(value);
  }

  public long getColour(String category, String key) {
    long value = -1;
    if (this.hasKey(category, key)) {
      try {
        String valueString = this.get(category, key, "").getString();
        if (valueString.length() > 0) {
          value = Long.parseLong(valueString, 16);
          value &= 0xffffffffL;
        }
      } catch (NumberFormatException e) {
        Mw.log.warn("Could not read colour from config file " + category + "->" + key);
        value = -1;
      }
    }
    return value;
  }

  public int getColour(String category, String key, int value) {
    long valueLong = this.getColour(category, key);
    if (valueLong >= 0L) {
      value = (int) (valueLong & 0xffffffffL);
    }
    return value;
  }

  public int getOrSetColour(String category, String key, int value) {
    long valueLong = this.getColour(category, key);
    if (valueLong >= 0L) {
      value = (int) (valueLong & 0xffffffffL);
    } else {
      this.setColour(category, key, value);
    }
    return value;
  }

  public void setColour(String category, String key, int n) {
    this.get(category, key, "00000000").set(String.format("%08x", n));
  }

  public void setColour(String category, String key, int n, String comment) {
    this.get(category, key, "00000000", comment).set(String.format("%08x", n));
  }

  public String getSingleWord(String category, String key) {
    String value = "";
    if (this.hasKey(category, key)) {
      value = this.get(category, key, value).getString().trim();
      int firstSpace = value.indexOf(' ');
      if (firstSpace >= 0) {
        value = value.substring(0, firstSpace);
      }
    }
    return value;
  }

  public void setSingleWord(String category, String key, String value, String comment) {
    if ((comment != null) && (comment.length() > 0)) {
      value = value + " # " + comment;
    }
    this.get(category, key, value).set(value);
  }

  public void getIntList(final String category, final String key, final List<Integer> list) {
    try {
      final int[] arrayFromConfig = super.get(category, key, list.stream().mapToInt(MwUtil.convertIntegerToInt).toArray()).getIntList();
      list.clear();
      list.addAll(Arrays.stream(arrayFromConfig).boxed().collect(Collectors.toCollection(MwUtil.arrayListOfIntegersSupplier)));
    } catch (Exception e) {
      FMLLog.warning("Unable to load config value '%s': %s", key, e.toString());
    }
  }

  public void setIntList(final String category, final String key, final List<Integer> list) {
    try {
      final String[] array = (String[]) list.stream().map(MwUtil.convertObjectToString).toArray();
      this.get(category, key, array).set(array);
    } catch (Exception e) {
      FMLLog.warning("Unable to write config value '%s': %s", key, e.toString());
    }
  }
}
