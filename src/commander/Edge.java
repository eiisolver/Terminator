package commander;

public class Edge {
   public int id;
   Object object;
   public Node from;
   public Node to;
   int value;
   
   public Edge(Node from, Node to, int value) {
      this.from = from;
      this.to = to;
      this.value = value;
   }
}
