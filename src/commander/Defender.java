package commander;

import com.aisandbox.cmd.info.BotInfo;
import com.aisandbox.util.Vector2;

public class Defender {
   public String name;
   public Vector2 location;
   public Vector2 facingDirection;
   public int creationTime;

   public Defender() {
   }

   public Defender(BotInfo bot) {
      name = bot.getName();
      location = bot.getPosition();
      facingDirection = bot.getFacingDirection();
   }
   
   public Defender copy() {
      Defender defender = new Defender();
      defender.name = name;
      defender.location = location;
      defender.facingDirection = facingDirection;
      return defender;
   }
   
   public String toString() {
      return name + "@" + Utils.toString(location);
   }
   
   public boolean isFlagDefender() {
      return name.equals(Defenders.flagDefenderName);
   }
}
