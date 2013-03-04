package commander;

import java.util.*;

import com.aisandbox.cmd.info.BotInfo;

/**
 * 
 * @author louis
 *
 */
public class TestAttackOrChargeSelector {
   private static final int WIDTH = 50;

   public static void test(int[] risks, boolean isSafe) {
      List<Tile> path = new ArrayList<Tile>();
      int[][] riskBasedCost = new int[WIDTH][WIDTH];
      for (int i = 0; i < risks.length; ++i) {
         path.add(Tile.get(i, i));
         riskBasedCost[i][i] = risks[i];
      }
      System.out.print("test for risks: ");
      for (int i = 0; i < risks.length; ++i) {
         System.out.print(i + ":" + risks[i]+ " ");
      }
      System.out.println();
      AttackOrChargeSelector selector = new AttackOrChargeSelector();
      int command = selector.analyze(path, riskBasedCost);
      System.out.println("-> command: " + Utils.stateToString(command));
      List<Tile> segment = selector.getPathSegment();
      System.out.print("-> segment: ");
      for (int i = 0; i < segment.size(); ++i) {
         Tile t = segment.get(i);
         if (t.x != i || t.y != i) {
            throw new RuntimeException(" Wrong! t= " + t + ", i= " + i);
         }
         System.out.print(i + ":" + risks[i] + " ");
      }
      System.out.println();
      if ((isSafe && command != BotInfo.STATE_CHARGING) || (!isSafe && command != BotInfo.STATE_ATTACKING)) {
         throw new RuntimeException("incorrect command!");
      }
   }
   
   public static void main(String[] args) {
      Tile.init(WIDTH, WIDTH, 10);
      test(new int[] {20, 200, 250}, true);
      test(new int[] {20, 200, 250, 290, 100, 150}, true);
      test(new int[] {20, 200, 250, 320, 100, 150}, true);
      test(new int[] {2000, 20, 200, 250, 290, 100, 150}, false);
      test(new int[] {2000, 20, 200, 250, 320, 100, 150}, false);
      test(new int[] {20, 200, 250, 320, 100, 150, 500, 500, 500, 500, 500, 10, 10, 10}, true);
   }
}
