/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.Gome;

public class Util {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Util");
  //#endif

  public static final int COLOR_WHITE = 0x00FFFFFF;
  public static final int COLOR_BLACK = 0x00000000;
  public static final int COLOR_DARKGREY = 0x00555555;
  public static final int COLOR_GREY = 0x00999999;
  public static final int COLOR_LIGHTGREY = 0x00CCCCCC;
  public static final int COLOR_RED = 0x00FF0000;
  public static final int COLOR_GREEN = 0x0000FF00;
  public static final int COLOR_BLUE = 0x000000FF;
  public static final int COLOR_LIGHT_BACKGROUND = 0x00FFEE00;
  public static final String EMPTY_STRING = ""; //$NON-NLS-1$
  private static Random rnd = new Random();

  public static int blendColors(int color1, int color2) {
    int r = (((color1 & 0xFF0000) + (color2 & 0xFF0000)) >> 1) & 0xFF0000;
    int g = (((color1 & 0x00FF00) + (color2 & 0x00FF00)) >> 1) & 0x00FF00;
    int b = (((color1 & 0x0000FF) + (color2 & 0x0000FF)) >> 1) & 0x0000FF;
    return r | g | b;
  }

  /**
   * returns the number from 0 to max-1
   * 
   * @param max
   * @return
   */
  public static int rnd(int max) {
    return (Math.abs(rnd.nextInt()) % max);
  }// rnd

  /**
   * returns the key name string for a action code. Note that an action may be
   * mapped to multiple keys, this function can only retrieve one of them.
   * 
   * @param can
   *            a canvas instance
   * @param act
   *            the action code, i.e. canvas.GAME_A
   * @return
   */
  public static final String getActionKeyName(Canvas can, int keyCode) {
    try {
      return can.getKeyName(keyCode);
    } catch (Throwable t) {
      return "Not Assigned";
    }
  }

  /**
   * count the occurence of a char
   */
  public static int countChar(String str, char ch) {
    int count = 0;
    for (int i = 0; i < str.length(); ++i) {
      if (str.charAt(i) == ch)
        ++count;
    }

    return count;
  }

  public static void messageBox(String title, String content, AlertType type) {
    Alert al = new Alert(title, content, null, type);
    al.setTimeout(Alert.FOREVER);
    Gome.singleton.display.setCurrent(al);
  }

  public static String formatSgfString(String sgf) {
    String str;
    StringBuffer buf = new StringBuffer();

    str = sgf.substring(sgf.indexOf('('), sgf.lastIndexOf(')'));

    for (int i = 0; i < str.length(); ++i) {
      char ch = str.charAt(i);

      if (ch != ' ' && ch != '\r' && ch != '\n') {
        buf.append(ch);
      }
    }

    return buf.toString();

  }

  public static Image renderOffScreenScrollableText(String text, int width, int scrollHeight, Font font, int fgColor, int bgcolor) {
    Vector lines = lineSplitter(text, width, font);
    int textHeight = lines.size() * font.getHeight();
    Image imagedComment;
    //#ifdef DEBUG
    log.debug("scrollHeight = " + scrollHeight);
    log.debug("font height = " + font.getHeight());
    //#endif
    int extraLines = scrollHeight / font.getHeight() + 1;
    //#ifdef DEBUG
    log.debug("Extralines = " + extraLines);
    //#endif
    if (lines.size() >= extraLines) {
      imagedComment = Image.createImage(width, textHeight + scrollHeight);
      // read the beginning at the end of the scroller
      for (int i = 0; i < extraLines; i++) {
        lines.addElement(lines.elementAt(i));
      }
    } else {
      imagedComment = Image.createImage(width, scrollHeight); // basically,
      // don't
      // scroll
    }
    drawText(imagedComment.getGraphics(), 0, 0, width, imagedComment.getHeight(), lines, font, fgColor, bgcolor);
    return imagedComment;
  }

  private static Vector lineSplitter(String text, int width, Font font) {
    Vector lines = new Vector();
    char[] commentChars = text.toCharArray();

    int totallength = commentChars.length;
    int index = 0;
    while (index < totallength) {
      int lineWidth = 0;
      int lastSpace = -1;
      int lineBeginIndex = index;
      while (lineWidth < width && index < totallength) {
        char currentChar = commentChars[index];
        if ((currentChar == '\n' || currentChar == '\r')) {

          lastSpace = index;
          break;
        }
        if (currentChar == ' ' || currentChar == ',' || currentChar == '.' || currentChar == ';' || currentChar == ':' || currentChar == '!' || currentChar == '?' || currentChar == '\t'
                || currentChar == '(' || currentChar == ')') {
          lastSpace = index;
        }
        lineWidth += font.charWidth(currentChar);
        index++;
      }

      if (index != totallength) {
        index = lastSpace + 1;
        if (lastSpace == -1)
          lastSpace = index - 1;
      } else {
        lastSpace = totallength - 1;
      }

      String line = new String(commentChars, lineBeginIndex, lastSpace - lineBeginIndex + 1).trim();
      if (line.length() != 0)
        lines.addElement(line);
    }
    return lines;
  }

  private static void drawText(Graphics graphics, int x, int y, int width, int heigth, Vector lines, Font font, int fgColor, int bgcolor) {
    Enumeration lineEnum = lines.elements();
    int drawat = y;
    graphics.setColor(bgcolor);
    graphics.fillRect(x, y, width, heigth);
    graphics.setFont(font);
    graphics.setColor(fgColor);
    while (lineEnum.hasMoreElements()) {
      graphics.drawString((String) lineEnum.nextElement(), x, drawat, Graphics.LEFT | Graphics.TOP);
      drawat += font.getHeight();
    }
  }

  public static Image renderOffScreenTextIcon(String text, int width, int height, Font font, int fgColor, int bgcolor) {
    Image imagedComment = Image.createImage(width, height);
    Graphics graphics = imagedComment.getGraphics();
    graphics.setColor(bgcolor);
    graphics.fillRect(0, 0, width, height);
    graphics.setFont(font);
    graphics.setColor(fgColor);
    graphics.drawString(text, 0, 0, Graphics.LEFT | Graphics.TOP);
    return Image.createImage(imagedComment);
  }

  public static Image renderIcon(Image icon, int width, int height) {
    Image imagedComment = Image.createImage(width, height);
    Graphics graphics = imagedComment.getGraphics();
    //#ifdef MIDP2
    int[] is = new int[width * height];
    for (int i = 0; i < is.length; i++) {
      is[i] = 0xffffffff;
    }
    graphics.drawRGB(is, 0, width, 0, 0, width, height, true);
    //#endif
    graphics.drawImage(icon, (width - icon.getWidth()) / 2, (height - icon.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
    return Image.createImage(imagedComment);
  }

  // public static Image
  // renderOffScreenScrollableTextWithGraphicPreambule(Image start, String
  // string, int width, int height, Font scrollerFont, int fgcolor, int
  // bgcolor) {
  // Image text = renderOffScreenText(string, width, scrollerFont, fgcolor,
  // bgcolor);
  // Image result = Image.createImage(width, text.getHeight() +
  // start.getHeight() + height);
  // Graphics g = result.getGraphics();
  // g.drawImage(start, 0, 0, Graphics.TOP | Graphics.LEFT);
  // g.drawImage(text, 0, start.getHeight(), Graphics.TOP | Graphics.LEFT);
  // g.drawImage(start, 0, text.getHeight() + start.getHeight(), Graphics.TOP
  // | Graphics.LEFT);
  // if (start.getHeight() < height)
  // {
  // g.drawImage(text, 0, text.getHeight() + 2 * start.getHeight(),
  // Graphics.TOP | Graphics.LEFT);
  // }
  //        
  // return result;
  // }

  public static void renderSplash(Graphics g, String splashInfo, int totalwidth, int totalHeight, Font info_font, int fg, int bg) {

    int textWidth = totalwidth * 5 / 6;
    Vector lines = lineSplitter(splashInfo, textWidth, info_font);
    int height = (lines.size()) * info_font.getHeight();
    int x = totalwidth / 12;
    int y = totalHeight / 4;
    drawText(g, x, y, textWidth, height, lines, info_font, fg, bg);
    g.setColor(Util.COLOR_LIGHTGREY);
    g.drawRect(x - 1, y - 1, textWidth + 1, height + 1);
    g.setColor(Util.COLOR_DARKGREY);
    for (int i = 1; i <= 3; i++) {
      g.drawLine(x + 3, y + height + i, x + textWidth + i, y + height + i);
      g.drawLine(x + textWidth + i, y + 3, x + textWidth + i, y + height + i);
    }
  }

  public static String komi2String(byte komi) {
    String mikomi = String.valueOf(komi / 2);
    if (komi % 2 == 0)
      return mikomi + ".0";
    return mikomi + ".5";
  }

  public static byte string2Komi(String komi) {
    int k = Integer.parseInt(komi.substring(0, komi.indexOf('.')));
    if (komi.indexOf(".5") != -1)
      return (byte) (k * 2 + 1);
    return (byte) (k * 2);
  }

  private static final char[] KEY = { 't', 'h', 'e', ' ', '$', ' ', 'a', 's', 'k', 'e', 'd', ' ', 'f', 'o', 'r', ' ', 'G', 'o', 'm', 'e', ' ', 'i', 's', ' ', '1', '0', '0', '%', ' ', 'i', 'n', 'v',
          'e', 's', 't', 'e', 'd', ' ', 'i', 'n', ' ', 'i', 'm', 'p', 'r', 'o', 'v', 'i', 'n', 'g', ' ', 'i', 't', ' ', 'd', 'o', ' ', 't', 'h', 'e', ' ', 'u', 's', 'e', 'r', 's', ' ', 'a', ' ', 'r',
          'e', 'a', 'l', ' ', 'f', 'a', 'v', 'o', 'r', ' ', 'd', 'o', 'n', '\'', 't', ' ', 'c', 'r', 'a', 'c', 'k', ' ', 'm', 'e' };

  public static String keygen(String name) {
    StringBuffer sb = new StringBuffer(name);
    int l = sb.length();
    for (int i = 0; i < l; i++)
      sb.setCharAt(i, (char) (sb.charAt(i) ^ KEY[i]));

    String coded = pad(Integer.toHexString(sb.toString().hashCode()), name.length());
    String plain = pad(Integer.toHexString(name.hashCode()), name.length() + 2);

    String key = coded + plain;
    return key;
  }

  private static String pad(String source, int offset) {
    StringBuffer sb = new StringBuffer(source);

    while (sb.length() < 8) {
      sb.insert(0, Integer.toHexString(KEY[offset++] % 16));
    }
    return sb.toString();
  }

  public static String padd(String str, int width, boolean left) {
    StringBuffer buff = new StringBuffer();
    int l = str.length();
    if (!left) {
      buff.append(str);
    }
    for (int i = l; i <= width; i++) {
      buff.append(' ');
    }
    if (left) {
      buff.append(str);
    }
    return buff.toString();
  }
}