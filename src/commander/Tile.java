package commander;

import java.util.*;

import com.aisandbox.util.Vector2;

public class Tile {
   private static final int BITS_PER_DIM = 16;
   private static final int MASK_PER_DIM = (1 << BITS_PER_DIM)-1;
   private static Tile[][] tiles;
   private static int width;
   private static int height;
   public static int maxNrAttackable;
   private static CountingTilesVisitor COUNTING_TILES_VISITOR = new CountingTilesVisitor();
   private static ValueCalculatingTilesVisitor TILE_VALUE_VISITOR = new ValueCalculatingTilesVisitor();
   
   public int x;
   public int y;
   /** true if we can walk over this tile */
   public boolean isWalkable;
   /** true if we can shoot over this tile (height less than 2 meters) */
   public boolean isShootable;
   /** Contains all tiles that are within shooting range of this tile */
   public Tile[] attackedTiles;
   /** Contains for every attacked tile the shooting angle to that tile */
   public double[] attackAngles;
   /** Contains all tiles that are within visibility range of this tile */
   public Tile[] seenTiles;
   /** Contains for every seen tile the shooting angle to that tile */
   public double[] seenAngles;
   public Node node = new Node(this);
   public Distance distance;
   
   public static void init(int w, int h, int fireDist) {
      width = w;
      height = h;
      maxNrAttackable = (int)(fireDist*fireDist*Math.PI);
      tiles = new Tile[width][height];
      for (int i = 0; i < width; ++i) {
         for (int j = 0; j < height; ++j) {
            Tile t = new Tile();
            t.x = i;
            t.y = j;
            tiles[i][j] = t;
         }
      }
   }
   
   public static Tile get(int x, int y) {
      if (x >= 0 && x < width && y >= 0 && y < height) {
         return tiles[x][y];
      } else {
         Tile t = new Tile();
         t.x = x;
         t.y = y;
         return t;
      }
   }
   
   public static Tile get(Vector2 v) {
      return tiles[(int)v.getX()][(int)v.getY()];
   }
   
   public Vector2 toVector2() {
      return new Vector2(x + 0.5f, y + 0.5f);
   }
   
   /**
    * Gets a measure how visible this tile is, from 0
    * (not visible at all) to 100 (can be attacked from
    * all possible squares)
    * @return
    */
   public int getVisibilityPercentage() {
      return (100*attackedTiles.length)/maxNrAttackable;
   }
   
   /**
    * Checks if x,y is on the board.
    * @param x
    * @param y
    * @return
    */
   public static boolean isValid(int x, int y) {
      return x >= 0 && x < width && y >= 0 && y < height;
   }
   
   /**
    * An encoded tile is an int that holds x in higher 16 bits, y in lower 16 bits.
    * Both x and y must be >= 0
    * @param encodedTile
    * @return x-component of the encoded int
    */
   public static final int getX(int encodedTile) {
      return (encodedTile >> BITS_PER_DIM) & MASK_PER_DIM;
   }

   /**
    * An encoded tile is an int that holds x in higher 16 bits, y in lower 16 bits
    * Both x and y must be >= 0
    * @param encodedTile
    * @return x-component of the encoded int
    */
   public static final int getY(int encodedTile) {
      return encodedTile & MASK_PER_DIM;
   }
   
   /**
    * An encoded tile is an int that holds x in higher 16 bits, y in lower 16 bits
    * Both x and y must be >= 0
    * @param encodedTile
    * @return x-component of the encoded int
    */
   public static final int encode(int x, int y) {
      return (x << BITS_PER_DIM) | y;
   }

   /**
    * An encoded tile is an int that holds x in higher 16 bits, y in lower 16 bits
    * Both x and y must be >= 0
    * @param encodedTile
    * @return x-component of the encoded int
    */
   public static final int encode(Tile t) {
      return (t.x << BITS_PER_DIM) | t.y;
   }
   
   /**
    * An encoded tile is an int that holds x in higher 16 bits, y in lower 16 bits
    * Both x and y must be >= 0
    * @param encodedTile
    * @return x-component of the encoded int
    */
   public static final int encode(Vector2 v) {
      return (((int)v.getX()) << BITS_PER_DIM) | ((int)v.getY());
   }
   
   public static final Vector2 decodeToVector(int encodedTile) {
      return new Vector2(0.5f + getX(encodedTile), 0.5f + getY(encodedTile));
   }
   
   public void calcAttackedTiles(int firingDistance, boolean attack) {
      List<Tile> list = new ArrayList<Tile>(firingDistance*firingDistance/2);
      for (int i = x-firingDistance; i <= x+firingDistance; ++i) {
         for (int j = y-firingDistance; j <= y+firingDistance; ++j) {
            if (isValid(i, j) && (i-x)*(i-x) + (j-y)*(j-y) <= firingDistance*firingDistance) {
               Tile t = Tile.get(i, j);
               if (t.isWalkable && calcCanAttack(t)) {
                  list.add(t);
               }
            }
         }
      }
      Tile[] tiles = list.toArray(new Tile[0]);
      double[] angles = new double[tiles.length];
      for (int i = 0; i < tiles.length; ++i) {
         Tile t = tiles[i];
         angles[i] = getAngleTo(t);
      }
      if (attack) {
         attackedTiles = tiles;
         attackAngles = angles;
      } else {
         seenTiles = tiles;
         seenAngles = angles;
      }
   }
   
   /**
    * Visits all tiles that are under attack by this tile
    */
   public void visitAttackedTiles(double facingAngle, double fovAngle, TileVisitor visitor) {
      visitAttackedTiles(facingAngle, fovAngle, visitor, attackedTiles, attackAngles);
   }
   
   /**
    * Visits all tiles that are seen by this tile
    */
   public void visitSeenTiles(double facingAngle, double fovAngle, TileVisitor visitor) {
      visitAttackedTiles(facingAngle, fovAngle, visitor, seenTiles, seenAngles);
   }
   
   private void visitAttackedTiles(double facingAngle, double fovAngle, TileVisitor visitor, Tile[] tiles, double[] angles) {
      if (fovAngle <= 0) {
         return;
      }
      if (tiles == null) {
         System.err.println("attacked Tiles is null for tile " + this + ", walkable: " + isWalkable);
      }
      for (int i = 0; i < tiles.length; ++i) {
         if (Utils.isWithinAngle(angles[i], facingAngle, fovAngle)) {
            visitor.visit(tiles[i]);
         }
      }
   }
   
   /**
    * Checks if a bot standing on this tile attacks the target
    * @param facingAngle
    * @param fovAngle
    * @param target
    * @return
    */
   public boolean attacks(double facingAngle, double fovAngle, Tile target) {
      return attacks(facingAngle, fovAngle, target, attackedTiles, attackAngles);
      
   }
   private boolean attacks(double facingAngle, double fovAngle, Tile target, Tile[] tiles, double[] angles) {
      for (int i = 0; i < tiles.length; ++i) {
         if (tiles[i] == target) {
            if (Utils.isWithinAngle(angles[i], facingAngle, fovAngle)) {
               return true;
            } else {
               return false;
            }
         }
      }
      return false;
   }
   
   public int calcNrAttackedTiles(double facingAngle, double fovAngle) {
      COUNTING_TILES_VISITOR.reset();
      visitAttackedTiles(facingAngle, fovAngle, COUNTING_TILES_VISITOR);
      return COUNTING_TILES_VISITOR.count;
   }
   
   public int calcValueOfAttackedTiles(double facingAngle, double fovAngle, int[][] tileValue) {
      TILE_VALUE_VISITOR.reset(tileValue);
      visitAttackedTiles(facingAngle, fovAngle, TILE_VALUE_VISITOR);
      return TILE_VALUE_VISITOR.value;
   }
   
   public Tile findDefendSpot(int[][] riskBasedCost, int maxRisk, int maxWalkDist) {
      int maxDist = 12;
      // find all possible defend spots that can defend some squares around this spot
      Set<Tile> attackedSet = getAttackedSet(x, y, maxDist);
      Set<Tile> s = getAttackedSet(x-2, y, maxDist+2);
      if (!s.isEmpty()) {
         attackedSet.retainAll(s);
      }
      s = getAttackedSet(x+2, y, maxDist+2);
      if (!s.isEmpty()) {
         attackedSet.retainAll(s);
      }
      s = getAttackedSet(x, y-2, maxDist+2);
      if (!s.isEmpty()) {
         attackedSet.retainAll(s);
      }
      s = getAttackedSet(x, y+2, maxDist+2);
      if (!s.isEmpty()) {
         attackedSet.retainAll(s);
      }
      // of these squares, find the best defending spot
      Tile bestSpot = this;
      int lowestRisk = maxRisk;
      for (Tile t : attackedSet) {
         if (t.distance.getDistance(x, y) <= maxWalkDist) {
            int risk = riskBasedCost[t.x][t.y];
            risk += getVisibilityPercentage();
            if (risk < lowestRisk) {
               bestSpot = t;
               lowestRisk = risk;
            }
         }
      }
      return bestSpot;
   }
   
   private static Set<Tile> getAttackedSet(int x, int y, int maxDist) {
      Set<Tile> attackedSet = new HashSet<Tile>();
      if (isValid(x, y)) {
         Tile t = get(x, y);
         if (t.isWalkable) {
            for (Tile t2 : t.attackedTiles) {
               if (t.squareDist(t2) <= maxDist*maxDist) {
                  attackedSet.add(t2);
               }
            }
         }
      }
      return attackedSet;
   }
   
   public int squareDist(Tile otherTile) {
      return (x-otherTile.x)*(x-otherTile.x) + (y-otherTile.y)*(y-otherTile.y); 
   }

   
   public double getAngleTo(Tile otherTile) {
      return Utils.getAngleTo(x, y, otherTile.x, otherTile.y);
   }
   
   private boolean calcCanAttack(Tile otherTile) {
      return Tile.isVisible(x, y, otherTile.x, otherTile.y, new TileVisitor() {

         @Override
         public boolean visit(Tile t) {
            return t.isShootable;
         }
      });
   }
   
   /**
    * Checks if tile x,y can see x2,y2. All tiles between these points are
    * visited.
    * 
    * @param x
    * @param y
    * @param x2
    * @param y2
    * @param visitor
    * @return
    */
   public static boolean isVisible(int x, int y, int x2, int y2, TileVisitor visitor) {
      int w = x2 - x;
      int h = y2 - y;
      int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
      if (w < 0)
         dx1 = -1;
      else if (w > 0)
         dx1 = 1;
      if (h < 0)
         dy1 = -1;
      else if (h > 0)
         dy1 = 1;
      if (w < 0)
         dx2 = -1;
      else if (w > 0)
         dx2 = 1;
      int longest = Math.abs(w);
      int shortest = Math.abs(h);
      if (!(longest > shortest)) {
         longest = Math.abs(h);
         shortest = Math.abs(w);
         if (h < 0)
            dy2 = -1;
         else if (h > 0)
            dy2 = 1;
         dx2 = 0;
      }
      int numerator = longest >> 1;
      for (int i = 0; i <= longest; i++) {
         if (!visitor.visit(Tile.get(x, y))) {
            return false;
         }
         numerator += shortest;
         if (!(numerator < longest)) {
            numerator -= longest;
            x += dx1;
            y += dy1;
         } else {
            x += dx2;
            y += dy2;
         }
      }
      return true;
   }
   
   /**
    * Checks with higher precision if it is possible to see point2 from point1.
    * 
    * @param x
    * @param y
    * @param x2
    * @param y2
    * @param visitor
    * @return
    */
   public static boolean isReallyVisible(Vector2 point1, Vector2 point2) {
      // implementation uses higher precision grid
      int x1 = Math.round(10*point1.getX());
      int y1 = Math.round(10*point1.getY());
      int x2 = Math.round(10*point2.getX());
      int y2 = Math.round(10*point2.getY());
      return isVisible(x1, y1, x2, y2, new TileVisitor() {

         @Override
         public boolean visit(Tile t) {
            Tile realTile = Tile.get(t.x/10, t.y/10);
            return realTile.isShootable;
         }
         
      });
   }

   
   public String toString() {
      return "(x:" + x + ", y:" + y + ")";
   }
   
   private static class CountingTilesVisitor implements TileVisitor {
      int count = 0;
      
      public void reset() {
         count = 0;
      }

      @Override
      public boolean visit(Tile t) {
         ++count;
         return true;
      }
   }
   
   private static class ValueCalculatingTilesVisitor implements TileVisitor {
      int value = 0;
      int[][] tileValue;
      
      public void reset(int[][] tileValue) {
         this.tileValue = tileValue;
         value = 0;
      }

      @Override
      public boolean visit(Tile t) {
         value += tileValue[t.x][t.y];
         return true;
      }
   }
}
