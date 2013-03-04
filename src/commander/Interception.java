package commander;

import java.util.*;

/**
 * Calculates intercept points for a path that is expected to be
 * taken by "the victim".
 * @author louis
 *
 */
public class Interception {
   public List<Tile> path;
   private Distance distToPath;
   private MyCommander commander;
   private int firingDist;
   
   /**
    * 
    * @param path The path that the victim is likely to use.
    * @param commander
    */
   public Interception(MyCommander commander) {
      this.commander = commander;
      firingDist = (int)commander.getLevelInfo().getFiringDistance();
   }
   
   public void update(List<Tile> path) {
      this.path = path;
      distToPath = new Distance(commander.walkable, commander.unitCost);
      for (int i = 0; i < path.size(); ++i) {
         Tile t = path.get(i);
         distToPath.addInitial(t.x, t.y);
      }
      distToPath.calcDistances(path.size() + firingDist);
   }
   public void calcInterceptInfo(Tile t, InterceptInfo info) {
      int dist = distToPath.getDistance(t.x, t.y);
      info.reset();
      if (dist < commander.width + commander.height) {
         List<Tile> pathToInterception = distToPath.getPathFrom(t.x, t.y);
         Tile tileOnPath = pathToInterception.get(pathToInterception.size()-1);
         int indexOnPath = findIndex(tileOnPath);
         int distToPath = pathToInterception.size();
         if (distToPath < indexOnPath + firingDist) {
            // the interceptor comes in time to the interception point
            info.pathToInterception = pathToInterception;
            info.distToImpact = Math.max(distToPath, indexOnPath-firingDist);
            info.isBehindVictim = indexOnPath < distToPath;
            info.victimDistToInterceptionPoint = indexOnPath;
         }
      }
   }
   
   private int findIndex(Tile tileOnPath) {
      for (int i = 0; i < path.size(); ++i) {
         if (path.get(i) == tileOnPath) {
            return i;
         }
      }
      return -1;
   }
   
   public String toString() {
      return path.get(0) + "-" + path.get(path.size()-1) + ", pathlen: " + path.size();
   }

}
