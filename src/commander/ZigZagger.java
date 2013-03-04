package commander;

import java.util.*;

import com.aisandbox.util.Vector2;

public class ZigZagger {
   private static double[] angles = new double[] {
      -Math.PI/3, Math.PI/3
   };
   private Set<Tile> attackedTiles = new HashSet<Tile>();
   private boolean[] zigZagAdded;
   private int[][] extraValue;
   
   public void addZigZag(final List<Vector2> path, final double fovAngle, final int[][] squareValue) {
      if (path.size() < 2) {
         return;
      }
      Log.log("addZigZag <- " + Utils.vector2ListToString(path) + ", size: " + path.size());
      attackedTiles.clear();
      zigZagAdded = new boolean[path.size()];
      extraValue = new int[angles.length][path.size()];
      // Calculate which tiles we will see when walking the
      // unzigzagged path, put result in attackedTiles
      for (int i = 0; i < path.size() - 1; ++i) {
         Vector2 from = path.get(i);
         Vector2 to = path.get(i+1);
         double angle = Utils.getAngleTo(from.getX(), from.getY(), to.getX(), to.getY());
         Tile t = Tile.get(from);
         t.visitAttackedTiles(angle, fovAngle, new TileVisitor() {

            @Override
            public boolean visit(Tile t) {
               attackedTiles.add(t);
               return true;
            }
         });
      }
      // calculate at each square/each angle how much value we would add
      // by adding a zigzag; put result in extraValue
      ValueCalculatingVisitor visitor = new ValueCalculatingVisitor();
      visitor.squareValue = squareValue;
      for (int a = 0; a < angles.length; ++a) {
         for (int i = 0; i < path.size() - 1; ++i) {
            Vector2 from = path.get(i);
            Vector2 to = path.get(i+1);
            Tile fromTile = Tile.get(from);
            Tile toTile = Tile.get(to);
            if (toTile.isWalkable) {
               double angle = Utils.getAngleTo(from.getX(), from.getY(), to.getX(), to.getY());
               angle += angles[a];
               visitor.clear();
               fromTile.visitAttackedTiles(angle, fovAngle, visitor);
               extraValue[a][i] = visitor.value;
            } else {
               extraValue[a][i] = -1;
            }
         }
      }
      boolean stop = false;
      while (!stop) {
         // find best zigzag on the path
         int bestIndex = -1;
         int bestAngle = 0;
         int bestValue = 10;
         for (int i = 0; i < path.size() - 1; ++i) {
            if (!zigZagAdded[i] && !zigZagAdded[i+1]) {
               for (int a = 0; a < angles.length; ++a) {
                  if (extraValue[a][i] > bestValue) {
                     bestIndex = i;
                     bestAngle = a;
                     bestValue = extraValue[a][i];
                  }
               }
            }
         }
         if (bestIndex >= 0) {
            addZigZag(path, bestIndex, bestAngle);
         } else {
            // no more zigzag added
            stop = true;
         }
      }
      Log.log("addZigZag -> " + Utils.vector2ListToString(path) + ", size: " + path.size());
   }
   
   /**
    * Replace path[index+1] by the zigzag point with the given angle
    */
   private void addZigZag(List<Vector2> path, int index, int angleIndex) {
      Vector2 from = path.get(index);
      Vector2 to = path.get(index+1);
      double angle = Utils.getAngleTo(from.getX(), from.getY(), to.getX(), to.getY());
      angle += angles[angleIndex];
      Vector2 zigV = null;
      // try to find a walkable target in the direction of the angle
      for (int i = 0; zigV == null && i <= 2; ++i) {
         Vector2 v = from.add(Utils.facingAngleToVector2(angle).scale(MyCommander.SMALL_DIST + i));
         
         if (Tile.isValid((int)v.getX(), (int)v.getY())) {
            Tile zigTile = Tile.get(v);
            if (zigTile.isWalkable) {
               zigV = v;
               break;
            }
         }
      }
      if (zigV != null) {
         path.remove(index+1);
         path.add(index+1, zigV);
      } else {
         Log.log("addZigZag: No walkable spot found, from = " + from + ", to = " + to + ", natural point: " + from.add(Utils.facingAngleToVector2(angle).scale(MyCommander.SMALL_DIST)));
      }
      zigZagAdded[index] = true;
      zigZagAdded[index+1] = true;
      if (index+2 < zigZagAdded.length) {
         zigZagAdded[index+2] = true;
      }
   }
   
   private class ValueCalculatingVisitor implements TileVisitor {
      int value;
      int[][] squareValue;
      
      public void clear() {
         value = 0;
      }

      @Override
      public boolean visit(Tile t) {
         if (!attackedTiles.contains(t)) {
            // this tile is not seen on the straight path; add its value
            value += squareValue[t.x][t.y];
         }
         return true;
      }
   }
}
