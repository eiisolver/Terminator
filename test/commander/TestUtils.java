package commander;

import com.aisandbox.util.Vector2;

public class TestUtils {
   
   private static void test(float x, float y) {
      Vector2 v = new Vector2(x, y);
      double angle = Utils.getFacingAngle(v);
      Vector2 newV = Utils.facingAngleToVector2(angle);
      System.out.println("x: " + x + ", y: " + y + ", v: " + v + ", newV: " + newV + ", angle: " + angle);
   }

   public static void main(String[] args) {
      test(1, 0);
      test(-1, 0);
      test(0, 1);
      test(0, -1);
      test(1,1);
      System.out.println((int)2.9f);
   }
}
