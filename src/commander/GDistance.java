package commander;

import java.util.*;

/**
 * Contains distances to interesting points
 * 
 * @author louis
 * 
 */
public class GDistance {
   public static final int FAR_AWAY = 100000000;
   /** Will contain distance to every node */
   public int[] dist;
   /** Predecessor of every square */
   public Edge[] predecessor;
   private int[] prevAdded;
   private int nrPrevAdded = 0;
   private int[] added;
   public int nrAdded = 0;
   public int maxDist = 0;
   private Graph graph;

   public GDistance(Graph graph) {
      this.graph = graph;
      dist = new int[graph.nodes.length];
      predecessor = new Edge[dist.length];
      prevAdded = new int[dist.length];
      added = new int[prevAdded.length];
      clear();
   }

   public void clear() {
      Arrays.fill(dist, FAR_AWAY);
      Arrays.fill(predecessor, null);
      nrAdded = 0;
      nrPrevAdded = 0;
      maxDist = 0;
   }

   /**
    * Return distance, FAR_AWAY if unknown.
    */
   public int getDistance(Node n) {
      return dist[n.id];
   }

   public void clearDistance(Node n) {
      dist[n.id] = FAR_AWAY;
   }

   public void addInitial(Node n) {
      setDist(null, n.id, 0);
   }

   /*private Tile getClosestTarget(Node n) {
      int sq = n.id;
      for (int i = 0; i < n.neighbours.length; ++i) {
         int neighbourSq = n.neighbours[i].to.id;
         if (dist[neighbourSq] == 0) {
            return tile(neighbourSq);
         }
         if (dist[neighbourSq] == dist[sq] - 1) {
            return getClosestTarget(toX(neighbourSq), toY(neighbourSq));
         }
      }
      return null;
   }*/

   /**
    * Returns shortest path to row/col
    * 
    * @param x
    * @param y
    * @return
    */
   public List<Edge> getPathTo(Node dest) {
      List<Edge> path = getPathFrom(dest);
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
   public List<Edge> getPathFrom(Node n) {
      List<Edge> path = new ArrayList<Edge>();
      for (Edge edge = predecessor[n.id]; edge != null; edge = predecessor[n.id]) {
         path.add(edge);
         n = edge.from;
      }
      return path;
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
            Node n = graph.nodes[sq];
            for (int j = 0; j < n.neighbours.length; ++j) {
               Edge edge = n.neighbours[j];
               int neighbourSq = edge.to.id;
               int newDist = dist[sq] + edge.value;
               // Log.log(" neighbour: " + neighbourX + "/" + neighbourY +
               // ", newDist = " + newDist);
               if (newDist < dist[neighbourSq]) {
                  setDist(edge, neighbourSq, newDist);
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


   public float getMaxDist() {
      return maxDist;
   }

   private void setDist(Edge edge, int sq, int d) {
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
      predecessor[sq] = edge;
   }
}
