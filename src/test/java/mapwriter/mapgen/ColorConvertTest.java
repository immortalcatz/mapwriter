/*
 */
package mapwriter.mapgen;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Two
 */
public class ColorConvertTest {

  public ColorConvertTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testCalculateAverageColor() {
    System.out.println("calculateAverageColor");
    int[] pixels = null;
    int result;

    try {
      ColorConvert.calculateAverageColor(pixels);
      fail("Calculated average color of null");
    } catch (NullPointerException e) {
      // OK
    }
    pixels = new int[]{0};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0);
    assertEquals(result, ColorConvert.BLACK);

    pixels = new int[]{0xFF000000};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0xFF000000);
    assertEquals(result, ColorConvert.BLACK);

    pixels = new int[]{0xFFFFFFFF};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0xFFFFFFFF);
    assertEquals(result, 0xFFFFFFFF);

    pixels = new int[]{0xFFFFFFFF, 0};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0xFFFFFFFF);
    assertEquals(pixels[1], 0);
    assertEquals(result, 0xFFFFFFFF);

    pixels = new int[]{0xFFFFFFFF, 0xFFFFFFFF};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0xFFFFFFFF);
    assertEquals(pixels[1], 0xFFFFFFFF);
    assertEquals(result, 0xFFFFFFFF);

    pixels = new int[]{0xFFFFFFFF, 0xFF000000};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0xFFFFFFFF);
    assertEquals(pixels[1], 0xFF000000);
    assertEquals(result, 0xFF7F7F7F);

    pixels = new int[]{0xFF000000, 0xFFFFFFFF};
    result = ColorConvert.calculateAverageColor(pixels);
    assertEquals(pixels[0], 0xFF000000);
    assertEquals(pixels[1], 0xFFFFFFFF);
    assertEquals(result, 0xFF7F7F7F);
  }

}
