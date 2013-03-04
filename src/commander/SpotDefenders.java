package commander;

import java.util.*;

import com.aisandbox.util.Vector2;

/**
 * Used by our bots to select a good spot from which to defend our flag
 * (or another point, called the spot).
 * @author louis
 *
 */
public class SpotDefenders {
   public Vector2 locationToDefend; // as spotToDefend, the exact location
   public Tile spotToDefend;
   public Tile defendLocation;
   public Set<String> defenders = new HashSet<String>();
   private boolean newCommandsNeeded;
   private Set<String> newDefenders = new HashSet<String>();
   private List<MyBotInfo> defenderList = new ArrayList<MyBotInfo>();

   public void update(Vector2 locationToDefend, Tile defendLocation) {
      spotToDefend = Tile.get(locationToDefend);
      this.locationToDefend = locationToDefend;
      newCommandsNeeded = false;
      if (spotToDefend != this.spotToDefend) {
         this.spotToDefend = spotToDefend;
         newCommandsNeeded = true;
         Log.log("SpotDefenders: new spot to defend: " + spotToDefend);
      }
      if ((defenders.isEmpty() || newCommandsNeeded) && defendLocation != this.defendLocation) {
         Log.log("SpotDefenders: new defend location: " + defendLocation);
         this.defendLocation = defendLocation;
      }
      newDefenders.clear();
      defenderList.clear();
   }
   
   public Tile getDefendLocation() {
      return defendLocation;
   }
   
   public void addActiveDefender(MyBotInfo bot) {
      Log.log("SpotDefenders: addActiveDefender for " + defendLocation + ": " + bot.name);
      newDefenders.add(bot.name);
      defenderList.add(bot);
   }
   
   public boolean isNewCommandsNeeded() {
      boolean result = newCommandsNeeded || !newDefenders.equals(defenders);
      defenders.clear();
      defenders.addAll(newDefenders);
      return result;
   }
   
   public List<MyBotInfo> getDefenders() {
      return defenderList;
   }
}
