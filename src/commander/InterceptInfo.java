package commander;

import java.util.*;

/**
 * Info about interception possibilities from some tile.
 * @author louis
 *
 */
public class InterceptInfo {
   /** Path to interception point, null if no interception is possible */
   public List<Tile> pathToInterception;
   /** Distance in meters to point where victim is in firing range of interceptor (given 100% clear view) */
   public int distToImpact;
   /** True if victim gets earlier than interceptor to interception point */
   public boolean isBehindVictim;
   /** Victim's distance to interception point in meters */
   public int victimDistToInterceptionPoint;
   
   public Tile getInterceptionPoint() {
      if (pathToInterception != null && !pathToInterception.isEmpty()) {
         return pathToInterception.get(pathToInterception.size()-1);
      }
      return null;
   }
   
   /**
    * Distance for interceptor to interception point in meters.
    * @return
    */
   public int distToInterceptionPoint() {
      if (pathToInterception != null && !pathToInterception.isEmpty()) {
         return pathToInterception.size();
      }
      return 10000;
   }
   
   public void reset() {
      pathToInterception = null;
      distToImpact = 10000;
   }

   public String toString() {
      return "Intercept point: " + getInterceptionPoint() + ", dist: " + distToInterceptionPoint() + ", distToImpact: " + distToImpact + ", behind: " + isBehindVictim;
   }
}
