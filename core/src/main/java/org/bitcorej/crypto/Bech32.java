package org.bitcorej.crypto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;

public class Bech32 {
    /** The Bech32 character set for encoding. */
    private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    public static class Bech32Data {
        public final String hrp;
        public final byte[] data;

        private Bech32Data(final String hrp, final byte[] data) {
            this.hrp = hrp;
            this.data = data;
        }
    }

    /** Find the polynomial with value coefficients mod the generator as 30-bit. */
    private static int polymod(final byte[] values) {
        int c = 1;
        for (byte v_i: values) {
            int c0 = (c >>> 25) & 0xff;
            c = ((c & 0x1ffffff) << 5) ^ (v_i & 0xff);
            if ((c0 &  1) != 0) c ^= 0x3b6a57b2;
            if ((c0 &  2) != 0) c ^= 0x26508e6d;
            if ((c0 &  4) != 0) c ^= 0x1ea119fa;
            if ((c0 &  8) != 0) c ^= 0x3d4233dd;
            if ((c0 & 16) != 0) c ^= 0x2a1462b3;
        }
        return c;
    }

    /** Expand a HRP for use in checksum computation. */
    private static byte[] expandHrp(final String hrp) {
        int hrpLength = hrp.length();
        byte ret[] = new byte[hrpLength * 2 + 1];
        for (int i = 0; i < hrpLength; ++i) {
            int c = hrp.charAt(i) & 0x7f; // Limit to standard 7-bit ASCII
            ret[i] = (byte) ((c >>> 5) & 0x07);
            ret[i + hrpLength + 1] = (byte) (c & 0x1f);
        }
        ret[hrpLength] = 0;
        return ret;
    }

    /** Verify a checksum. */
    private static boolean verifyChecksum(final String hrp, final byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] combined = new byte[hrpExpanded.length + values.length];
        System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.length);
        System.arraycopy(values, 0, combined, hrpExpanded.length, values.length);
        return polymod(combined) == 1;
    }

    /** Create a checksum. */
    private static byte[] createChecksum(final String hrp, final byte[] values)  {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] enc = new byte[hrpExpanded.length + values.length + 6];
        System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
        System.arraycopy(values, 0, enc, hrpExpanded.length, values.length);
        int mod = polymod(enc) ^ 1;
        byte[] ret = new byte[6];
        for (int i = 0; i < 6; ++i) {
            ret[i] = (byte) ((mod >>> (5 * (5 - i))) & 31);
        }
        return ret;
    }

    /** Encode a Bech32 string. */
    public static String encode(final Bech32Data bech32) {
        return encode(bech32.hrp, bech32.data);
    }

    /** Encode a Bech32 string. */
    public static String encode(String hrp, final byte[] values) {
        checkArgument(hrp.length() >= 1, "Human-readable part is too short");
        checkArgument(hrp.length() <= 83, "Human-readable part is too long");
        hrp = hrp.toLowerCase(Locale.ROOT);
        byte[] checksum = createChecksum(hrp, values);
        byte[] combined = new byte[values.length + checksum.length];
        System.arraycopy(values, 0, combined, 0, values.length);
        System.arraycopy(checksum, 0, combined, values.length, checksum.length);
        StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
        sb.append(hrp);
        sb.append('1');
        for (byte b : combined) {
            sb.append(CHARSET.charAt(b));
        }
        return sb.toString();
    }

    private static int polymodStep (int pre) {
        int b = pre >> 25;
        return ((pre & 0x1FFFFFF) << 5) ^
                (-((b >> 0) & 1) & 0x3b6a57b2) ^
                (-((b >> 1) & 1) & 0x26508e6d) ^
                (-((b >> 2) & 1) & 0x1ea119fa) ^
                (-((b >> 3) & 1) & 0x3d4233dd) ^
                (-((b >> 4) & 1) & 0x2a1462b3);
    }

    private static int prefixChk(String prefix) {
        int chk = 1;
        for (int i = 0; i < prefix.length(); ++i) {
            int c = prefix.charAt(i);
            if (c < 33 || c > 126) throw new RuntimeException();

            chk = polymodStep(chk) ^ (c >> 5);
        }

        chk = polymodStep(chk);
        for (int i = 0; i < prefix.length(); ++i) {
            int v = prefix.charAt(i);
            chk = polymodStep(chk) ^ (v & 0x1f);
        }
        return chk;
    }

    public static String encode(String prefix, int[] words) {
        int LIMIT = 90;

        if ((prefix.length() + 7 + words.length) > LIMIT) throw new RuntimeException();

        prefix = prefix.toLowerCase();

        // determine chk mod
        int chk = prefixChk(prefix);
        String result = prefix + '1';
        for (int i = 0; i < words.length; ++i) {
            int x = words[i];
            if ((x >> 5) != 0) throw new RuntimeException();

            chk = polymodStep(chk) ^ x;
            result += CHARSET.charAt(x);
        }

        for (int i = 0; i < 6; ++i) {
            chk = polymodStep(chk);
        }
        chk ^= 1;

        for (int i = 0; i < 6; ++i) {
            int v = (chk >> ((5 - i) * 5)) & 0x1f;
            result += CHARSET.charAt(v);
        }
        return result;
    }

    private static int[] convert(byte[] data, int inBits, int outBits, boolean pad) {
        int value = 0;
        int bits = 0;
        int maxV = (1 << outBits) - 1;

        List<Integer> result = new ArrayList<>();

        int[] ints = convertToIntArray(data);
        for (int i = 0; i < ints.length; ++i) {
            value = (value << inBits) | ints[i];
            bits += inBits;

            while (bits >= outBits) {
                bits -= outBits;
                result.add((value >> bits) & maxV);
            }
        }

        if (pad) {
            if (bits > 0) {
                result.add(((value << (outBits - bits)) & maxV));
            }
        }

        int size = result.size();
        int[] resultArray = new int[size];
        Integer[] temp = result.toArray(new Integer[size]);
        for (int n = 0; n < size; ++n) {
            resultArray[n] = temp[n];
        }
        return resultArray;
    }

    public static int[] toWords(byte[] bytes) {
        return convert(bytes, 8, 5, true);
    }

    private static int[] convertToIntArray(byte[] input)
    {
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++)
        {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }
}