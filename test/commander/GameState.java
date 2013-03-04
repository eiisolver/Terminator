package commander;

import java.util.*;

import com.aisandbox.cmd.cmds.BotCommand;

public class GameState {
   int nrTicks;
   public float timePassed;
   int nrLivingBots = 0;
   int nrAttackers = 0;
   int nrDefenders = 0;
   int nrFlagCarriers = 0;
   int nrBots;
   int nrLivingEnemies;
   public List<MyBotInfo> myBots;
   public MyBotInfo enemyBots;
   public String gameInfoJson;
   public List<String> log;
   public List<BotCommand> issuedCmds;
   int[][] riskBasedCost;
   int[][] visitedByEnemy;
   int totalNrVisitedByEnemy;
   public List<AmbushPoint> ambushPoints;
   
   public void setState(MyCommander commander) {
      gameInfoJson = commander.getGameInfo().json;
      log = Log.getBuffer();
      nrTicks = commander.nrTicks;
      timePassed = commander.getGameInfo().getMatchInfo().getTimePassed();
      nrLivingBots = commander.nrLivingBots;
      nrLivingEnemies = commander.nrLivingEnemies;
      issuedCmds = new ArrayList<BotCommand>(commander.issuedCmds);
      riskBasedCost = Utils.copyArray(commander.riskBasedCost);
      visitedByEnemy = Utils.copyArray(commander.visitedByEnemy);
      totalNrVisitedByEnemy = commander.totalNrVisitedByEnemy;
      myBots = new ArrayList<MyBotInfo>();
      for (MyBotInfo bot : commander.myBots) {
         myBots.add(bot.copy());
      }
      ambushPoints = new ArrayList<AmbushPoint>(commander.ambushPoints);
   }
   
   public boolean logContains(String stringToFind) {
      for (String s : log) {
         if (s.contains(stringToFind)) {
            return true;
         }
      }
      return false;
   }
   

}
