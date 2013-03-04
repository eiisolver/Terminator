package commander;

import java.util.*;
import com.aisandbox.util.Vector2;

public class EnemyBotInfo {
   public String name;
   public Vector2 position;
   public Vector2 facingDirection;
   public int state;
   /** suspected running speed of this bot */
   public double speed;
   /** Expected path that this bot is going to take. First element is current position */
   public List<Vector2> path;

}
