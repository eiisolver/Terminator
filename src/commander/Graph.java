package commander;

import java.util.*;

public class Graph {
   private static final int BIG_VALUE = 1000000000;
   public Node[] nodes;
   public Edge[] edges;
   private List<Node> tempNodes = new ArrayList<Node>();
   private List[] tempEdges;
   
   /**
    * Order of building a graph: first add all nodes, then call
    * allNodesAdded, then add all edges, then call allEdgesAdded.
    * @param node
    */
   public void add(Node node) {
      tempNodes.add(node);
   }
   
   public void allNodesAdded() {
      nodes = tempNodes.toArray(new Node[0]);
      tempNodes = null;
      for (int i = 0; i < nodes.length; ++i) {
         nodes[i].id = i;
      }
      tempEdges = new List[nodes.length];
      for (int i = 0; i < tempEdges.length; ++i) {
         tempEdges[i] = new ArrayList();
      }
   }
   
   public void add(Edge edge) {
      tempEdges[edge.from.id].add(edge);
   }
   
   public void allEdgesAdded() {
      int nrEdges = 0;
      for (int i = 0; i < tempEdges.length; ++i) {
         nrEdges += tempEdges[i].size();
      }
      edges = new Edge[nrEdges];
      int index = 0;
      for (int i = 0; i < tempEdges.length; ++i) {
         nodes[i].neighbours = new Edge[tempEdges[i].size()];
         for (int j = 0; j < tempEdges[i].size(); ++j) {
            Edge edge = (Edge)tempEdges[i].get(j);
            nodes[i].neighbours[j] = edge;
            edges[index] = edge;
            edges[index].id = index;
            ++index;
         }
         tempEdges[i] = null;
      }
      tempEdges = null;
   }
   
   /**
    * Floyd-Warshall calculation of all-pair shortest paths in the graph
    * @return
    */
   public int[][] allPairsShortestPath() {
      int[][] adjGraph = new int[nodes.length][nodes.length];
      for (int i = 0; i < nodes.length; ++i) {
         for (int j = 0; j < nodes.length; ++j) {
            adjGraph[i][j] = BIG_VALUE;
         }
      }
      for (int i = 0; i < nodes.length; ++i) {
         adjGraph[i][i] = 0;
      }
      for (int i = 0; i < edges.length; ++i) {
         adjGraph[edges[i].from.id][edges[i].to.id] = edges[i].value;
      }
      for (int k = 0; k < nodes.length; ++k) {
         for (int i = 0; i < nodes.length; ++i) {
            for (int j = 0; j < nodes.length; ++j) {
               int throughK = adjGraph[i][k] + adjGraph[k][j];
               if (throughK < adjGraph[i][j]) {
                  adjGraph[i][j] = throughK;
               }
            }
         }
      }
      return adjGraph;
   }
}
