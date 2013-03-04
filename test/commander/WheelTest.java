package commander;

import java.util.Random;
import com.aisandbox.util.Vector2;

public class WheelTest {

   private static void testRandom() {
      Random rnd = new Random();
      Wheel w = new Wheel();
      w.setFovAngle(Math.PI/6);
      for (int i = 0; i < 100; ++i) {
         double angle = 2*(rnd.nextDouble() * Math.PI) - Math.PI;
         w.addValue(angle, rnd.nextInt(500));
      }
      int seg = w.getBestSegmentNr();
      Vector2 v = w.getBestFacingDirection();
      System.out.println("seg: " + seg + ", facing: " + v);
   }
   
   public static void main(String[] args) {
      for (int i = 0; i < 20; ++i) {
         testRandom();
      }
   }
}
