package commander;

import java.util.Arrays;
import java.util.List;

import com.aisandbox.cmd.info.BotInfo;
import com.aisandbox.util.Area;
import com.aisandbox.util.Vector2;

public class Utils {
   public static boolean DEBUG = false;
   
   public static boolean isWithinAngle(double angleToTile, double facingAngle, double fovAngle) {
      double angleDiff = Math.abs(angleToTile - facingAngle);
      while (angleDiff > Math.PI) {
         angleDiff = 2*Math.PI - angleDiff;
      }
      /*while (angleDiff < -Math.PI) {
         angleDiff += Math.PI;
      }*/
      return 2*angleDiff <= fovAngle;
   }
   
   /**
    * Returns a value from -pi..pi
    * @param fromX
    * @param fromY
    * @param toX
    * @param toY
    * @return angle in radiants
    */
   public static double getAngleTo(double fromX, double fromY, double toX, double toY) {
      return getFacingAngle(toX - fromX, toY - fromY);
   }
   
   public static double getFacingAngle(Vector2 facingDirection) {
      return getFacingAngle(facingDirection.getX(), facingDirection.getY());
   }
   
   public static double getFacingAngle(double xDelta, double yDelta) {
      return Math.atan2(yDelta, xDelta);
   }
   
   public static Vector2 facingAngleToVector2(double angle) {
      return new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
   }
   
   /**
    * Returns the absolute of the angle difference between the two angles,
    * returns a value between 0 and PI.
    * @param angle1
    * @param angle2
    * @return
    */
   public static double absAngleDiff(double angle1, double angle2) {
      double diff = Math.abs(angle1 - angle2);
      if (diff >= Math.PI) {
         diff = 2*Math.PI - diff;
      }
      return diff;
   }

   public static String stateToString(int state) {
   	switch (state) {
   	case BotInfo.STATE_ATTACKING: return "ATTACKING";
   	case BotInfo.STATE_CHARGING: return "CHARGING";
   	case BotInfo.STATE_DEFENDING: return "DEFENDING";
   	case BotInfo.STATE_HOLDING: return "HOLDING";
   	case BotInfo.STATE_IDLE: return "IDLE";
   	case BotInfo.STATE_MOVING: return "MOVING";
   	case BotInfo.STATE_SHOOTING: return "SHOOTING";
   	case BotInfo.STATE_TAKING_ORDERS: return "TAKING_ORDERS";
   	case BotInfo.STATE_UNKNOWN: return "UNKNOWN";
   	case BotInfo.STATE_DEAD: return "DEAD";
   	default:
   		return "Unknown state " + state;
   	}
   }
   
   public static String toString(Vector2 v) {
      return String.format("[%2.1f,%2.1f]", v.getX(), v.getY());
   }
   
   public static String toString(List<Object> list) {
      StringBuilder buf = new StringBuilder();
      buf.append("[");
      boolean first = true;
      for (Object obj : list) {
         if (!first) {
            buf.append(",");
         }
         first = false;
         buf.append(obj.toString());
      }
      buf.append("]");
      return buf.toString();
   }
   
   public static String vector2ListToString(List<Vector2> list) {
      StringBuilder buf = new StringBuilder();
      buf.append("[");
      boolean first = true;
      for (Vector2 obj : list) {
         if (!first) {
            buf.append(",");
         }
         first = false;
         buf.append(toString(obj));
      }
      buf.append("]");
      return buf.toString();
   }
   private static final String[] combatEventNames = new String[] {
      "NONE", "BOT_KILLED", "FLAG_PICKED_UP", "FLAG_DROPPED", "FLAG_CAPTURED", "FLAG_RESTORED", "RESPAWN"

   };
   public static String combatEventToString(int event) {
      return combatEventNames[event];
   }
   
   public static void assrt(boolean b) {
      if (!b) {
         if (DEBUG) {
            throw new RuntimeException("Assertion failed: " + b);
         } else {
            try {
               throw new RuntimeException("Assertion failed: " + b);
            } catch(RuntimeException ex) {
               ex.printStackTrace();
            }
         }
      }
   }
   
   public static void reportError(String msg) {
      System.err.println(msg);
      assrt(false);
   }
   
   public static int[][] copyArray(int[][] arr) {
      int[][] result = new int[arr.length][arr[0].length];
      for (int i = 0; i < arr.length; ++i) {
         result[i] = Arrays.copyOf(arr[i], arr[i].length);
      }
      return result;
   }
   
   public static Vector2 getCenter(Area area) {
      return area.getMax().add(area.getMin()).scale(0.5f);
   }
   
   public static int clamp(int min, int max, int val) {
      return Math.min(Math.max(val, min), max);
   }
}
