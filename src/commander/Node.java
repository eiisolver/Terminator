package commander;

import java.util.Random;

/**
 * Node of a graph.
 * @author louis
 *
 */
public class Node {
   public int id;
   public Object object;

   public Edge[] neighbours;
   
   public Node(Object object) {
      this.object = object;
   }
   
   /**
    * Randomly shuffles order of the neighbours
    */
   public void shuffleNeighbours() {
      Random rnd = new Random();
      for (int i = 0; i < neighbours.length; ++i) {
         int a = rnd.nextInt(neighbours.length);
         int b = rnd.nextInt(neighbours.length);
         if (a != b) {
            Edge h = neighbours[a];
            neighbours[a] = neighbours[b];
            neighbours[b] = h;
         }
      }
   }
}
