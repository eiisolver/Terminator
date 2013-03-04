package commander;

import java.util.*;

import com.aisandbox.util.Vector2;

/**
 * Contains distances to interesting points
 * 
 * @author louis
 * 
 */
public class Distance {
   public static final int FAR_AWAY = 100000000;
   private boolean[][] walkable;
   /** Contains for every square the cost for walking over that square */
   private int[][] cost;
   /** Will contain distance to every square */
   public int[] dist;
   /** Predecessor of every square */
   public int[] predecessor;
   private int width;
   private int height;
   private int[] prevAdded;
   private int nrPrevAdded = 0;
   private int[] added;
   public int nrAdded = 0;
   public int maxDist = 0;
   /** help variable */
   private static int[] neighbours = new int[4];
   private int nrNeighbours;
   private boolean reverseNeighbours;

   public Distance(boolean[][] walkable, int[][] cost) {
      this.walkable = walkable;
      this.cost = cost;
      width = walkable.length;
      height = walkable[0].length;
      dist = new int[width * height];
      predecessor = new int[dist.length];
      prevAdded = new int[dist.length];
      added = new int[prevAdded.length];
      clear();
   }

   public void clear() {
      Arrays.fill(dist, FAR_AWAY);
      Arrays.fill(predecessor, -1);
      nrAdded = 0;
      nrPrevAdded = 0;
      maxDist = 0;
   }

   public int getDistance(Vector2 target) {
      return getDistance((int) Math.floor(target.getX()), (int) Math.floor(target.getY()));
   }

   /**
    * Return distance, FAR_AWAY if unknown.
    */
   public int getDistance(int x, int y) {
      return dist[square(x, y)];
   }

   public void clearDistance(int x, int y) {
      dist[square(x, y)] = FAR_AWAY;
   }

   public void addInitial(Vector2 initial) {
      addInitial((int) Math.floor(initial.getX()), (int) Math.floor(initial.getY()));
   }

   public void addInitial(int x, int y) {
      setDist(-1, square(x, y), 0);
   }

   public Tile getClosestTarget(Tile t) {
      return getClosestTarget(t.x, t.y);

   }

   private Tile getClosestTarget(int x, int y) {
      int sq = square(x, y);
      setNeighbours(x, y);
      for (int i = 0; i < nrNeighbours; ++i) {
         int neighbourSq = neighbours[i];
         if (dist[neighbourSq] == 0) {
            return tile(neighbourSq);
         }
         if (dist[neighbourSq] == dist[sq] - 1) {
            return getClosestTarget(toX(neighbourSq), toY(neighbourSq));
         }
      }
      return null;
   }

   /**
    * Returns shortest path to row/col
    * 
    * @param x
    * @param y
    * @return
    */
   public List<Tile> getPathTo(int x, int y) {
      List<Tile> path = getPathFrom(x, y);
      Collections.reverse(path);
      return path;
   }

   /**
    * Returns shortest path from target to row/col
    * 
    * @param x
    * @param y
    * @return
    */
   public List<Tile> getPathFrom(int x, int y) {
      // Utils.assrt(walkable[x][y]);
      // sometimes this is called from a non-walkable square; in that case find some
      // walkable neighbour
      x = Utils.clamp(0, width-1, x);
      y = Utils.clamp(0, height-1, y);
      if (!walkable[x][y]) {
         setNeighbours(x, y);
         if (nrNeighbours > 0) {
            x = toX(neighbours[0]);
            y = toY(neighbours[0]);
         }
      }
      List<Tile> path = new ArrayList<Tile>();
      int sq = square(x, y);
      while (sq >= 0 && path.size() < 80) {
         Tile t = Tile.get(toX(sq), toY(sq));
         if (!Tile.isValid(t.x, t.y)) {
            break;
         }
         path.add(t);
         sq = predecessor[sq];
      }
      return path;
   }

   public List<Tile> getPathFrom(Vector2 position) {
      return getPathFrom((int) position.getX(), (int) position.getY());
   }

   /**
    * Adds one more distance layer of squares
    * 
    * @return nr added squares
    */
   public int calcDistOneMore() {
      // Log.log("calcDistOneMore");
      // exchange added/prevAdded
      int[] helpArr = prevAdded;
      prevAdded = added;
      added = helpArr;
      nrPrevAdded = nrAdded;
      nrAdded = 0;
      for (int i = 0; i < nrPrevAdded; ++i) {
         // Log.log("i = " + i + ", dist = " + dist[prevAdded[i]]);
         if (dist[prevAdded[i]] < FAR_AWAY) {
            int sq = prevAdded[i];
            int x = toX(sq);
            int y = toY(sq);
            // Log.log("setNeighbours " + x + "/" + y);
            setNeighbours(x, y);
            for (int j = 0; j < nrNeighbours; ++j) {
               int neighbourSq = neighbours[j];
               int neighbourX = toX(neighbourSq);
               int neighbourY = toY(neighbourSq);
               int newDist = dist[sq] + cost[neighbourX][neighbourY];
               // Log.log(" neighbour: " + neighbourX + "/" + neighbourY +
               // ", newDist = " + newDist);
               if (newDist < dist[neighbourSq]) {
                  setDist(sq, neighbourSq, newDist);
               }
            }
         }
      }
      return nrAdded;
   }

   public void calcDistances(int maxDist) {
      for (int i = 0; i < maxDist; ++i) {
         int nrAdded = calcDistOneMore();
         if (nrAdded == 0) {
            added = null;
            prevAdded = null;
            return;
         }
      }
   }

   public List<Tile> getCloserTiles(Tile t) {
      List<Tile> result = new ArrayList<Tile>();
      int sq = square(t.x, t.y);
      if (dist[sq] < FAR_AWAY) {
         setNeighbours(t.x, t.y);
         for (int i = 0; i < nrNeighbours; ++i) {
            int neighbourSq = neighbours[i];
            if (dist[neighbourSq] == dist[sq] - 1) {
               result.add(tile(neighbourSq));
            }
         }
      }
      return result;
   }

   private int toX(int sq) {
      return sq / height;
   }

   private int toY(int sq) {
      return sq % height;
   }

   public int square(int x, int y) {
      int result = x * height + y;
      if (result >= width * height) {
         throw new RuntimeException("Error in square, x/y = " + x + "," + y);
      }
      return result;
   }

   public int square(Tile t) {
      return square(t.x, t.y);
   }

   public Tile tile(int sq) {
      return Tile.get(toX(sq), toY(sq));
   }

   public float getMaxDist() {
      return maxDist;
   }

   private void setDist(int fromSq, int sq, int d) {
      dist[sq] = d;
      boolean alreadyAdded = false;
      for (int i = 0; !alreadyAdded && i < nrAdded; ++i) {
         alreadyAdded = added[i] == sq;
      }
      if (!alreadyAdded) {
         added[nrAdded] = sq;
         ++nrAdded;
      }
      if (d > maxDist) {
         maxDist = d;
      }
      predecessor[sq] = fromSq;
   }

   private void setNeighbours(int x, int y) {
      nrNeighbours = 0;
      if (reverseNeighbours) {
         addNeighbour(x - 1, y);
         addNeighbour(x, y - 1);
         addNeighbour(x + 1, y);
         addNeighbour(x, y + 1);
      } else {
         addNeighbour(x, y + 1);
         addNeighbour(x + 1, y);
         addNeighbour(x, y - 1);
         addNeighbour(x - 1, y);
      }
      reverseNeighbours = !reverseNeighbours;
   }

   private void addNeighbour(int x, int y) {
      if (x >= 0 && x < width && y >= 0 && y < height && walkable[x][y]) {
         neighbours[nrNeighbours] = square(x, y);
         ++nrNeighbours;
      }
   }
}
