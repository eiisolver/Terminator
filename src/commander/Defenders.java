package commander;
import java.util.*;

public class Defenders {
   private MyCommander commander;
   /**
    * One special defender has name "flagDefender" (intended to indicate a defender
    * near enemy flag), which will never be removed from the defender list
    * (there can only be 1 such defender in the list).
    */
   public static final String flagDefenderName = "flagDefender";
   /**
    * Potential defender (an enemy that we saw shooting near enemy flag, but we
    * don't know anything else about him, perhaps he is not defending at all)
    */
   private String potentialDefenderName = "potentialFlagDefender";
   private List<Defender> defenderList = new ArrayList<Defender>();
   private Map<Tile, Defender> attackedSquares = new HashMap<Tile, Defender>(); 
   
   public Defenders(MyCommander commander) {
      this.commander = commander;
   }
   
   public void add(Defender defender, boolean isFlagDefender) {
      if (isFlagDefender) {
         remove(flagDefenderName);
         Defender flagDefender = defender.copy();
         flagDefender.name = flagDefenderName;
         defenderList.add(flagDefender);
         // remove potential defenders
         remove(potentialDefenderName);
      }
      if (getDefender(defender.name) == null) {
         defenderList.add(defender);
      }
   }
   
   public void addPotentialFlagDefender(Defender defender) {
      Log.log("addPotentialFlagDefender" + defender);
      remove(potentialDefenderName);
      Defender flagDefender = defender.copy();
      flagDefender.name = potentialDefenderName;
      flagDefender.creationTime = commander.currTimeMs;
      defenderList.add(flagDefender);
   }
   
   public void remove(String name) {
      for (int i = 0; i < defenderList.size(); ++i) {
         if (defenderList.get(i).name.equals(name)) {
            defenderList.remove(i);
            return;
         }
      }
   }
   
   public Defender getDefender(String name) {
      for (int i = 0; i < defenderList.size(); ++i) {
         if (defenderList.get(i).name.equals(name)) {
            return defenderList.get(i);
         }
      }
      return null;
   }
   
   public List<Defender> getAllDefenders() {
      return defenderList;
   }
   
   public List<Defender> getDefenders() {
      // check if we have seen the potential flag defender tile, and if yes, remove it
      Defender potentialFlagDefender = getDefender(potentialDefenderName);
      if (potentialFlagDefender != null) {
         int lastSeenTime = commander.timeSinceLastSeen(Tile.get(potentialFlagDefender.location));
         if (lastSeenTime < 1000 && potentialFlagDefender.creationTime + 1100 < commander.currTimeMs) {
            remove(potentialDefenderName);
         }
      }
      List<Defender> list = new ArrayList<Defender>();
      for (Defender defender : defenderList) {
         if (isActiveDefender(defender)) {
            list.add(defender);
         }
      }
      Log.log("Defenders: " + Utils.toString((List)list));
      return list;
   }
   
   private boolean isActiveDefender(Defender defender) {
      if (defender.name.equals(flagDefenderName)) {
         int lastSeenTime = commander.timeSinceLastSeen(Tile.get(defender.location));
         //System.out.println("Last seen time to flag defender: " + lastSeenTime);
         if (lastSeenTime < 12000) {
            return false;
         }
      }
      return true;
   }
   
   public void calcAttackedSquares() {
      attackedSquares.clear();
      for (Defender defender : defenderList) {
         if (isActiveDefender(defender)) {
            for (Tile t : Tile.get(defender.location).attackedTiles) {
               attackedSquares.put(t, defender);
            }
         }
      }
   }
   
   /**
    * Returns the defender that can (in principle) attack the given tile.
    * @param t
    * @return null if there is no such defender
    */
   public Defender getDefender(Tile t) {
      return attackedSquares.get(t);
   }
}
