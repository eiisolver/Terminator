package commander;

import java.util.*;

import com.aisandbox.cmd.info.BotInfo;


/**
 * Helper class that helps to select whether to attack or charge to
 * a target. Input is a desired path + risk.
 * Output is a first part of the path + whether to use charge or attack.
 * 
 * The idea is that squares are divided into safe or dangerous.
 * We will always attack over dangerous squares, and stop attacking when
 * there are at least SEG_SIZE safe squares.
 * @author louis
 *
 */
public class AttackOrChargeSelector {
   private static final int SEG_SIZE = 8;
   private List<Tile> path;
   private int[][] riskBasedCost;
   private List<Tile> pathSegment;
   
   /**
    * Analyzes path + determines whether to attack or charge.
    * Get segment to execute
    * @param path
    * @param riskBasedCost
    * @return BotInfo.STATE_CHARGING or BotInfo.STATE_ATTACKING
    */
   public int analyze(List<Tile> path, int[][] riskBasedCost) {
      int commandToPerform;
      this.path = path;
      this.riskBasedCost = riskBasedCost;
      int segSize = 0;
      int nrSafeSquares = nrConsecutiveSafeSquares(0);
      if (nrSafeSquares >= path.size() || nrSafeSquares >= SEG_SIZE) {
         commandToPerform = BotInfo.STATE_CHARGING;
         segSize = nrSafeSquares;
      } else {
         commandToPerform = BotInfo.STATE_ATTACKING;
         // find first square with >= 4 safe consecutive squares
         segSize = 0;
         while (nrConsecutiveLowRiskSquares(segSize) < SEG_SIZE && segSize < path.size()) {
            ++segSize;
         }
         if (segSize < path.size()) {
            // jump over the last non-safe square
            ++segSize;
         }
      }
      pathSegment = new ArrayList<Tile>();
      for (int i = 0; i < segSize; ++i) {
         pathSegment.add(path.get(i));
      }
      return commandToPerform;
   }
   
   /**
    * Returns segment, must  be called after analyze.
    * @return
    */
   public List<Tile> getPathSegment() {
      return pathSegment;
   }
   
   public int nrConsecutiveLowRiskSquares(int index) {
      int count = 0;
      boolean safe = true;
      for (int i = index; safe && i < path.size(); ++i) {
         safe = isLowRiskSquare(i);
         if (safe) {
            ++count;
         }
      }
      return count;
   }
   
   public int nrConsecutiveSafeSquares(int index) {
      int count = 0;
      boolean safe = true;
      for (int i = index; safe && i < path.size(); ++i) {
         safe = isSafe(i);
         if (safe) {
            ++count;
         }
      }
      return count;
   }
   
   /**
    * A tile is "safe" if its risk is low, or if the average of this+next 3 tiles
    * is low. The idea is that we will always go in Attack mode over non-safe
    * squares.
    * @param index
    * @return
    */
   public boolean isSafe(int index) {
      boolean safe = isLowRiskSquare(index);
      if (!safe) {
         int count = 0;
         int totalRisk = 0;
         for (int i = index; i < index + SEG_SIZE && i < path.size(); ++i) {
            ++count;
            Tile tile = path.get(i);
            totalRisk += riskBasedCost[tile.x][tile.y];
         }
         safe = isLowRisk(totalRisk/count);
      }
      return safe;
   }
   
   private boolean isLowRiskSquare(int index) {
      Tile t = path.get(index);
      return isLowRisk(riskBasedCost[t.x][t.y]);
   }

   private boolean isLowRisk(int risk) {
      return risk < MyCommander.HIGH_RISK;
   }
}
