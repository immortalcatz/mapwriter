/*
 */
package mapwriter.mapgen;

import mapwriter.map.Region;
import mapwriter.map.RegionID;
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
public class RegionIDTest {

  public RegionIDTest() {
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
  public void testByCoordinates() {
    System.out.println("byCoordinates");
    RegionID expResult, result;

    expResult = new RegionID(0, 0);
    result = RegionID.byCoordinates(0, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byCoordinates(1, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byCoordinates(0, 1);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byCoordinates(1, 1);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byCoordinates(1023, 1023);
    assertEquals(result, expResult);

    expResult = new RegionID(-1, 0);
    result = RegionID.byCoordinates(-1, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, -1);
    result = RegionID.byCoordinates(0, -1);
    assertEquals(result, expResult);

    expResult = new RegionID(-1, -1);
    result = RegionID.byCoordinates(-1, -1);
    assertEquals(result, expResult);

    expResult = new RegionID(1, 0);
    result = RegionID.byCoordinates(1024, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 1);
    result = RegionID.byCoordinates(0, 1024);
    assertEquals(result, expResult);

    expResult = new RegionID(1, 1);
    result = RegionID.byCoordinates(1024, 1024);
    assertEquals(result, expResult);

    expResult = new RegionID(-2, 0);
    result = RegionID.byCoordinates(-1025, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, -2);
    result = RegionID.byCoordinates(0, -1025);
    assertEquals(result, expResult);

    expResult = new RegionID(-2, -2);
    result = RegionID.byCoordinates(-1025, -1025);
    assertEquals(result, expResult);
  }

  @Test
  public void testByChunk() {
    System.out.println("byChunk");
    RegionID expResult, result;

    expResult = new RegionID(0, 0);
    result = RegionID.byChunk(0, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byChunk(1, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byChunk(0, 1);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byChunk(1, 1);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 0);
    result = RegionID.byChunk(63, 63);
    assertEquals(result, expResult);

    expResult = new RegionID(-1, 0);
    result = RegionID.byChunk(-1, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, -1);
    result = RegionID.byChunk(0, -1);
    assertEquals(result, expResult);

    expResult = new RegionID(-1, -1);
    result = RegionID.byChunk(-1, -1);
    assertEquals(result, expResult);

    expResult = new RegionID(1, 0);
    result = RegionID.byChunk(64, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, 1);
    result = RegionID.byChunk(0, 64);
    assertEquals(result, expResult);

    expResult = new RegionID(1, 1);
    result = RegionID.byChunk(64, 64);
    assertEquals(result, expResult);

    expResult = new RegionID(-2, 0);
    result = RegionID.byChunk(-65, 0);
    assertEquals(result, expResult);

    expResult = new RegionID(0, -2);
    result = RegionID.byChunk(0, -65);
    assertEquals(result, expResult);

    expResult = new RegionID(-2, -2);
    result = RegionID.byChunk(-65, -65);
    assertEquals(result, expResult);
  }

  @Test
  public void testToFilename() {
    System.out.println("toFilename");
    RegionID instance = new RegionID(0, 0);
    String expResult = "0.0." + Region.IMAGE_TYPE;
    String result = instance.toFilename();
    assertEquals(result, expResult);

    instance = new RegionID(1, 0);
    expResult = "1.0." + Region.IMAGE_TYPE;
    result = instance.toFilename();
    assertEquals(result, expResult);

    instance = new RegionID(0, 1);
    expResult = "0.1." + Region.IMAGE_TYPE;
    result = instance.toFilename();
    assertEquals(result, expResult);

    instance = new RegionID(1, 1);
    expResult = "1.1." + Region.IMAGE_TYPE;
    result = instance.toFilename();
    assertEquals(result, expResult);
    instance = new RegionID(-1, 0);
    expResult = "-1.0." + Region.IMAGE_TYPE;
    result = instance.toFilename();
    assertEquals(result, expResult);

    instance = new RegionID(0, -1);
    expResult = "0.-1." + Region.IMAGE_TYPE;
    result = instance.toFilename();
    assertEquals(result, expResult);

    instance = new RegionID(-1, -1);
    expResult = "-1.-1." + Region.IMAGE_TYPE;
    result = instance.toFilename();
    assertEquals(result, expResult);
  }

  @Test
  public void testToString() {
    System.out.println("toString");
    RegionID instance = new RegionID(0, 0);
    String result = instance.toString();
    assertNotNull(result);
    assertTrue(result.length() >= 2);
    // there isn't much more to test, as the result is not defined
  }

  @Test
  public void testHashCode() {
    System.out.println("hashCode");
    RegionID instance = new RegionID(0, 0);
    RegionID instance2 = new RegionID(0, 0);
    assertEquals(instance.hashCode(), instance2.hashCode());

    RegionID instance3 = new RegionID(1, 0);
    assertNotEquals(instance.hashCode(), instance3.hashCode());

    final Random random = new Random();
    final RegionID[] regions = new RegionID[200];
    for (int i = 0; i < regions.length; ++i) {
      regions[i] = new RegionID(random.nextInt(200) - 100, random.nextInt(200) - 100);
    }

    for (int i = 0; i < regions.length; ++i) {
      instance = regions[i];
      for (int other = 0; other < regions.length; ++other) {
        instance2 = regions[other];
//        System.out.println("Testing R1{" + instance.regionX + ", " + instance.regionZ + "} #" + instance.hashCode() + " vs R2{" + instance2.regionX + ", " + instance2.regionZ + "} #" + instance2.hashCode() + " == " + (instance.hashCode() == instance2.hashCode()));
        if (instance.equals(instance2)) {
          assertEquals(instance.hashCode(), instance2.hashCode());
        } else {
          assertNotEquals(instance.hashCode(), instance2.hashCode());
        }
      }
    }
  }

  @Test
  public void testEquals() {
    System.out.println("equals");
    RegionID instance = new RegionID(0, 0);
    RegionID instance2 = new RegionID(0, 0);
    assertTrue(instance.equals(instance2));
    assertTrue(instance2.equals(instance));

    RegionID instance3 = new RegionID(1, 0);
    assertFalse(instance.equals(instance3));
    assertFalse(instance3.equals(instance));

    final Random random = new Random();
    final RegionID[] regions = new RegionID[200];
    for (int i = 0; i < regions.length; ++i) {
      regions[i] = new RegionID(random.nextInt(200) - 100, random.nextInt(200) - 100);
    }

    for (int i = 0; i < regions.length; ++i) {
      instance = regions[i];
      for (int other = 0; other < regions.length; ++other) {
        instance2 = regions[other];
//        System.out.println("Testing R1{" + instance.regionX + ", " + instance.regionZ + "} vs R2{" + instance2.regionX + ", " + instance2.regionZ + "} == " + instance.equals(instance2));
        if ((instance.regionX == instance2.regionX) && (instance.regionZ == instance2.regionZ)) {
          assertTrue(instance.equals(instance2));
          assertTrue(instance2.equals(instance));
        } else {
          assertFalse(instance.equals(instance2));
          assertFalse(instance2.equals(instance));
        }
      }
    }
  }

  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    RegionID instance = new RegionID(0, 0);
    RegionID instance2 = new RegionID(0, 0);
    assertEquals(instance.compareTo(instance2), 0);
    assertEquals(instance2.compareTo(instance), 0);

    instance2 = new RegionID(1, 0);
    assertEquals(instance.compareTo(instance2), -1);
    assertEquals(instance2.compareTo(instance), 1);

    final Random random = new Random();
    final RegionID[] regions = new RegionID[200];
    for (int i = 0; i < regions.length; ++i) {
      regions[i] = new RegionID(random.nextInt(200) - 100, random.nextInt(200) - 100);
    }

    for (int i = 0; i < regions.length; ++i) {
      instance = regions[i];
      for (int other = 0; other < regions.length; ++other) {
        instance2 = regions[other];
//        System.out.println("Testing R1{" + instance.regionX + ", " + instance.regionZ + "} vs R2{" + instance2.regionX + ", " + instance2.regionZ + "}");
        if ((instance.regionX == instance2.regionX) && (instance.regionZ == instance2.regionZ)) {
          assertEquals(instance.compareTo(instance2), 0);
          assertEquals(instance2.compareTo(instance), 0);
        } else if ((instance.regionX < instance2.regionX) || ((instance.regionX == instance2.regionX) && (instance.regionZ < instance2.regionZ))) {
          assertEquals(instance.compareTo(instance2), -1);
          assertEquals(instance2.compareTo(instance), 1);
        } else if ((instance.regionX > instance2.regionX) || ((instance.regionX == instance2.regionX) && (instance.regionZ > instance2.regionZ))) {
          assertEquals(instance.compareTo(instance2), 1);
          assertEquals(instance2.compareTo(instance), -1);
        } else {
          fail("Missing case for R1{" + instance.regionX + ", " + instance.regionZ + "} vs R2{" + instance2.regionX + ", " + instance2.regionZ + "}");
        }
      }
    }
  }

}
