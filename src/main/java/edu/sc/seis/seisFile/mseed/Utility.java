package edu.sc.seis.seisFile.mseed;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility.java
 * 
 * 
 * Created: Fri Apr 2 14:28:55 1999
 * 
 * @author Philip Crotwell
 */
public class Utility {
    
    public static int extractInteger(byte[] info, int start, int length) {
        return Integer.parseInt(extractString(info, start, length));
    }
    
    public static String extractString(byte[] info, int start, int length) {
        byte[] subbytes = new byte[length];
        System.arraycopy(info, start, subbytes, 0, length);
        return new String(subbytes);
    }
    
    public static String extractVarString(byte[] info, int start, int maxLength) {
        return extractTermString(info, start, maxLength, (byte)126);
    }

    public static String extractNullTermString(byte[] info, int start, int maxLength) {
        return extractTermString(info, start, maxLength, (byte)0);
    }
    
    public static void writeNullTermString(String value, int maxLength, DataOutput out) throws IOException {
        String s = value;
        if (s.length() > maxLength) { s = s.substring(0, maxLength); }
        out.writeBytes(s);
        for (int i = s.length(); i < maxLength; i++) {
            out.write((byte)0);
        }
    }

    static String extractTermString(byte[] info, int start, int maxLength, byte termChar) {
        int length = 0;
        while (length<maxLength && start+length<info.length && info[start+length] != termChar) {
            length++;
        }
        if (length == 0) {
            return "";
        }
        byte[] tmp = new byte[length];
        System.arraycopy(info, start, tmp, 0, length);
        return new String(tmp);
    }
    
    public static short bytesToShort(byte hi, byte low, boolean swapBytes) {
        if(swapBytes) {
            return (short)((hi & 0xff) + ((low & 0xff) << 8));
        } else {
            return (short)(((hi & 0xff) << 8) + (low & 0xff));
        }
    }

    public static int bytesToInt(byte a) {
        return (int)a;
    }

    public static int uBytesToInt(byte a) {
        // we and with 0xff in order to get the sign correct (pos)
        return a & 0xff;
    }

    public static int bytesToInt(byte[] info, int start, boolean swapBytes) {
        return bytesToInt(info[start], info[start + 1], info[start + 2], info[start + 3], swapBytes);
    }

    public static long bytesToLong(byte[] info, int start, boolean swapBytes) {
        return bytesToLong(info[start], info[start + 1], info[start + 2], info[start + 3], info[start + 4], info[start + 5], info[start + 6], info[start + 7], swapBytes);
    }

    public static int bytesToInt(byte a, byte b, boolean swapBytes) {
        if(swapBytes) {
            return (a & 0xff) + ((int)b << 8);
        } else {
            return ((int)a << 8) + (b & 0xff);
        }
    }

    public static int uBytesToInt(byte a, byte b, boolean swapBytes) {
        // we "and" with 0xff to get the sign correct (pos)
        if(swapBytes) {
            return (a & 0xff) + ((b & 0xff) << 8);
        } else {
            return ((a & 0xff) << 8) + (b & 0xff);
        }
    }

    public static int bytesToInt(byte a, byte b, byte c, boolean swapBytes) {
        if(swapBytes) {
            return (a & 0xff) + ((b & 0xff) << 8) + ((int)c << 16);
        } else {
            return ((int)a << 16) + ((b & 0xff) << 8) + (c & 0xff);
        }
    }

    public static int bytesToInt(byte a,
                                 byte b,
                                 byte c,
                                 byte d,
                                 boolean swapBytes) {
        if(swapBytes) {
            return ((a & 0xff)) + ((b & 0xff) << 8) + ((c & 0xff) << 16)
                    + ((d & 0xff) << 24);
        } else {
            return ((a & 0xff) << 24) + ((b & 0xff) << 16) + ((c & 0xff) << 8)
                    + ((d & 0xff));
        }
    }

    public static long bytesToLong(byte a,
                                 byte b,
                                 byte c,
                                 byte d,
                                 byte e,
                                 byte f,
                                 byte g,
                                 byte h,
                                 boolean swapBytes) {
        if(swapBytes) {
            return ((a & 0xffl)) + ((b & 0xffl) << 8) + ((c & 0xffl) << 16)
                    + ((d & 0xffl) << 24) + ((e & 0xffl) << 32) + ((f & 0xffl) << 40) + ((g & 0xffl) << 48)
                    + ((h & 0xffl) << 56);
        } else {
            return ((a & 0xffl) << 56) + ((b & 0xffl) << 48) + ((c & 0xffl) << 40)
                    + ((d & 0xffl) << 32) + ((e & 0xffl) << 24) + ((f & 0xffl) << 16) + ((g & 0xffl) << 8)
                    + ((h & 0xffl));
        }
    }
    
    public static float bytesToFloat(byte a,
                                 byte b,
                                 byte c,
                                 byte d,
                                 boolean swapBytes) {
        return Float.intBitsToFloat(bytesToInt(a, b, c, d, swapBytes));
    }
    
    public static double bytesToDouble(byte a,
                                 byte b,
                                 byte c,
                                 byte d,
                                 byte e,
                                 byte f,
                                 byte g,
                                 byte h,
                                 boolean swapBytes) {
        return Double.longBitsToDouble(bytesToLong(a, b, c, d, e, f, g, h, swapBytes));
    }
    
    public static double bytesToDouble(byte[] info, int start, boolean swapBytes) {
        return Double.longBitsToDouble(Utility.bytesToLong(info, start, swapBytes));
    }
    
    public static float bytesToFloat(byte[] info, int start, boolean swapBytes) {
        return Float.intBitsToFloat(Utility.bytesToInt(info, start, swapBytes));
    }

    public static byte[] intToByteArray(int a) {
        byte[] returnByteArray = new byte[4];// int is 4 bytes
        returnByteArray[0] = (byte)((a >> 24) & 0xff);
        returnByteArray[1] = (byte)((a >> 16) & 0xff);
        returnByteArray[2] = (byte)((a >>  8) & 0xff);
        returnByteArray[3] = (byte)((a      ) & 0xff);
        return returnByteArray;
    }

    public static byte[] floatToByteArray(float a) {
        return intToByteArray(Float.floatToIntBits(a));
    }


    public static byte[] longToByteArray(long a) {
        byte[] returnByteArray = new byte[8];// long is 8 bytes
        returnByteArray[0] = (byte)((a >>> 56) & 0xffl);
        returnByteArray[1] = (byte)((a >>> 48) & 0xffl);
        returnByteArray[2] = (byte)((a >>> 40) & 0xffl);
        returnByteArray[3] = (byte)((a >>> 32) & 0xffl);
        returnByteArray[4] = (byte)((a >>> 24) & 0xffl);
        returnByteArray[5] = (byte)((a >>> 16) & 0xffl);
        returnByteArray[6] = (byte)((a >>>  8) & 0xffl);
        returnByteArray[7] = (byte)((a       ) & 0xffl);
        return returnByteArray;
    }
    
    public static byte[] doubleToByteArray(double d) {
        return longToByteArray(Double.doubleToLongBits(d));
    }
    
    /**
     * Inserts float into dest at index pos 
     */
    public static void insertFloat(float value, byte[] dest, int pos) {
        int bits = Float.floatToIntBits(value);
        byte[] b = Utility.intToByteArray(bits);
        System.arraycopy(b, 0, dest, pos, 4);
    }

    public static byte[] pad(byte[] source, int requiredBytes, byte paddingByte) {
        if(source.length == requiredBytes) {
            return source;
        } else {
            byte[] returnByteArray = new byte[requiredBytes];
            System.arraycopy(source, 0, returnByteArray, 0, source.length);
            for(int i = source.length; i < requiredBytes; i++) {
                returnByteArray[i] = paddingByte;
            }
            return returnByteArray;
        }
    }

    public static byte[] format(byte[] source, int start, int end) {
        byte[] returnByteArray = new byte[start - end + 1];
        int j = 0;
        for(int i = start; i < end; i++, j++) {
            returnByteArray[j] = source[i];
        }
        return returnByteArray;
    }
    
    public static boolean areContiguous(DataRecord first, DataRecord second) {
        Btime fEnd = first.getPredictedNextStartBtime();
        Btime sBegin = second.getHeader().getStartBtime();
        return fEnd.tenthMilli == sBegin.tenthMilli &&
            fEnd.sec == sBegin.sec &&
            fEnd.min == sBegin.min &&
            fEnd.hour == sBegin.hour &&
            fEnd.jday == sBegin.jday &&
            fEnd.year == sBegin.year;
    }
    
    /** breaks the List into sublists where the DataRecords are contiguous. Assumes
     * that the input List is sorted (by begin time?) and does not contain overlaps.
     */
    public static List<List<DataRecord>> breakContiguous(List<DataRecord> inList) {
        List<List<DataRecord>> out = new ArrayList<List<DataRecord>>();
        List<DataRecord> subout = new ArrayList<DataRecord>();
        DataRecord prev = null;
        for (DataRecord dataRecord : inList) {
            if (prev == null) { 
                // first one
                out.add(subout);
            } else if (areContiguous(prev, dataRecord)) {
                // contiguous
            } else {
                subout = new ArrayList<DataRecord>();
                out.add(subout);
            }
            subout.add(dataRecord);
            prev = dataRecord;
        }
        return out;
    }

    public static void cleanDuplicatesOverlaps(List<DataRecord> drFromFileList) {
        Collections.sort(drFromFileList, new DataRecordBeginComparator());
        DataRecord prev = null;
        Iterator<DataRecord> itFromFileList = drFromFileList.iterator();
        while (itFromFileList.hasNext()) {
            DataRecord dataRecord = itFromFileList.next();
            if (prev != null && prev.getHeader().getStartBtime().equals(dataRecord.getHeader().getStartBtime())) {
                //  a duplicate
                itFromFileList.remove();
            } else if (prev != null && prev.getLastSampleBtime().afterOrEquals(dataRecord.getHeader().getStartBtime())) {
                //  a overlap
                itFromFileList.remove();
            } else {
                prev = dataRecord;
            }
        }
    }
} // Utility
