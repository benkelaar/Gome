//#condition BT || IGS
package com.indigonauts.gome.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.ui.GameController;

public abstract class MultiplayerConnector extends Thread {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("MultiplayerConnector");
  //#endif
  protected MultiplayerCallback callback;

  protected Challenge currentChallenge;

  protected DataInputStream input;
  protected DataOutputStream output;
  private boolean noneErrorDisconnect = false;

  protected static final byte CHALLENGE = 0x04;

  private static final byte DECLINE = 0x05;

  private static final byte PLAY_MOVE = 0x06;

  private static final byte SEND_MESSAGE = 0x07;
  private static final byte MARK_STONE = 0x08;

  private static final byte DONE_WITH_COUNTING = 0x09;

  private static final byte SET_KOMI = 0x10;

  private static final byte RESIGN = 0x11;

  private static final byte SET_HANDICAP = 0x12;

  private static final byte RESET_DEAD_STONE = 0x13;

  private static final byte MOVE_EVENT = 0x43;

  protected static final byte MESSAGE_EVENT = 0x46;

  protected static final byte GAME_EVENT = 0x47;

  private static final byte TIME_EVENT = 0x48;

  private static final byte TIMES_UP_EVENT = 0x49;

  private static final byte END_GAME_EVENT = 0x50;

  protected static final byte MARK_STONE_EVENT = 0x51;

  private static final byte RESTORE_GAME_FOR_COUNING_EVENT = 0x52;

  private static final byte GAME_IS_DONE_EVENT = 0x53;

  private static final byte HANDICAP_EVENT = 0x54;

  protected static final byte OPP_WANT_KOMI_EVENT = 0x55;

  protected static final byte SET_KOMI_EVENT = 0x56;

  private static final byte IGS_RESIGNED_EVENT = 0x58;

  protected static final byte SCORE_EVENT = 0x59;

  private static final byte LOST_MESSAGE_EVENT = 0x60;
  private static final byte CHALLENGE_EVENT = 0x61;

  public MultiplayerConnector(MultiplayerCallback callback) {
    this.callback = callback;
  }

  public void challenge(Challenge challenge) throws IOException {
    //#if DEBUG
    log.debug("send challenge");
    //#endif
    output.writeByte(CHALLENGE);
    challenge.marshall(output);
    output.flush();
  }

  public abstract void acceptChallenge(Challenge challenge) throws IOException;

  public void decline(String nick) throws IOException {
    output.writeByte(DECLINE);
    output.writeUTF(nick);
    output.flush();
  }

  public void playMove(Move move) throws IOException {
    //#if DEBUG
    log.debug("playMove " + move);
    //#endif
    output.writeByte(PLAY_MOVE);
    output.writeInt(move.nb);
    output.writeByte(move.color);
    output.writeByte(move.x);
    output.writeByte(move.y);
    output.flush();

  }

  public void sendMessage(String nickToSend, String message) throws IOException {
    output.writeByte(SEND_MESSAGE);
    output.writeUTF(nickToSend);
    output.writeUTF(message);
    output.flush();
  }

  public void removeDeadStone(byte posX, byte posY) throws IOException {
    //#if DEBUG
    log.debug("Remove dead stone");
    //#endif
    output.writeByte(MARK_STONE);
    output.writeByte(posX);
    output.writeByte(posY);
    output.flush();
  }

  // scores for P2P
  public void doneWithTheCounting(int whiteScore, int blackScore) throws IOException {
    output.writeByte(DONE_WITH_COUNTING);
    output.flush();
  }

  public void setKomi(byte k) throws IOException {
    output.writeByte(SET_KOMI);
    output.writeByte(k);
    output.flush();
  }

  public void resign() throws IOException {
    output.writeByte(RESIGN);
    output.flush();
  }

  public void setHandicap(byte h) throws IOException {
    output.writeByte(SET_HANDICAP);
    output.writeByte(h);
    output.flush();
  }

  public void resetDeadStone() throws IOException {
    output.writeByte(RESET_DEAD_STONE);
    output.flush();
  }

  /**
   * Handle an event
   * @param event
   * @return true if the event is generic and has been treated
   * @throws IOException 
   */
  protected boolean handleEvent(byte event) throws IOException {
    switch (event) {
    case PLAY_MOVE: // for symetric conversations
    case MOVE_EVENT:
      Move move = Move.unmarshal(input);
      //#if DEBUG
      log.debug("Move event " + move);
      //#endif
      callback.moveEvent(move);
      break;

    case CHALLENGE: // for symetric conversations
    case CHALLENGE_EVENT:
      //#if DEBUG
      log.debug("Challenge Event Received");
      //#endif
      Challenge challenge = Challenge.unmarshal(input);
      callback.challenge(challenge);
      break;
    case MESSAGE_EVENT:
      //#if DEBUG
      log.debug("Incoming message event");
      //#endif
      byte type = input.readByte();
      String nick = input.readUTF();
      String message = input.readUTF();
      callback.message(type, nick, message);
      break;
    case GAME_EVENT:
      //#if DEBUG
      log.debug("Start game event");
      //#endif
      challenge = Challenge.unmarshal(input);
      callback.startGame(challenge, GameController.ONLINE_MODE);
      break;
    case TIME_EVENT:

      int whiteTime = input.readInt();
      int whiteByoStone = input.readInt();
      int blackTime = input.readInt();
      int blackByoStone = input.readInt();
      //#if DEBUG
      log.debug("W" + whiteTime + "/" + whiteByoStone + " B " + blackTime + "/" + blackByoStone);
      //#endif
      callback.synOnlineTime(whiteTime, whiteByoStone, blackTime, blackByoStone);
      break;
    case TIMES_UP_EVENT:
      //#if DEBUG
      log.debug("times up event");
      //#endif
      String name = input.readUTF();
      callback.timesUP(name);
      break;
    case END_GAME_EVENT:
      //#if DEBUG
      log.debug("end game event");
      //#endif
      callback.endGame();
      break;
    case MARK_STONE_EVENT:
      //#if DEBUG
      log.debug("mark stone event");
      //#endif
      byte x = input.readByte();
      byte y = input.readByte();
      callback.oppRemoveDeadStone(x, y);
      break;
    case RESTORE_GAME_FOR_COUNING_EVENT:
      //#if DEBUG
      log.debug("restore game for counting event");
      //#endif
      callback.restoreGameForCounting();
      break;
    case GAME_IS_DONE_EVENT:
      //#if DEBUG
      log.debug("game is done event");
      //#endif
      String name1 = input.readUTF();
      int value1 = input.readInt();
      String name2 = input.readUTF();
      int value2 = input.readInt();
      callback.gameIsDone(name1, value1, name2, value2);
      break;
    case HANDICAP_EVENT:
      //#if DEBUG
      log.debug("handicap event");
      //#endif
      byte value = input.readByte();
      callback.oppSetHandicap(value);
      break;

    case IGS_RESIGNED_EVENT:
      //#if DEBUG
      log.debug("resigned event");
      //#endif
      String resignedName = input.readUTF();
      callback.onlineResigned(resignedName);
      break;
    case SCORE_EVENT:
      //#if DEBUG
      log.debug("score event");
      //#endif
      int whiteScore = input.readInt();
      int blackScore = input.readInt();
      callback.onlineScore(whiteScore, blackScore);
      break;
    case LOST_MESSAGE_EVENT:
      //#if DEBUG
      log.debug("igs lost message event");
      //#endif
      String winnerName = input.readUTF();
      int winValue = input.readInt();
      callback.winByValue(winnerName, winValue);
      break;
    default:
      //#if DEBUG
      log.debug("Too specific event for me forward it");
      //#endif
      return false;
    }

    return true;
  }

  /**
   * @return true if the connection is OK
   * @throws IOException
   */
  protected abstract boolean connect() throws IOException;

  /**
  * @see java.lang.Thread#run()
  */
  public void run() {
    try {
      noneErrorDisconnect = true;
      if (!connect())
        return; // should not do anything as the specific error should be displayed by the underlying specific connect
      noneErrorDisconnect = false;
      while (true)
        handleEvent(input.readByte());

    } catch (java.io.InterruptedIOException iie) {
      //#if DEBUG
      log.error("Disconnected ", iie);
      //#endif
    } catch (IOException e) {
      //#if DEBUG
      log.error("IOException", e);
      //#endif
      Util.errorNotifier(e);
    } catch (Throwable t) {
      //#if DEBUG
      log.error("server loop error " + t.getClass(), t);
      //#endif
      Util.errorNotifier(t);
    }

    finally {
      if (output != null) {
        try {
          output.close();
        } catch (Throwable e) {
          // Do nothing
        }
      }
      if (input != null) {
        try {
          input.close();
        } catch (Throwable e) {
          // Do nothing
        }
      }
      if (!noneErrorDisconnect) {
        callback.message(MultiplayerCallback.MESSAGE_ERROR_TYPE, "", I18N.online.connectionError);
        noneErrorDisconnect = true;
      }
    }
    return;
  }

  public void disconnect() {
    try {
      if (input != null)
        input.close();
    } catch (IOException e) {
      //#if DEBUG
      log.error(e);
      //#endif
    }
    try {
      if (output != null)
        output.close();
    } catch (IOException e) {
      //#if DEBUG
      log.error(e);
      //#endif
    }
    noneErrorDisconnect = true;
  }

  public abstract String getCurrentOpponent();

  public Challenge getCurrentChallenge() {
    return currentChallenge;
  }

  public void setCurrentChallenge(Challenge currentChallenge) {
    this.currentChallenge = currentChallenge;
  }

}
