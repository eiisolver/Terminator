package commander;

import java.util.*;

import com.aisandbox.cmd.info.BotInfo;
import com.aisandbox.util.Vector2;

public class MyBotInfo {
   public int rnd = new Random().nextInt(100);
	public String name;
	public BotInfo bot;
	public float distToAttackGoal;
	public float distToMyFlag;
	public float distToFlagCarrierHelperTarget;
	public MyBotRole proposedRole;
	/** The role that was assigned to this bot at last issued command */
	public MyBotRole realRole;
	/** Name of the attacked enemy (only valid if role is attacking enemy) */
	public String attackedEnemy;
	/** Target goal (valid in some states) */
	public Vector2 target;
	/** waypoints to target */
	public List<Vector2> targetPath;
	/** Bot position when target was set */
	public Vector2 pathOrigin;
	public String mission = "";
	public float timeOfLastOrder = -1000;
	/** Location of the bot at previous tick (only for enemy bots) */
	public Vector2 previousPosition;
	/** Time stamp when we last saw this enemy bot */
	public float previousSeenLastTime;
	public InterceptInfo flagInterceptInfo = new InterceptInfo();
	public GDistance distance;
	
	public MyBotInfo copy() {
	   MyBotInfo b = new MyBotInfo();
	   b.name = name;
	   b.proposedRole = proposedRole;
	   b.realRole = realRole;
	   b.target = target;
	   b.mission = mission;
	   b.timeOfLastOrder = timeOfLastOrder;
	   b.targetPath = new ArrayList<Vector2>(targetPath);
	   b.pathOrigin = pathOrigin;
	   return b;
	}
	public boolean hasFlag() {
		return bot.hasFlag();
	}
	
	public boolean isDead() {
	   return bot.getState() == BotInfo.STATE_DEAD;
	}
	
	public void enableNewCommand() {
	   timeOfLastOrder = -1000;
	}
	
	public int getDistance(Vector2 position) {
	   return distance.getDistance(Tile.get(position).node);
	}
	
	public List<Tile> getPathTo(Vector2 position) {
	   List<Edge> edgePath = distance.getPathTo(Tile.get(position).node);
	   List<Tile> path = new ArrayList<Tile>(edgePath.size());
	   for (Edge edge : edgePath) {
	      path.add((Tile)edge.to.object);
	   }
	   return path;
	}
	
	public static Comparator<MyBotInfo> DIST_TO_ENEMY_FLAG_COMPARATOR = new Comparator<MyBotInfo>() {

		@Override
		public int compare(MyBotInfo bot1, MyBotInfo bot2) {
			return (int)Math.signum(bot1.distToAttackGoal - bot2.distToAttackGoal);
		}
		
	};
   public static Comparator<MyBotInfo> DIST_TO_FLAG_SCORE_COMPARATOR = new Comparator<MyBotInfo>() {

      @Override
      public int compare(MyBotInfo bot1, MyBotInfo bot2) {
         return (int)Math.signum(bot1.distToFlagCarrierHelperTarget - bot2.distToFlagCarrierHelperTarget);
      }
      
   };
	public static Comparator<MyBotInfo> DIST_TO_MY_FLAG_COMPARATOR = new Comparator<MyBotInfo>() {

		@Override
		public int compare(MyBotInfo bot1, MyBotInfo bot2) {
			return (int)Math.signum(bot1.distToMyFlag - bot2.distToMyFlag);
		}
		
	};
   public static Comparator<MyBotInfo> DIST_TO_FLAG_INTERCEPTION_COMPARATOR = new Comparator<MyBotInfo>() {

      @Override
      public int compare(MyBotInfo bot1, MyBotInfo bot2) {
         return (int)Math.signum(bot1.flagInterceptInfo.distToImpact - bot2.flagInterceptInfo.distToImpact);
      }
      
   };
}
