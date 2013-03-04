package commander;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

import javax.swing.*;

import com.aisandbox.cmd.cmds.BotCommand;
import com.aisandbox.cmd.info.*;
import com.aisandbox.util.Area;
import com.aisandbox.util.Vector2;

public class GameStatePanel extends JPanel {
   public enum Visualisation {
      NONE, RISK, VISITED_BY_ENEMY
   }
   private Visualisation visualisation = Visualisation.RISK;
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private static final int defaultBgColor = 0xffffff;
   JPanel boardPanel;
   JTextArea textArea;
   JTextArea statusArea;

   int PIXELS_PER_M = 15;
   GameState state;
   MyCommander commander;
   GameInfo gameInfo;
   LevelInfo levelInfo;
   private String myTeam;
   private String enemyTeam;
   /** width of the play field in meters */
   int width;
   int height;
   
   public GameStatePanel() {
      setLayout(new BorderLayout());
      boardPanel = new BoardPanel();
      add(boardPanel, BorderLayout.CENTER);
      textArea = new JTextArea(15, 80);
      JScrollPane scrollPane = new JScrollPane(textArea); 
      textArea.setEditable(false);
      add(scrollPane, BorderLayout.SOUTH);
      statusArea = new JTextArea(60, 52);
      JScrollPane scrollPane2 = new JScrollPane(statusArea); 
      statusArea.setEditable(false);
      add(scrollPane2, BorderLayout.EAST);
   }
   
   public void setCommander(MyCommander commander) {
      this.commander = commander;
   }
   
   public void setState(GameState state) {
      this.state = state;
      gameInfo = new GameInfo(state.gameInfoJson);
      for (MyBotInfo bot : state.myBots) {
         bot.bot = gameInfo.getBotInfo(bot.name);
      }
      levelInfo = commander.getLevelInfo();
      width = commander.width;
      height = commander.height;
      myTeam = gameInfo.getTeam();
      enemyTeam = gameInfo.getEnemyTeam();
      updateText();
      updateStatus();
      repaint();
   }
   
   public void setVisualisation(Visualisation visualisation) {
      this.visualisation = visualisation;
      repaint();
   }
   
   public void updateText() {
      StringBuilder buf = new StringBuilder();
      for (String s : state.log) { 
         buf.append(s);
         buf.append("\n");
      }
      textArea.setText(buf.toString());
      textArea.invalidate();
   }
   
   public void updateStatus() {
      StringBuilder buf = new StringBuilder();
      buf.append("Tick: " + state.nrTicks + ", time: " + String.format("%2.3f", gameInfo.getMatchInfo().getTimePassed())+ "\n");
      buf.append("MyNrLiving: " + state.nrLivingBots + "\n");
      buf.append("NrLivingEnemies: " + state.nrLivingEnemies + "\n");
      buf.append("Score: " + gameInfo.getMatchInfo().getScores(myTeam) + "-" + gameInfo.getMatchInfo().getScores(enemyTeam));
      buf.append("\n");
      List<String> botNames = new ArrayList<String>(gameInfo.getBots().keySet());
      Collections.sort(botNames);
      buf.append("My bots:\n");
      for (String name: botNames) {
         BotInfo bot = gameInfo.getBots().get(name);
         if (bot.getTeam().equals(myTeam) && bot.getHealth() > 0) {
            MyBotInfo myBot = find(name);
            buf.append(myBotInfoToString(myBot));
            buf.append("\n");
         }
      }
      buf.append("Enemy bots:\n");
      for (String name: botNames) {
         BotInfo bot = gameInfo.getBots().get(name);
         if (!bot.getTeam().equals(myTeam) && bot.getHealth() > 0) {
            String pos = Utils.toString(bot.getPosition());
            buf.append(bot.getName() + " " + pos + ", "+ Utils.stateToString(bot.getState()) + ", seenLast: " + bot.getSeenLast());
            buf.append("\n");
         }
      }
      buf.append("Combat events:\n");
      for (MatchCombatEvent event : gameInfo.getMatchInfo().getCombatEvents()) {
         buf.append("Combat event " + Utils.combatEventToString(event.getType()) + ", subject: " + event.getSubject() + ", inst: " + event.getInstigator());
         buf.append("\n");
      }
      buf.append("Issued commands:\n");
      for (BotCommand cmd : state.issuedCmds) {
         buf.append(cmd.getBot() + ": " + cmd.getCmdClass() + " " + cmd.getDescription() + "\n");
      }
      statusArea.setText(buf.toString());
      statusArea.invalidate();
   }
   
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (state == null) {
         return;
      }
   }
   
   private List<String> getSortedBotNames(String team) {
      List<String> list = new ArrayList<String>();
      for (String name : gameInfo.getBots().keySet()) {
         BotInfo bot = gameInfo.getBotInfo(name);
         if (team == null || team.equals(bot.getTeam())) {
            list.add(name);
         }
      }
      Collections.sort(list);
      return list;
   }
   
   private MyBotInfo find(String name) {
      for (MyBotInfo bot : state.myBots) {
         if (bot.name.equals(name)) {
            return bot;
         }
      }
      return null;
   }
   
   private String myBotInfoToString(MyBotInfo bot) {
      Vector2 pos = bot.bot.getPosition();
      Vector2 tgt = bot.target;
      return String.format("%-6s %s %s/%s %s pos: (%2.1f,%2.1f), tgt: (%2.1f,%2.1f)",
            bot.name, bot.proposedRole, bot.realRole, bot.mission, Utils.stateToString(bot.bot.getState()),
            pos.getX(), pos.getY(), tgt.getX(), tgt.getY());
   }
   
   public class BoardPanel extends JPanel {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      Font textFont = new Font("Arial", Font.BOLD, 15); 
      Font bigTextFont = new Font("Arial", Font.BOLD, 20); 

      public BoardPanel() {
         addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent event) {
               System.out.printf("clicked on %2.1f, %2.1f\n", pixelXToTile(event.getPoint().x), pixelYToTile(event.getPoint().y));
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
               // TODO Auto-generated method stub
               
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
               // TODO Auto-generated method stub
               
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
               // TODO Auto-generated method stub
               
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
               // TODO Auto-generated method stub
               
            }
            
         });
      }
      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (state == null) {
            return;
         }
         Graphics2D g2 = (Graphics2D) g;
         g2.setFont(textFont);  
         visualize(g2);
         showLevelInfoSpots(g2);
         paintGrid(g2);
         for (AmbushPoint p : state.ambushPoints) {
            showAmbushPoint(g2, p);
         }
         for (MyBotInfo bot : state.myBots) {
            if (!bot.isDead() && bot.bot.getState() != BotInfo.STATE_DEFENDING && bot.bot.getState() != BotInfo.STATE_IDLE) {
               drawPath(g2, bot.pathOrigin, bot.targetPath, Color.cyan);
            }
         }
         for (BotInfo bot : gameInfo.getBots().values()) {
            showBot(g2, bot);
         }
         g2.setFont(bigTextFont);  
         for (FlagInfo flag : gameInfo.getFlags().values()) {
            showFlag(g2, flag);
         }
      }
      
      public void showBot(Graphics2D g2, BotInfo bot) {
         if (bot.getHealth() <= 0) {
            return;
         }
         if (bot.getSeenLast() > 5 && bot.getState() != BotInfo.STATE_DEFENDING) {
            return;
         }
         Vector2 pos = null;
         try {
            pos = bot.getPosition();
         } catch (Exception ex) {
         }
         if (pos == null) {
            return;
         }
         Color stateCol = stateToColor(bot.getState());
         g2.setColor(stateCol);
         int x = toX(pos);
         int y = toY(pos);
         int circleWidth = 7;
         g2.fillOval(x-circleWidth/2, y-circleWidth/2, circleWidth, circleWidth);
         g2.setColor(Color.black);
         circleWidth = 8;
         g2.drawOval(x-circleWidth/2, y-circleWidth/2, circleWidth, circleWidth);
         String shortName = bot.getName().startsWith("Red") ? bot.getName().substring(3):bot.getName().substring(4);
         Color col = bot.getName().startsWith("Red") ? Color.red : Color.blue;
         g2.setColor(col);
         drawString(g2, shortName, x, y-15);
         double facingAngle = Utils.getFacingAngle(bot.getFacingDirection());
         double fov = levelInfo.getFovAngle(bot.getState());
         Vector2 dir1 = Utils.facingAngleToVector2(facingAngle + fov/2);
         Vector2 p1 = pos.add(dir1.scale(levelInfo.getFiringDistance()));
         g2.drawLine(x, y, toX(p1), toY(p1));
         Vector2 dir2 = Utils.facingAngleToVector2(facingAngle - fov/2);
         Vector2 p2 = pos.add(dir2.scale(levelInfo.getFiringDistance()));
         g2.drawLine(x, y, toX(p2), toY(p2));
         if (fov > 0.01) {
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 40));
            int w = (int)(levelInfo.getFiringDistance()*PIXELS_PER_M);
            g2.fillArc(x-w, y-w, 2*w, 2*w, 
               (int)(-180*(facingAngle + fov/2)/Math.PI), (int)(180*fov/Math.PI));
         }
      }
      
      public void showFlag(Graphics2D g2, FlagInfo flag) {
         Vector2 pos = flag.getPosition();
         g2.setColor(Color.black);
         drawString(g2, "F", toX(pos)+2, toY(pos)+2);
         g2.setColor(nameToColor(flag.getName()));
         drawString(g2, "F", toX(pos), toY(pos));
      }
      
      private void showLevelInfoSpots(Graphics2D g2) {
         for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
               Tile t = Tile.get(i, j);
               if (!t.isShootable) {
                  fillTile(g2, i, j, Color.black);
               } else if (!t.isWalkable) {
                  fillTile(g2, i, j, Color.gray);
               }
            }
         }
         for (String name : levelInfo.getFlagSpawnLocations().keySet()) {
            Vector2 pos = levelInfo.getFlagSpawnLocations().get(name);
            Color col = nameToColor(name);
            col = col.brighter().brighter();
            fillTile(g2, pos, col);
         }
         for (String name : levelInfo.getFlagScoreLocations().keySet()) {
            Vector2 pos = levelInfo.getFlagScoreLocations().get(name);
            Color col = nameToColor(name);
            col = col.brighter().brighter();
            fillTile(g2, pos, col);
         }
         for (String name : levelInfo.getBotSpawnAreas().keySet()) {
            Area area = levelInfo.getBotSpawnAreas().get(name);
            Color col = nameToColor(name);
            col = col.brighter().brighter();
            drawArea(g2, area, col);
         }
      }
      
      private void visualize(Graphics2D g2) {
         if (visualisation == Visualisation.NONE) {
            colorize(g2, state.riskBasedCost, noneColorer);
         } else if (visualisation == Visualisation.RISK) {
            colorize(g2, state.riskBasedCost, riskColorer);
         } else if (visualisation == Visualisation.VISITED_BY_ENEMY) {
            double avgVisitedPerTile = ((double)state.totalNrVisitedByEnemy)/commander.nrWalkableTiles;
            int[] limits = new int[] {
                  (int)avgVisitedPerTile, (int)(1.5*avgVisitedPerTile), (int)(3*avgVisitedPerTile), state.totalNrVisitedByEnemy
            };
            visitedByEnemyColorer.limits = limits;
            colorize(g2, state.visitedByEnemy, visitedByEnemyColorer);
         }
      }
      
      private void colorize(Graphics2D g2, int[][] c, Colorer colorer) {
         for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
               if (Tile.get(i, j).isWalkable) {
                  fillTile(g2, i, j, colorer.color(c[i][j]));
               }
            }
         }
      }
      
      private void showAmbushPoint(Graphics2D g2, AmbushPoint p) {
         g2.setColor(Color.black);
         Vector2 v = p.tile.toVector2();
         Vector2 toV = v.add(p.bestFacingDirection);
         g2.fillOval(toX(v.getX()) - 2, toY(v.getY())-2, 4, 4);
         g2.drawLine(toX(v.getX()), toY(v.getY()), toX(toV), toY(toV));
      }
      
      private void paintGrid(Graphics2D g2) {
         g2.setColor(Color.black);
         g2.setStroke(new BasicStroke(1));
         int y0 = toY(0);
         int y1 = toY(height);
         for (int i = 0; i <= width; ++i) {
            int x = toX(i);
            g2.drawLine(x, y0, x, y1);
         }
         int x0 = toX(0);
         int x1 = toX(width);
         for (int i = 0; i <= height; ++i) {
            int y = toY(i);
            g2.drawLine(x0, y, x1, y);
         }
      }
      
      private void fillTile(Graphics2D g2, Vector2 pos, Color c) {
         fillTile(g2, (int)pos.getX(), (int)pos.getY(), c);
      }
      
      private void fillTile(Graphics2D g2, int x, int y, Color c) {
         g2.setColor(c);
         g2.fillRect(toX(x), toY(y), PIXELS_PER_M, PIXELS_PER_M);
      }
      
      private void drawPath(Graphics2D g2, Vector2 origin, List<Vector2> path, Color c) {
         g2.setColor(c);
         g2.setStroke(new BasicStroke(2));
         Vector2 prev = origin;
         for (int i = 0; i < path.size(); ++i) {
            Vector2 to = path.get(i);
            g2.drawLine(toX(prev.getX()), toY(prev.getY()), toX(to.getX()), toY(to.getY()));
            prev = to;
         }
      }
      
      private void drawString(Graphics2D g2, String s, int x, int y) {
         FontMetrics textMetrics = g2.getFontMetrics(textFont);  
         int centeredX = x - (textMetrics.stringWidth(s)/2);  
         int centeredY = y + (textMetrics.getHeight()/2);  
         g2.drawString(s, centeredX, centeredY);
      }
      
      private Color nameToColor(String name) {
         if (name.startsWith("Red")) {
            return Color.red;
         }
         return Color.blue;
      }
      
      private void drawArea(Graphics2D g2, Area area, Color c) {
         g2.setColor(c);
         g2.drawLine(toX(area.getMin().getX()), toY(area.getMin().getY()), toX(area.getMax().getX()), toY(area.getMax().getY()));
         g2.drawLine(toX(area.getMax().getX()), toY(area.getMin().getY()), toX(area.getMin().getX()), toY(area.getMax().getY()));
      }
      
      private Color stateToColor(int state) {
         switch (state) {
         case BotInfo.STATE_ATTACKING: return Color.orange;
         case BotInfo.STATE_CHARGING: return Color.cyan;
         case BotInfo.STATE_DEFENDING: return Color.pink;
         case BotInfo.STATE_TAKING_ORDERS: return Color.yellow;
         case BotInfo.STATE_IDLE: return Color.green;
         case BotInfo.STATE_HOLDING: return Color.yellow;
         case BotInfo.STATE_SHOOTING: return Color.red;
         default:
            return Color.darkGray;
         }
      }
      
      private int MARGIN = 2;
      private int toX(int x) {
         return MARGIN+x*PIXELS_PER_M;
      }
      private int toX(float x) {
         return (int)(MARGIN+x*PIXELS_PER_M);
      }
      
      private int toY(int y) {
         return toX(y);
      }
      
      private int toY(float y) {
         return toX(y);
      }
      
      private int toX(Vector2 v) {
         return toX(v.getX());
      }
      
      private int toY(Vector2 v) {
         return toY(v.getY());
      }
      
      private double pixelXToTile(int x) {
         return (x-MARGIN)/(double)PIXELS_PER_M;
      }
      private double pixelYToTile(int y) {
         return pixelXToTile(y);
      }
      
   }
   
   private static class Colorer {
      int[] colors;
      public int[] limits;
      private Color[] cols;
      
      public Colorer(int[] colors, int[] limits) {
         this.colors = colors;
         this.limits = limits;
         init();
      }
      
      public void init() {
         if (cols == null) {
            cols = new Color[colors.length];
            for (int i = 0; i < cols.length; ++i) {
               cols[i] = new Color(colors[i]);
            }
         }
      }
      
      public Color color(int val) {
         for (int i = 0; i < limits.length-1; ++i) {
            if (val <= limits[i]) {
               return cols[i];
            }
         }
         return cols[cols.length-1];
      }
   }
   
   private static final Colorer noneColorer = new Colorer(
         new int[] {defaultBgColor}, new int[] {10000});
   
   static int[] gradeColors = new int[] {
         defaultBgColor, 0x00ff00, 0xffff00, Color.orange.getRGB(),
      };
   private static final Colorer riskColorer = new Colorer(gradeColors,
         new int[] {
               299, 400, 500, 600,
         });
   
   
   private final Colorer visitedByEnemyColorer = new Colorer(gradeColors, null);
   
}
