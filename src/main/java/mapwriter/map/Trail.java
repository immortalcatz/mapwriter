package mapwriter.map;

import java.awt.Point;
import java.util.LinkedList;
import mapwriter.Config;

import mapwriter.Mw;
import mapwriter.Render;
import mapwriter.forge.MwConfig;
import mapwriter.gui.MapView;
import mapwriter.map.mapmode.MapMode;

public class Trail {

  class TrailMarker {

    double x, y, z, heading;
    int alphaPercent;

    static final int borderColour = 0xff000000;
    static final int colour = 0xff00ffff;

    public TrailMarker(double x, double y, double z, double heading) {
      this.set(x, y, z, heading);
    }

    public void set(double x, double y, double z, double heading) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.heading = heading;
      this.alphaPercent = 100;
    }

    public void draw(MapMode mapMode, MapView mapView) {
      final boolean isWithin = mapMode.circular
              ? mapView.isBlockWithin(this.x, this.z, mapMode.w / 2)
              : mapView.isBlockWithin(this.x, this.z);
      if (isWithin) {
        Point.Double p = mapMode.blockXZtoScreenXY(mapView, this.x, this.z);

        // draw a coloured arrow centered on the calculated (x, y)
        Render.setColourWithAlphaPercent(borderColour, this.alphaPercent);
        Render.drawArrow(p.x, p.y, this.heading, mapMode.trailMarkerSize);
        Render.setColourWithAlphaPercent(colour, this.alphaPercent);
        Render.drawArrow(p.x, p.y, this.heading, mapMode.trailMarkerSize - 1.0);
      }
    }
  }

  public final LinkedList<TrailMarker> trailMarkerList = new LinkedList<TrailMarker>();
  public int maxLength = 30;
  public String name;
  public boolean enabled;
  public long lastMarkerTime = 0;
  public long intervalMillis = 5000;

  public Trail(String name) {
    this.name = name;
    final MwConfig configFile = Config.instance.getConfigFile();
    this.enabled = configFile.getOrSetBoolean(Mw.catOptions, this.name + "TrailEnabled", false);
    this.maxLength = configFile.getOrSetInt(Mw.catOptions, this.name + "TrailMaxLength", this.maxLength, 1, 200);
    this.intervalMillis = (long) configFile.getOrSetInt(Mw.catOptions, this.name + "TrailMarkerIntervalMillis", (int) this.intervalMillis, 100, 360000);
  }

  public void close() {
    final MwConfig configFile = Config.instance.getConfigFile();
    configFile.setBoolean(Mw.catOptions, this.name + "TrailEnabled", this.enabled);
    configFile.setInt(Mw.catOptions, this.name + "TrailMaxLength", this.maxLength);
    configFile.setInt(Mw.catOptions, this.name + "TrailMarkerIntervalMillis", (int) this.intervalMillis);
    this.trailMarkerList.clear();
  }

  // for other types of trails will need to extend Trail and override this method
  public void onTick() {
    long time = System.currentTimeMillis();
    if ((time - this.lastMarkerTime) > this.intervalMillis) {
      this.lastMarkerTime = time;
      this.addMarker(Mw.instance.playerX, Mw.instance.playerY, Mw.instance.playerZ, Mw.instance.playerHeading);
    }
  }

  public void addMarker(double x, double y, double z, double heading) {
    this.trailMarkerList.add(new TrailMarker(x, y, z, heading));
    // remove elements from beginning of list until the list has at most
    // maxTrailLength elements
    while (this.trailMarkerList.size() > this.maxLength) {
      this.trailMarkerList.poll();
    }
    int i = this.maxLength - this.trailMarkerList.size();
    for (TrailMarker marker : this.trailMarkerList) {
      marker.alphaPercent = (i * 100) / this.maxLength;
      i++;
    }
  }

  public void draw(MapMode mapMode, MapView mapView) {
    for (TrailMarker marker : this.trailMarkerList) {
      marker.draw(mapMode, mapView);
    }
  }

}
