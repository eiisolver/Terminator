package commander;

import java.util.*;
import javax.swing.*;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

/**
 * Wrapper class for the AI sandbox client.
 * 
 * @author Matthias F. Brandstetter
 */
public class MySandboxClientWrapper implements GameStateSaver {
   public static final boolean DEBUG = false;
   MyCommander commander;
   List<GameState> states = new ArrayList<GameState>();
   GameStatePanel gameStatePanel = new GameStatePanel();
   boolean paused = false;
   int currMove = 0;
   float speedFactor = 1;
   boolean ignoreInput = false;

   @Override
   public synchronized void saveGameState(MyCommander commander) {
      this.commander = new MyCommander();
      GameState state = new GameState();
      state.setState(commander);
      states.add(state);
      gameStatePanel.setCommander(commander);
   }

   public synchronized int getNrStates() {
      return states.size();
   }

   public synchronized GameState getState(int index) {
      return states.get(index);
   }

   private void setMoveNr(int moveNr) {
      if (states.isEmpty()) {
         return;
      }
      currMove = moveNr;
      if (currMove < 0) {
         currMove = 0;
      } else if (currMove >= getNrStates()) {
         currMove = getNrStates() - 1;
      }
      gameStatePanel.setState(getState(currMove));
      gameStatePanel.repaint();
   }
   
   private int findInLog(String stringToFind) {
      int nrStates = getNrStates();
      for (int i = currMove + 1; i < nrStates; ++i) {
         GameState state = getState(i);
         if (state.logContains(stringToFind)) {
            return i;
         }
      }
      return -1;
   }
   
   private int getMsToNextState() {
      int nrStates = getNrStates();
      int ms = 500;
      if (currMove < nrStates-1) {
         ms = (int)(1000*(getState(currMove+1).timePassed - getState(currMove).timePassed));
      }
      return ms;
   }

   public void startUiThread() {
      Thread t = new Thread(new Runnable() {

         @Override
         public void run() {
            JFrame frame = new JFrame();
            frame.setSize(600, 600);
            frame.add(gameStatePanel);
            frame.setVisible(true);
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new MyKeyDispatcher());
            showGame();
         }

      });
      t.start();
   }

   public void showGame() {
      try {
         int currNrStates = 0;
         int sleepTime = 500;
         while (true) {
            long startTime = System.currentTimeMillis();
            Thread.sleep(sleepTime);
            int nrStates = getNrStates();
            if (!paused) {
               if (nrStates > currNrStates) {
                  currNrStates = nrStates;
                  setMoveNr(currNrStates-1);
                  sleepTime = 500;
               } else if (currMove < getNrStates() - 1) {
                  setMoveNr(currMove + 1);
                  int msToNext = getMsToNextState();
                  sleepTime = (int)(speedFactor * msToNext);
               }
            }
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public void keyPressed(KeyEvent event) {
      if (ignoreInput) {
         return;
      }
      char c = event.getKeyChar();
      if (c == ' ') {
         paused = !paused;
      } else if (c == '<') {
         setMoveNr(currMove - 1000);
      } else if (c == '>') {
         setMoveNr(currMove + 1000);
      } else if (c == '-') {
         setMoveNr(currMove - 100);
      } else if (c == '+') {
         setMoveNr(currMove + 100);
      } else if (c >= '0' && c <= '9') {
         speedFactor = 0.4f * (1 + c - '0');
      } else if (c == 'r') {
         gameStatePanel.setVisualisation(GameStatePanel.Visualisation.RISK);
      } else if (c == 'n') {
         gameStatePanel.setVisualisation(GameStatePanel.Visualisation.NONE);
      } else if (c == 'v') {
         gameStatePanel.setVisualisation(GameStatePanel.Visualisation.VISITED_BY_ENEMY);
      } else if (c == 'f') {
         ignoreInput = true;
         String stringToFind = JOptionPane.showInputDialog(null, "Find in log");
         if (stringToFind != null) {
            int move = findInLog(stringToFind);
            if (move > 0) {
               setMoveNr(move);
            }
         }
         ignoreInput = false;
      } else {
         switch (event.getKeyCode()) {
         case KeyEvent.VK_UP:
            setMoveNr(currMove + 20);
            break;
         case KeyEvent.VK_RIGHT:
            setMoveNr(currMove + 1);
            break;
         case KeyEvent.VK_DOWN:
            setMoveNr(currMove - 20);
            break;
         case KeyEvent.VK_LEFT:
            setMoveNr(currMove - 1);
            break;
         case KeyEvent.VK_HOME:
            setMoveNr(0);
            break;
         case KeyEvent.VK_END:
            setMoveNr(getNrStates() - 1);
            break;
         default:
         }
      }
   }

   private class MyKeyDispatcher implements KeyEventDispatcher {
      @Override
      public boolean dispatchKeyEvent(KeyEvent e) {
         if (e.getID() == KeyEvent.KEY_PRESSED) {
            keyPressed(e);
         } else if (e.getID() == KeyEvent.KEY_RELEASED) {
         } else if (e.getID() == KeyEvent.KEY_TYPED) {
         }
         return false;
      }
   }

   /**
    * Program entry point. Program can either be called w/o arguments or with
    * <server> <port> as arguments.
    */
   public static void main(String[] args) {
      MySandboxClientWrapper w = new MySandboxClientWrapper();
      w.startUiThread();
      MyCommander.stateSaver = w;
      Utils.DEBUG = DEBUG;
      Log.buffering = true;
      SandboxClientWrapper.main(args);
   }
}
