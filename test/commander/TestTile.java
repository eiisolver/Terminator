package commander;

import com.aisandbox.util.Vector2;

public class TestTile {

   /**
    * @param args
    */
   public static void main(String[] args) {
      int w = 50;
      int h = 30;
      int fireDist = 10;
      Tile.init(w, h, fireDist);
      for (int i = 0; i < w; ++i) {
         for (int j = 0; j < h; ++j) {
            Tile t = Tile.get(i, j);
            t.isShootable = true;
            t.isWalkable = true;
         }
      }
      for (int i = 0; i < w; ++i) {
         for (int j = 0; j < h; ++j) {
            Tile t = Tile.get(i, j);
            t.calcAttackedTiles(fireDist, true);
         }
      }
      Tile t = Tile.get(6, 6);
      System.out.println("All attackable tiles");
      for (int i = 0; i < t.attackedTiles.length; ++i) {
         System.out.println(t.attackedTiles[i] + ", angle: " + t.attackAngles[i]);
      }
      Vector2 direction = new Vector2(1, 0);
      double angle = Utils.getFacingAngle(direction);
      TileVisitor visitor = new TileVisitor() {

         @Override
         public boolean visit(Tile t) {
            System.out.println("visible: " + t);
            return true;
         }
         
      };
      System.out.println("Tiles visible from direction: " + direction);
      t.visitAttackedTiles(angle, 0.5, visitor);
      direction = new Vector2(-1, 0);
      angle = Utils.getFacingAngle(direction);
      System.out.println("Tiles visible from direction: " + direction);
      t.visitAttackedTiles(angle, 0.5, visitor);
   }

}
