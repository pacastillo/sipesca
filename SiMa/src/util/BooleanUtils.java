/* Copyright (c) 2002 The European Commission DREAM Project IST-1999-12679
 *
 * This file is part of JEO.
 * JEO is free software; you can redistribute it and/or modify it under the terms of GNU 
 * General Public License as published by the Free Sortware Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JEO is distributed in the hope that it will be useful,but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for mor details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 TEmple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package util;

//import org.apache.log4j.*;
/**
 * This class holds statics method to manage boolean strings translations into int values.
 * @author  Mar�a Isabel Garc�a Arenas (Dpto: ATC. Universidad de Granada)
 * @see java.io.Serializable
 */

public class BooleanUtils implements java.io.Serializable{
//        private Logger logger                     = Logger.getLogger("SA");

    
    /**
     * Gets the integer represented by a bit string (as an array).
     * @param pBits boolean array representing the bits (should, of course,
     * never be longer than the length of an integer)
     * @return int represented by this bit string (most important bit is leftmost)
     */
    public static int bitsToInt(boolean[] pBits) {
        int vInt = 0;
        int vIntBit = 0;
        for (int i = pBits.length - 1; i >= 0; i--) {
            if (pBits[i]) {
                vInt |= (1 << vIntBit);
            }
            vIntBit++;
        }
        return vInt;
    }
    
    /**
     * Gets the bit string represented by an integer. Inverse function of bitsToInt.
     * @param pInt int to convert
     * @param pBits bit string to store result
     */
    public static void intToBits(int pInt, boolean[] pBits) {
        int vBitIndex = pBits.length - 1;
        for (int i = 0; i < pBits.length; i++) {
            pBits[vBitIndex--] = (((pInt >> i) & 1) == 1);
        }
    }
    

    /**
     * Gets the integer represented by a bit string (as a String of 0's and 1's).
     * @param pBits String array representing the bits (should, of course,
     * never be longer than the length of an integer)
     * @return int represented by this bit string (most important bit is leftmost)
     */
    public static int getInt(String pString) {
	return bitsToInt(stringToBits(pString));
    }

    /**
     * Converts a String of 0's and 1's to a boolean[]. 
     * @param pString string of 0's and 1's
     * @return boolean[] of length pString.length() which has true for each index which is non-zero in the String
     */
    public static boolean[] stringToBits(String pString) {
	boolean[] vBits = new boolean[pString.length()];
	for (int i = 0; i < vBits.length; i++) {
	    vBits[i] = pString.charAt(i) != '0';
	}

	return vBits;
    }

    /**
     * Converts a boolean[] to a String of 0's and 1's.
     * @param pBits boolean[] to convert
     * @return String of 0's and 1's with length pBits.length
     */
    public static String bitsToString(boolean[] pBits) {
	String vString = "";
	for (int i = 0; i < pBits.length; i++) {
	    vString += (pBits[i]?"1":"0");
	}

	return vString;
    }
    
    
    
}
