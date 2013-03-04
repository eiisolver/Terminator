package commander;

import com.aisandbox.util.Vector2;

/**
 * Point that is suitable for ambushing
 * @author louis
 *
 */
public class AmbushPoint implements Comparable<AmbushPoint> {
   public Tile tile;
   public Vector2 bestFacingDirection;
   public int value;
   
   @Override
   public int compareTo(AmbushPoint point) {
      return point.value - value;
   }

   @Override
   public String toString() {
      return tile + ", facing: " + Utils.toString(bestFacingDirection) + ", val: " + value;
   }

}
