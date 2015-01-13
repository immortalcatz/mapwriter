/*
 */
package mapwriter.util;

import java.awt.image.BufferedImage;
import java.util.Random;
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
public class PixelDataTest {

  public PixelDataTest() {
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
  public void testGetPixelsFromImage() {
    System.out.println("getPixelsFromImage");
    BufferedImage image = null;
    int[] expResult, result;
    try {
      PixelData.getPixelsFromImage(image);
      fail("Able to get pixels from null image");
    } catch (NullPointerException e) {
    }
    expResult = new int[]{0};
    image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, image.getWidth(), image.getHeight(), expResult, 0, image.getWidth());
    result = PixelData.getPixelsFromImage(image);
    assertArrayEquals(expResult, result);

    final Random random = new Random();
    expResult = new int[20 * 20];
    image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
    for (int i = 0; i < expResult.length; ++i) {
      expResult[i] = random.nextInt();
    }
    image.setRGB(0, 0, image.getWidth(), image.getHeight(), expResult, 0, image.getWidth());
    result = PixelData.getPixelsFromImage(image);
    assertArrayEquals(expResult, result);
  }

  @Test
  public void testGetRGB_0args() {
    System.out.println("getRGB");
    PixelData instance;
    int[] expResult, result;

    instance = new PixelData(1, 1);
    result = instance.getRGB();
    expResult = new int[]{0};
    assertArrayEquals(result, expResult);

    instance = new PixelData(10, 10);
    result = instance.getRGB();
    expResult = new int[10 * 10];
    assertArrayEquals(result, expResult);

    instance = new PixelData(2, 2);
    expResult = new int[]{2, 3, 4, 5};
    instance.setRGB(expResult);
    result = instance.getRGB();
    assertArrayEquals(result, expResult);
  }

  @Test
  public void testGetRGB_6args() {
    System.out.println("getRGB 6args");
    PixelData instance;
    int[] expResult, result;

    instance = new PixelData(1, 1);
    result = null;
    try {
      instance.getRGB(0, 0, 1, 1, result, 0);
      fail("Able to request null as target.");
    } catch (NullPointerException e) {
    }

    result = new int[]{0};
    instance.getRGB(0, 0, 1, 1, result, 0);
    expResult = new int[]{0};
    assertArrayEquals(result, expResult);

    instance = new PixelData(1, 1);
    try {
      instance.getRGB(-1, 0, 1, 1, result, 0);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.getRGB(0, -1, 1, 1, result, 0);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.getRGB(0, 0, 1, 1, result, -1);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.getRGB(0, 0, 2, 1, result, 0);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.getRGB(0, 0, 1, 2, result, 0);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.getRGB(0, 0, -1, 1, result, 0);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.getRGB(0, 0, 1, -1, result, 0);
      fail("Able to request pixels outside of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }

    instance = new PixelData(2, 2);
    result = new int[4];
    expResult = new int[]{2, 3, 4, 5};
    instance.setRGB(expResult);
    instance.getRGB(0, 0, 2, 2, result, 0);
    assertArrayEquals(result, expResult);

    result = new int[1];
    expResult = new int[]{2};
    instance.getRGB(0, 0, 1, 1, result, 0);
    assertArrayEquals(result, expResult);

    result = new int[1];
    expResult = new int[]{3};
    instance.getRGB(1, 0, 1, 1, result, 0);
    assertArrayEquals(result, expResult);

    result = new int[1];
    expResult = new int[]{4};
    instance.getRGB(0, 1, 1, 1, result, 0);
    assertArrayEquals(result, expResult);

    result = new int[1];
    expResult = new int[]{5};
    instance.getRGB(1, 1, 1, 1, result, 0);
    assertArrayEquals(result, expResult);

    instance = new PixelData(10, 10);
    expResult = new int[10 * 10];
    for (int i = 0; i < expResult.length; ++i) {
      expResult[i] = i;
    }
    instance.setRGB(expResult);

    result = new int[1];
    for (int x = 0; x < instance.width; ++x) {
      for (int z = 0; z < instance.height; ++z) {
//        System.out.println("getRGB(" + x + ", " + z + ", 1, 1, result, 0)");
        instance.getRGB(x, z, 1, 1, result, 0);
        expResult = new int[]{x + z * instance.width};
        assertEquals(result[0], expResult[0]);
      }
    }
  }

  @Test
  public void testSetRGB_intArr() {
    System.out.println("setRGB");
    int[] pixels = null;
    PixelData instance = new PixelData(1, 1);
    try {
      instance.setRGB(pixels);
      fail("Able to set null pixels");
    } catch (NullPointerException e) {
    }

    instance = new PixelData(1, 1);
    pixels = new int[]{1, 2, 3, 4};
    try {
      instance.setRGB(pixels);
      fail("Able to set too large pixels");
    } catch (IllegalArgumentException e) {
    }

    instance = new PixelData(10, 10);
    pixels = new int[]{1, 2, 3, 4};
    try {
      instance.setRGB(pixels);
      fail("Able to set too few pixels");
    } catch (IllegalArgumentException e) {
    }

    int[] result;
    int[] expResult;
    instance = new PixelData(1, 1);
    result = instance.getRGB();
    expResult = new int[]{0};
    assertArrayEquals(result, expResult);

    instance = new PixelData(10, 10);
    result = instance.getRGB();
    expResult = new int[10 * 10];
    assertArrayEquals(result, expResult);

    instance = new PixelData(2, 2);
    expResult = new int[]{2, 3, 4, 5};
    instance.setRGB(expResult);
    result = instance.getRGB();
    assertArrayEquals(result, expResult);
  }

  @Test
  public void testSetRGB_6args() {
    System.out.println("setRGB 6args");
    int[] input, output;
    PixelData instance;

    instance = new PixelData(1, 1);
    input = new int[]{0};
    instance.setRGB(0, 0, 1, 1, input, 0);
    output = instance.getRGB();
    assertArrayEquals(input, output);

    instance = new PixelData(10, 10);
    input = new int[10 * 10];
    instance.setRGB(0, 0, 1, 1, input, 0);
    output = instance.getRGB();
    assertArrayEquals(input, output);

    instance = new PixelData(10, 10);
    input = new int[10 * 10];
    instance.setRGB(0, 0, 10, 10, input, 0);
    output = instance.getRGB();
    assertArrayEquals(input, output);

    instance = new PixelData(10, 10);
    input = new int[10 * 10];
    for (int i = 0; i < input.length; ++i) {
      input[i] = i;
    }
    instance.setRGB(0, 0, 10, 10, input, 0);
    output = instance.getRGB();
    assertArrayEquals(input, output);

    input = new int[]{1, 2, 3, 4};
    output = new int[input.length];
    instance.setRGB(1, 1, 2, 2, input, 0);
    instance.getRGB(1, 1, 2, 2, output, 0);
    assertArrayEquals(input, output);

    instance = new PixelData(1, 1);
    input = new int[]{0};
    try {
      instance.setRGB(-1, 0, 1, 1, input, 0);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, -1, 1, 1, input, 0);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, 0, -1, 1, input, 0);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, 0, 1, -1, input, 0);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, 0, 2, 1, input, 0);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, 0, 1, 2, input, 0);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, 0, 1, 1, input, -1);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      instance.setRGB(0, 0, 1, 1, input, 1);
      fail("Able to set pixel data out of bounds.");
    } catch (ArrayIndexOutOfBoundsException e) {
    }

    instance = new PixelData(10, 10);
    int expectedResult;
    output = new int[1];
    for (int x = 0; x < instance.width; ++x) {
      for (int z = 0; z < instance.height; ++z) {
        expectedResult = x + z * instance.height;
//        System.out.println("Setting " + x + ", " + z + " to " + expectedResult);
        input = new int[]{expectedResult};
        instance.setRGB(x, z, 1, 1, input, 0);
        instance.getRGB(x, z, 1, 1, output, 0);
        assertArrayEquals(input, output);
      }
    }
  }

  @Test
  public void testMixedSetGetRGB() {
    System.out.println("mixedSetGetRGB");

    final int width = 200;
    final int height = 200;
    PixelData instanceA = new PixelData(width, height);
    PixelData instanceB = new PixelData(width, height);

    final int[] rgbSource = new int[width * height];
    final Random random = new Random();
    for (int i = 0; i < rgbSource.length; ++i) {
      rgbSource[i] = random.nextInt();
    }
    instanceA.setRGB(rgbSource);
    instanceB.setRGB(rgbSource);

    final int[] rgbCopy = new int[rgbSource.length];
    instanceA.getRGB(0, 0, width, height, rgbCopy, 0);
    instanceB.setRGB(0, 0, width, height, rgbCopy, 0);
    assertArrayEquals(instanceA.getRGB(), instanceB.getRGB());
    instanceB.getRGB(0, 0, width, height, rgbCopy, 0);
    instanceA.setRGB(0, 0, width, height, rgbCopy, 0);
    assertArrayEquals(instanceA.getRGB(), instanceB.getRGB());

    int x, y, w, h;
    for (int i = 0; i < 200; ++i) {
      x = random.nextInt(width - 1);
      y = random.nextInt(height - 1);
      w = random.nextInt(width - x);
      h = random.nextInt(height - y);

      instanceA.getRGB(x, y, w, h, rgbCopy, 0);
      instanceB.setRGB(x, y, w, h, rgbCopy, 0);
      assertArrayEquals(instanceA.getRGB(), instanceB.getRGB());
      instanceB.getRGB(x, y, w, h, rgbCopy, 0);
      instanceA.setRGB(x, y, w, h, rgbCopy, 0);
      assertArrayEquals(instanceA.getRGB(), instanceB.getRGB());
    }
  }

  @Test
  public void testAsImage() {
    System.out.println("asImage");
    int[] input, output;

    input = new int[]{0};
    PixelData instance = new PixelData(1, 1);
    instance.setRGB(input);
    BufferedImage result = instance.asImage();
    output = PixelData.getPixelsFromImage(result);
    assertNotNull(result);
    assertEquals(result.getWidth(), instance.width);
    assertEquals(result.getHeight(), instance.height);
    assertArrayEquals(input, output);

    instance = new PixelData(10, 10);
    input = new int[instance.width * instance.height];
    instance.setRGB(input);
    result = instance.asImage();
    output = PixelData.getPixelsFromImage(result);
    assertNotNull(result);
    assertEquals(result.getWidth(), instance.width);
    assertEquals(result.getHeight(), instance.height);
    assertArrayEquals(input, output);

    instance = new PixelData(10, 10);
    input = new int[instance.width * instance.height];
    for (int i = 0; i < input.length; ++i) {
      input[i] = i;
    }
    instance.setRGB(input);
    result = instance.asImage();
    output = PixelData.getPixelsFromImage(result);
    assertNotNull(result);
    assertEquals(result.getWidth(), instance.width);
    assertEquals(result.getHeight(), instance.height);
    assertArrayEquals(input, output);
  }

}
