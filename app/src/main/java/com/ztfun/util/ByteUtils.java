package com.ztfun.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteUtils {
    /**
     * Convert a String object to byte array, trim to <code>size</code> or pad with 0 when necessary.
     * @param src
     *        Source of string to be converted to bytes
     * @param size
     *        Size of the result byte array
     * @return the result byte array, null if size is not greater than 0.
     */
    public static byte[] getBytes(String src, int size) {
        if (size <= 0) {
            return null;
        }

        if (src == null) {
            return new byte[size];
        }

        byte[] textBytes = src.getBytes();
        if (textBytes.length == size) {
            return textBytes;
        } else if (textBytes.length > size) {
            // trim to fit size
            return Arrays.copyOfRange(textBytes, 0, size);
        } else {
            // pad with zeroes
            byte[] result = new byte[size]; // byte array are initialized to zeroes in Java
            System.arraycopy(textBytes, 0, result, 0, textBytes.length);
            return result;
        }
    }

    /**
     * Convert a byte array to String, ignoring the bytes after (byte)0, similar to C's end of
     * string array.
     * @param src
     *        Source of byte array.
     * @param offset
     *        Offset of start position in <code>src</code>.
     * @param size
     *        Max size of bytes to be converted.
     * @return the result String object.
     */
    public static String toString(byte[] src, int offset, int size) {
        if (src == null || src.length < offset + size || size == 0) {
            return null;
        }

        // find the first non zero byte
        for (int i = offset; i < offset + size; i ++) {
            if (src[i] == 0) {
                size = i - offset;
                break;
            }
        }

        return String.valueOf(new String(src).toCharArray(), offset, size);
    }

    /**
     * Convert hex representation in the form of [ \t]*[0x|0X]?[0-9A-Fa-f _]+[ \t]* to byte array.
     * 1. trim leading and trailing space
     * 2. remove leading 0x|0X if any
     * 3. remove all spaces( ) and underscores(_) if any
     * 4. return null if contains letters other than [0-9A-Fa-f]
     * 5. return null if normalized length is not even
     *
     * @param hexString hex representation of byte array
     * @return bytes array or null
     */
    public static byte[] hexString2Bytes(String hexString) {
        if (hexString == null) {
            return null;
        }

        // remove leading and trailing white spaces
        String normalizedHexString = hexString.trim();

        // remove leading 0x | 0X
        if (normalizedHexString.startsWith("0x") || normalizedHexString.startsWith("0X")) {
            normalizedHexString = normalizedHexString.substring(2);
        }

        // remove space & underscore, the purpose of which is only for readability
        normalizedHexString = normalizedHexString.replace(" ", "").replace("_", "");

        // check if contains letters other than [0-9A-Fa-f]
        if (! normalizedHexString.matches("^[0-9A-Fa-f]+$")) {
            return null;
        }

        // check whether the length of the string is even
        if (normalizedHexString.length() % 2 != 0) {
            return null;
        }

        byte[] data = new byte[normalizedHexString.length() / 2];
        for (int i = 0; i < data.length; i ++) {
            data[i] = (byte)(Integer.parseInt(normalizedHexString.substring(i << 1, (i << 1) + 2), 16));
        }

        return data;
    }

    public static String bytes2HexString(byte[] data) {
        if (data == null || data.length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("0X");
        for (int i = 0; i < data.length; i ++) {
            sb.append(String.format("%02X", data[i]));
            // append an underscore for every 4 bytes if not the last one
            if (i % 4 == 3 && i != data.length - 1) {
                sb.append('_');
            }
        }

        return sb.toString();
    }

    /**
     * default to 4 bytes and big endian
     */
    public static long getUnsignedInt(byte[] data, int offset) {
        return getUnsignedInt(data, offset, 4, ByteOrder.BIG_ENDIAN);
    }

    /**
     * default to big endian
     */
    public static long getUnsignedInt(byte[] data, int offset, int size) {
        return getUnsignedInt(data, offset, size, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Get integer with <code>size</code> bytes with <code>offset</code> in byte array
     * <code>data</code>, in Big endian byte order.
     * @param data byte array
     * @param offset offset in <code>data</code>
     * @param size should not be larger than 8 bytes, otherwise overflow might occur.
     * @return unsigned long, -1 if parameters are invalid.
     */
    public static long getUnsignedInt(byte[] data, int offset, int size, ByteOrder bo) {
        if (data == null || data.length < offset + size || size <= 0 || size > 8) {
            return -1;
        }

        long result = 0;

        if (bo == ByteOrder.LITTLE_ENDIAN) {
            // little endian
            for (int i = 0; i < size; i ++){
                result |= ((long)(data[offset++] & 0xFF) << (8 * i));
            }
        } else {
            // fall back to big endian
            result = data[offset++] & 0xFF;
            while((--size) > 0) {
                result = (result << 8) | (data[offset++] & 0xFF);
            }
        }

        return result;
    }

    /**
     * unpack byte array to JSONObject, with descriptions similar to python struct.unpack
     */
    public static JSONObject unpack(String[] formats, byte[] buffer) {
        return unpack(formats, buffer, 0);
    }

    /**
     * First character is byte order, if any.
     * Character	Byte order	Size	Alignment
     * @	native	native	native
     * =	native	standard	none
     * <	little-endian	standard	none
     * >	big-endian	standard	none
     * !	network (= big-endian)	standard	none
     */
    // private static final String UNPACK_ENDIAN_CHARSET = "@=<>!";
    private static final String UNPACK_LITTLE_ENDIAN_CHARSET = "<";
    private static final String UNPACK_BIG_ENDIAN_CHARSET = ">!";

    /**
     * Format characters. Currently only xcbB?hHiIlLqQnNfs are supported. Half precision e is not
     * supported.
     *
     * Format  C Type          Python type     Standard size
     * x       pad byte        no value
     * c       char            bytes of length 1       1
     * b       signed char     integer                 1
     * B       unsigned char   integer                 1
     * ?       _Bool           bool                    1
     * h       short           integer                 2
     * H       unsigned short  integer                 2
     * i       int             integer                 4
     * I       unsigned int    integer                 4
     * l       long            integer                 4
     * L       unsigned long   integer                 4
     * q       long long       integer                 8
     * Q       unsigned long long      integer         8
     * n       ssize_t         integer
     * N       size_t          integer
     * e       (6)             float                   2
     * f       float           float                   4
     * d       double          float                   8
     * s       char[]          bytes
     * p       char[]          bytes
     * P       void *          integer
     *
     *
     * (6) The IEEE 754 binary16 “half precision” type was introduced in the 2008 revision of the
     * IEEE 754 standard. It has a sign bit, a 5-bit exponent and 11-bit precision (with 10 bits
     * explicitly stored), and can represent numbers between approximately 6.1e-05 and 6.5e+04 at
     * full precision. This type is not widely supported by C compilers: on a typical machine, an
     * unsigned short can be used for storage, but not for math operations. See the Wikipedia page
     * on the half-precision floating-point format for more information.
     *
     * Usage:
     * <code>
     *     String[] desc = new String[]{
     *             ">",  // byte order, default is big endian
     *             "cStrength", "iWidth", "HHeight", "bLevel", // descriptions
     *             "?isConnected", "s4Name" // string with 4 bytes, put it in Name field
     *     };
     *     // space and underscore are ignored in ByteUtils.hexString2Bytes
     *     String hexString = "0x 02000001_ffFFFF03 0161626300";
     *     JSONObject json = ByteUtils.unpack(desc, ByteUtils.hexString2Bytes(hexString));
     *     assertNotNull(json);
     *     assertEquals(0x02, json.getInt("Strength"));
     *     assertEquals(0x01ff, json.getInt("Width"));
     *     assertEquals(0xFFFF, json.getInt("Height"));
     *     assertEquals(0x03, json.getInt("Level"));
     *     assertTrue(json.getBoolean("isConnected"));
     *     // 0x61626300 is abc, the last 00 is ignored.
     *     assertEquals(new String(new char[]{0x61, 0x62, 0x63}), json.getString("Name"));
     *
     *     byte[] packed = ByteUtils.pack(desc, json);
     *     assertArrayEquals(packed, ByteUtils.hexString2Bytes(hexString));
     * </code>
     */
    public static JSONObject unpack(String[] formats, byte[] buffer, int offset) {
        if (formats == null || formats.length <= 0 ||
                buffer == null || offset >= buffer.length) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, buffer.length - offset);
        int i = 0; // format index
        int bufferOffset = 0;

        // byte order
        String firstCharInString = String.valueOf(formats[0].charAt(0));
        if (UNPACK_LITTLE_ENDIAN_CHARSET.contains(firstCharInString)) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            i ++;
        } else if (UNPACK_BIG_ENDIAN_CHARSET.contains(firstCharInString)) {
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            i ++;
        }

        try {
            JSONObject json = new JSONObject();
            for (; i < formats.length; i ++) {
                String name = formats[i].substring(1);
                switch (formats[i].charAt(0)) {
                    case 'c': // char in one byte
                    case 'b': // signed char in one byte
                        json.put(name, byteBuffer.get(bufferOffset));
                        bufferOffset += 1;
                        break;

                    case 'B': // unsigned char in one byte
                        json.put(name, byteBuffer.get(bufferOffset) & 0xFF);
                        bufferOffset += 1;
                        break;

                    case '?': // boolean in one byte
                        json.put(name, byteBuffer.get(bufferOffset) != 0);
                        bufferOffset += 1;
                        break;

                    case 'h': // short in 2 bytes
                        json.put(name, byteBuffer.getShort(bufferOffset));
                        bufferOffset += 2;
                        break;

                    case 'H': // unsigned short in 2 bytes
                        json.put(name, byteBuffer.getShort(bufferOffset) & 0xFFFF);
                        bufferOffset += 2;
                        break;

                    case 'i': // integer in 4 bytes
                    case 'l': // long in 4 bytes
                    case 'n': // ssize_t
                    case 'N': // ssize_t
                        json.put(name, byteBuffer.getInt(bufferOffset));
                        bufferOffset += 4;
                        break;

                    case 'I': // unsigned integer in 4 bytes, raise to 8 bytes Java long
                    case 'L': // unsigned long in 4 bytes
                        json.put(name, byteBuffer.getInt(bufferOffset) & 0xFFFFFFFFFFL);
                        bufferOffset += 4;
                        break;

                    case 'q': // long long in 8 bytes
                    case 'Q': // unsigned long long in 8 bytes fixme: unsigned might be different with signed long
                        json.put(name, byteBuffer.getLong(bufferOffset)); //
                        bufferOffset += 8;
                        break;

                    case 'f':
                        json.put(name, byteBuffer.getFloat(bufferOffset));
                        bufferOffset += 4;
                        break;

                    case 'd':
                        json.put(name, byteBuffer.getDouble(bufferOffset));
                        bufferOffset += 8;
                        break;

                    case 's': // string s13Name s[0-9]+.+
                        DescS s = DescS.parse(formats[i]);
                        json.put(s.name, toString(byteBuffer.array(), bufferOffset, s.length));
                        bufferOffset += s.length;
                        break;

                    case 'x': // pad byte
                    default:
                        break;
                }
            }

            return json;
        } catch (JSONException | IndexOutOfBoundsException | InvalidPropertiesFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] pack(String[] formats, JSONObject json) {
        if (formats == null || formats.length <= 0 || json ==null) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(formats.length * 8); // make it large enough
        int i = 0; // format index
        int size = 0;

        // byte order
        String firstCharInString = String.valueOf(formats[0].charAt(0));
        if (UNPACK_LITTLE_ENDIAN_CHARSET.contains(firstCharInString)) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            i ++;
        } else if (UNPACK_BIG_ENDIAN_CHARSET.contains(firstCharInString)) {
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            i ++;
        }

        try {
            for (; i < formats.length; i ++) {
                String name = formats[i].substring(1);
                switch (formats[i].charAt(0)) {
                    case 'x':  // pad byte
                        byteBuffer.put((byte)0);
                        size += 1;
                        break;

                    case 'c': // char in one byte
                    case 'b': // signed char in one byte
                    case 'B': // unsigned char in one byte
                        byteBuffer.put((byte)(json.getInt(name) & 0xFF));
                        size += 1;
                        break;

                    case '?': // boolean in one byte
                        byteBuffer.put(json.getBoolean(name) ? (byte)1 : (byte)0);
                        size += 1;
                        break;

                    case 'h': // short in 2 bytes
                    case 'H': // unsigned short in 2 bytes
                        byteBuffer.putShort((short)(json.getInt(name) & 0xFFFF));
                        size += 2;
                        break;

                    case 'i': // integer in 4 bytes
                    case 'l': // long in 4 bytes
                    case 'n': // ssize_t
                    case 'N': // ssize_t
                    case 'I': // unsigned integer in 4 bytes, raise to 8 bytes Java long
                    case 'L': // unsigned long in 4 bytes
                        byteBuffer.putInt(json.getInt(name));
                        size += 4;
                        break;

                    case 'q': // long long in 8 bytes
                        byteBuffer.putLong(json.getLong(name));
                        size += 8;
                        break;

                    case 'f':
                        byteBuffer.putFloat((float)json.getDouble(name));
                        size += 4;
                        break;

                    case 'd':
                        byteBuffer.putDouble(json.getDouble(name));
                        size += 8;
                        break;

                    case 's': // string s13Name s[0-9]+.+
                        DescS s = DescS.parse(formats[i]);
                        String value = json.getString(s.name);
                        int count = Math.min(s.length, value.length());
                        byteBuffer.put(value.getBytes(), 0, count);
                        // pad with 0 if necessary
                        count = s.length - count;
                        if (count > 0) {
                            byteBuffer.put(new byte[count], 0, count);
                        }
                        size += s.length;
                        break;

                    default:
                        break;
                }
            }

            // limit the result to actual size
            return Arrays.copyOfRange(byteBuffer.array(), 0, size);
        } catch (JSONException | IndexOutOfBoundsException | NullPointerException |
                NumberFormatException | InvalidPropertiesFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class DescS {
        public int length;
        public String name;

        public static DescS parse(String desc) throws InvalidPropertiesFormatException {
            Matcher matcher = Pattern.compile("^s([0-9]+)(.+)$").matcher(desc);
            if (! matcher.find()) {
                throw new InvalidPropertiesFormatException("Invalid description:" + desc);
            }

            DescS s = new DescS();
            s.length = Integer.parseInt(matcher.group(1));
            s.name = matcher.group(2);

            return s;
        }
    }
}
