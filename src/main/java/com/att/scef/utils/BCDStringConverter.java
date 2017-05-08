package com.att.scef.utils;

/**
 * Created by Adi Enzel on 3/23/17.
 *
 * @author <a href="mailto:aa7133@att.com"> Adi Enzel </a>
 */
public class BCDStringConverter {
  private static final String   BCD_STRING = "0123456789*#abc";
  private static final char[]   BCD_CHAR   = BCD_STRING.toCharArray();

  public static byte[] toBCD (String str) {
    int length = (str == null ? 0:str.length());
    int size = (length + 1)/2;
    byte[] buf = new byte[size];

    int evenLoc = 0;
    int oddLoc = 1;

    for (int i = 0; i < size; ++i) {
      char c = str.charAt(evenLoc);
      int n2 = convNible(c);
      int octet = 0;
      int n1 = 15;
      if (oddLoc < length) {
        c = str.charAt(oddLoc);
        n1 = convNible(c);
      }
      octet = (n1 << 4) + n2;
      buf[i] = (byte)(octet & 0xFF);
      evenLoc += 2;
      oddLoc +=2;
    }
    return buf;
  }

  private static int convNible(char c) {

    int digit = Character.digit(c, 10); // convert to digit

    if (digit < 0 || digit > 9) {  // the E164 supports the charecters 'a', 'b', 'c', 'd', '#' and '*' the '*' and '#' are lower then normal digits order in ASCI
      switch (c) {
        case '*':
          digit = 10;
          break;
        case '#':
          digit = 11;
          break;
        case 'a':
          digit = 12;
          break;
        case 'b':
          digit = 13;
          break;
        case 'c':
          digit = 14;
          break;
        default:
          throw new NumberFormatException("character : " + c + " is bad " );
      }
    }
    return digit;
  }

  /**
   *
   * @param bcd  byte[] encoded BCD
   * @return  string if converted null if char empty.
   */
  public static String toStringNumber (byte[] bcd) {
    if (bcd.length == 0 || bcd == null) {
      return null;
    }

    int size = bcd.length;
    // the string is twice as the byte array
    StringBuffer buf = new StringBuffer(2*size);
    for (int i = 0; i < size; ++i) {
      int octet = bcd[i];
      int n2 = (octet >> 4) & 0xF; // move the upper nible to its location and clean the upper bytes
      int n1 = octet & 0xF; // clean upper bytes of the second
      if (n1 == 15) { // if there is filler in the specific byte since it is not supose to be with filler byte
        throw new NumberFormatException("Illegal filler in octet n=" + i);
      }
      buf.append(BCD_CHAR[n1]);  // add the string in n1 location

      if (n2 == 15) { // only if this is the last
        if (i != size - 1) { // needs to be only on the last one if size is odd number
          throw new NumberFormatException("Illegal filler in octet n=" + i);
        }
      }
      else {
        buf.append(BCD_CHAR[n2]);
      }
    }

    return buf.toString();
  }

}
