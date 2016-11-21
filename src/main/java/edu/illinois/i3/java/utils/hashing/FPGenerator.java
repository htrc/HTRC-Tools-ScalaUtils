package edu.illinois.i3.java.utils.hashing;

import java.util.Hashtable;

/**
 * <p>
 * This class provides methods that construct fingerprints of strings of bytes
 * via operations in <i>GF[2^d]</i> for <i>0 < d <= 64</i>. <i>GF[2^d]</i> is
 * represented as the set of polynomials of degree <i>d</i> with coefficients
 * in <i>Z(2)</i>, modulo an irreducible polynomial <i>P</i> of degree <i>d</i>.
 * The representation of polynomials is as an unsigned binary number in which
 * the least significant exponent is kept in the most significant bit.
 * <p>
 * Let S be a string of bytes and <i>g(S)</i> the string obtained by taking the
 * byte <code>0x01</code> followed by eight <code>0x00</code> bytes followed
 * by <code>S</code>. Let <i>f(S)</i> be the polynomial associated to the
 * string <i>S</i> viewed as a polynomial with coefficients in the field
 * <i>Z(2)</i>. The fingerprint of S is simply the value <i>f(g(S))</i> modulo
 * <i>P</i>. Because polynomials are represented with the least significant
 * coefficient in the most significant bit, fingerprints of degree <i>d</i> are
 * stored in the <code>d</code> <strong>most</code> significant bits of a
 * long word.
 * <p>
 * Fingerprints can be used as a probably unique id for the input string. More
 * precisely, if <i>P</i> is chosen at random among irreducible polynomials of
 * degree <i>d</i>, then the probability that any two strings <i>A</i> and
 * <i>B</i> have the same fingerprint is less than <i>max(|A|,|B|)/2^(d+1)</i>
 * where <i>|A|</i> is the length of A in bits.
 * <p>
 * The routines named <code>extend[8]</code> and <code>fp[8]</code> return
 * reduced results, while <code>extend_[byte/char/int/long]</code> do not. An
 * <em>un</em>reduced result is a number that is equal (mod </code>polynomial</code>
 * to the desired fingerprint but may have degree <code>degree</code> or
 * higher. The method <code>reduce</code> reduces such a result to a
 * polynomial of degree less than <code>degree</code>. Obtaining reduced
 * results takes longer than obtaining unreduced results; thus, when
 * fingerprinting long strings, it's better to obtain irreduced results inside
 * the fingerprinting loop and use <code>reduce</code> to reduce to a
 * fingerprint after the loop.
 *
 * Source: http://www.crawler.archive.org/apidocs/st/ata/util/FPGenerator.html
 */

public final class FPGenerator {

    /**
     * Return a fingerprint generator. The fingerprints generated will have
     * degree <code>degree</code> and will be generated by
     * <code>polynomial</code>. If a generator based on
     * <code>polynomial</code> has already been created, it will be returned.
     * Requires that <code>polynomial</code> is an irreducible polynomial of
     * degree <code>degree</code> (the array <code>polynomials</code>
     * contains some irreducible polynomials).
     */
    public static FPGenerator make(long polynomial, int degree) {
        Long l = polynomial;
        FPGenerator fpgen = generators.get(l);
        if (fpgen == null) {
            fpgen = new FPGenerator(polynomial, degree);
            generators.put(l, fpgen);
        }

        return fpgen;
    }

    private static final Hashtable<Long,FPGenerator> generators = new Hashtable<>(10);

    private static final long zero = 0;

    private static final long one = 0x8000000000000000L;

    /**
     * Return a value equal (mod <code>polynomial</code>) to <code>fp</code>
     * and of degree less than <code>degree</code>.
     */
    public long reduce(long fp) {
        int N = (8 - degree / 8);
        long local = (N == 8 ? 0 : fp & (-1L << 8 * N));
        long temp = zero;
        for (int i = 0; i < N; i++) {
            temp ^= ByteModTable[8 + i][((int) fp) & 0xff];
            fp >>>= 8;
        }

        return local ^ temp;
    }

    /**
     * Extends <code>f</code> with lower eight bits of <code>v</code> with<em>out</em>
     * full reduction. In other words, returns a polynomial that is equal (mod
     * <code>polynomial</code>) to the desired fingerprint but may be of
     * higher degree than the desired fingerprint.
     */
    public long extend_byte(long f, int v) {
        f ^= (0xff & v);
        int i = (int) f;
        long result = (f >>> 8);
        result ^= ByteModTable[7][i & 0xff];

        return result;
    }

    /**
     * Extends <code>f</code> with lower sixteen bits of <code>v</code>.
     * Does not reduce.
     */
    public long extend_char(long f, int v) {
        f ^= (0xffff & v);
        int i = (int) f;
        long result = (f >>> 16);
        result ^= ByteModTable[6][i & 0xff];
        i >>>= 8;
        result ^= ByteModTable[7][i & 0xff];

        return result;
    }

    /**
     * Extends <code>f</code> with (all bits of) <code>v</code>. Does not
     * reduce.
     */
    public long extend_int(long f, int v) {
        f ^= (0xffffffffL & v);
        int i = (int) f;
        long result = (f >>> 32);
        result ^= ByteModTable[4][i & 0xff];
        i >>>= 8;
        result ^= ByteModTable[5][i & 0xff];
        i >>>= 8;
        result ^= ByteModTable[6][i & 0xff];
        i >>>= 8;
        result ^= ByteModTable[7][i & 0xff];

        return result;
    }

    /**
     * Extends <code>f</code> with <code>v</code>. Does not reduce.
     */
    public long extend_long(long f, long v) {
        f ^= v;
        long result = ByteModTable[0][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[1][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[2][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[3][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[4][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[5][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[6][(int) (f & 0xff)];
        f >>>= 8;
        result ^= ByteModTable[7][(int) (f & 0xff)];

        return result;
    }

    /**
     * Compute fingerprint of "n" bytes of "buf" starting from "buf[start]".
     * Requires "[start, start+n)" is in bounds.
     */
    public long fp(byte[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    /**
     * Compute fingerprint of (all bits of) "n" characters of "buf" starting
     * from "buf[i]". Requires "[i, i+n)" is in bounds.
     */
    public long fp(char[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    // COMMENTED OUT TO REMOVE Dependency on st.ata.util.Text
    // /** Compute fingerprint of (all bits of) <code>t</code> */
    // public long fp(Text t) {
    // return extend(empty, t);
    // }
    /** Compute fingerprint of (all bits of) the characters of "s". */
    public long fp(String s) {
        return extend(empty, s);
    }

    /**
     * Compute fingerprint of (all bits of) "n" characters of "buf" starting
     * from "buf[i]". Requires "[i, i+n)" is in bounds.
     */
    public long fp(int[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    /**
     * Compute fingerprint of (all bits of) "n" characters of "buf" starting
     * from "buf[i]". Requires "[i, i+n)" is in bounds.
     */
    public long fp(long[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    /**
     * Compute fingerprint of the lower eight bits of the characters of "s".
     */
    public long fp8(String s) {
        return extend8(empty, s);
    }

    /**
     * Compute fingerprint of the lower eight bits of "n" characters of "buf"
     * starting from "buf[i]". Requires "[i, i+n)" is in bounds.
     */
    public long fp8(char[] buf, int start, int n) {
        return extend8(empty, buf, start, n);
    }

    /**
     * Extends fingerprint <code>f</code> by adding the low eight bits of "b".
     */
    public long extend(long f, byte v) {
        return reduce(extend_byte(f, v));
    }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) "v".
     */
    public long extend(long f, char v) {
        return reduce(extend_char(f, v));
    }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) "v".
     */
    public long extend(long f, int v) {
        return reduce(extend_int(f, v));
    }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) "v".
     */
    public long extend(long f, long v) {
        return reduce(extend_long(f, v));
    }

    /**
     * Extends fingerprint <code>f</code> by adding "n" bytes of "buf"
     * starting from "buf[start]". Result is reduced. Requires "[i,&nbsp;i+n)"
     * is in bounds.
     */
    public long extend(long f, byte[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_byte(f, buf[start + i]);
        }

        return reduce(f);
    }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) "n"
     * characters of "buf" starting from "buf[i]". Result is reduced. Requires
     * "[i,&nbsp;i+n)" is in bounds.
     */
    public long extend(long f, char[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_char(f, buf[start + i]);
        }

        return reduce(f);
    }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) the
     * characters of "s". Result is reduced.
     */
    public long extend(long f, String s) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            int v = (int) s.charAt(i);
            f = extend_char(f, v);
        }

        return reduce(f);
    }

    // COMMENTED OUT TO REMOVE Dependency on st.ata.util.Text
    // /** Extends fingerprint <code>f</code> by adding (all bits of)
    // * <code>t</code> */
    // public long extend(long f, Text t) {
    // return extend(f, t.buf, t.start, t.length());
    // }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) "n"
     * characters of "buf" starting from "buf[i]". Result is reduced. Requires
     * "[i,&nbsp;i+n)" is in bounds.
     */
    public long extend(long f, int[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_int(f, buf[start + i]);
        }

        return reduce(f);
    }

    /**
     * Extends fingerprint <code>f</code> by adding (all bits of) "n"
     * characters of "buf" starting from "buf[i]". Result is reduced. Requires
     * "[i,&nbsp;i+n)" is in bounds.
     */
    public long extend(long f, long[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_long(f, buf[start + i]);
        }

        return reduce(f);
    }

    /**
     * Extends fingerprint <code>f</code> by adding the lower eight bits of
     * the characters of "s". Result is reduced.
     */
    public long extend8(long f, String s) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            int x = (int) s.charAt(i);
            f = extend_byte(f, x);
        }

        return reduce(f);
    }

    /**
     * Extends fingerprint <code>f</code> by adding the lower eight bits of
     * "n" characters of "buf" starting from "buf[i]". Result is reduced.
     * Requires "[i, i+n)" is in bounds.
     */
    public long extend8(long f, char[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_byte(f, buf[start + i]);
        }

        return reduce(f);
    }

    /** Fingerprint of the empty string of bytes. */
    public final long empty;

    /**
     * The number of bits in fingerprints generated by <code>this</code>.
     */
    public final int degree;

    /**
     * The polynomial used by <code>this</code> to generate fingerprints.
     */
    public long polynomial;

    /**
     * Result of reducing certain polynomials. Specifically, if
     * <code>f(S)</code> is bit string <code>S</code> interpreted as a
     * polynomial, <code>f(ByteModTable[i][j])</code> equals
     * <code>mod(x^(127&nbsp;-&nbsp;8*i)&nbsp;*&nbsp;f(j),&nbsp;P)</code>.
     */
    private long[][] ByteModTable;

    /**
     * Create a fingerprint generator. The fingerprints generated will have
     * degree <code>degree</code> and will be generated by
     * <code>polynomial</code>. Requires that <code>polynomial</code> is an
     * irreducible polynomial of degree <code>degree</code> (the array
     * <code>polynomials</code> contains some irreducible polynomials).
     */
    private FPGenerator(long polynomial, int degree) {
        this.degree = degree;
        this.polynomial = polynomial;
        ByteModTable = new long[16][256];

        long[] PowerTable = new long[128];

        long x_to_the_i = one;
        long x_to_the_degree_minus_one = (one >>> (degree - 1));
        for (int i = 0; i < 128; i++) {
            // Invariants:
            // x_to_the_i = mod(x^i, polynomial)
            // forall 0 <= j < i, PowerTable[i] = mod(x^i, polynomial)
            PowerTable[i] = x_to_the_i;
            boolean overflow = ((x_to_the_i & x_to_the_degree_minus_one) != 0);
            x_to_the_i >>>= 1;
            if (overflow) {
                x_to_the_i ^= polynomial;
            }
        }
        this.empty = PowerTable[64];

        for (int i = 0; i < 16; i++) {
            // Invariant: forall 0 <= i' < i, forall 0 <= j' < 256,
            // ByteModTable[i'][j'] = mod(x^(127 - 8*i') * f(j'), polynomial)
            for (int j = 0; j < 256; j++) {
                // Invariant: forall 0 <= i' < i, forall 0 <= j' < j,
                // ByteModTable[i'][j'] = mod(x^(degree+i')*f(j'),polynomial)
                long v = zero;
                for (int k = 0; k < 8; k++) {
                    // Invariant:
                    // v = mod(x^(degree+i) * f(j & ((1<<k)-1)), polynomial)
                    if ((j & (1 << k)) != 0) {
                        v ^= PowerTable[127 - i * 8 - k];
                    }
                }
                ByteModTable[i][j] = v;
            }
        }
    }

    /**
     * Array of irreducible polynomials. For each degree <code>d</code>
     * between 1 and 64 (inclusive), <code>polynomials[d][i]</code> is an
     * irreducible polynomial of degree <code>d</code>. There are at least
     * two irreducible polynomials for each degree.
     */
    private static final long polynomials[][] = { null,
            { 0xC000000000000000L, 0xC000000000000000L },
            { 0xE000000000000000L, 0xE000000000000000L },
            { 0xD000000000000000L, 0xB000000000000000L },
            { 0xF800000000000000L, 0xF800000000000000L },
            { 0xEC00000000000000L, 0xBC00000000000000L },
            { 0xDA00000000000000L, 0xB600000000000000L },
            { 0xE500000000000000L, 0xE500000000000000L },
            { 0x9680000000000000L, 0xD480000000000000L },
            { 0x80C0000000000000L, 0x8840000000000000L },
            { 0xB0A0000000000000L, 0xE9A0000000000000L },
            { 0xD9F0000000000000L, 0xC9B0000000000000L },
            { 0xE758000000000000L, 0xDE98000000000000L },
            { 0xE42C000000000000L, 0x94E4000000000000L },
            { 0xD4CE000000000000L, 0xB892000000000000L },
            { 0xE2AB000000000000L, 0x9E39000000000000L },
            { 0xCCE4800000000000L, 0x9783800000000000L },
            { 0xD8F8C00000000000L, 0xA9CDC00000000000L },
            { 0x9A28200000000000L, 0xFD79E00000000000L },
            { 0xC782500000000000L, 0x96CD300000000000L },
            { 0xBEE6880000000000L, 0xE902C80000000000L },
            { 0x86D7E40000000000L, 0xF066340000000000L },
            { 0x9888060000000000L, 0x910ABE0000000000L },
            { 0xFF30E30000000000L, 0xB27EFB0000000000L },
            { 0x8E375B8000000000L, 0xA03D948000000000L },
            { 0xD1415C4000000000L, 0xF5357CC000000000L },
            { 0x91A916E000000000L, 0xB6CE66E000000000L },
            { 0xE6D2FC5000000000L, 0xD55882B000000000L },
            { 0x9A3BA0B800000000L, 0xFBD654E800000000L },
            { 0xAEA5D2E400000000L, 0xF0E533AC00000000L },
            { 0xDA88B7BE00000000L, 0xAA3AAEDE00000000L },
            { 0xBA75BB4300000000L, 0xF5A811C500000000L },
            { 0x9B6C9A2F80000000L, 0x9603CCED80000000L },
            { 0xFABB538840000000L, 0xE2747090C0000000L },
            { 0x8358898EA0000000L, 0x8C698D3D20000000L },
            { 0xDA8ABD5BF0000000L, 0xF6DF3A0AF0000000L },
            { 0xB090C3F758000000L, 0xD3B4D3D298000000L },
            { 0xAD9882F5BC000000L, 0x88DA4FB544000000L },
            { 0xC3C366272A000000L, 0xDCCF2A2262000000L },
            { 0x9BC0224A97000000L, 0xAF5D96F273000000L },
            { 0x8643FFF621800000L, 0x8E390C6EDC800000L },
            { 0xE45C01919BC00000L, 0xCBB34C8945C00000L },
            { 0x80D8141BC2E00000L, 0x886AFC3912200000L },
            { 0xF605807C26500000L, 0xA3B92D28F6300000L },
            { 0xCE9A2CFC76280000L, 0x98400C1921280000L },
            { 0xF61894904C040000L, 0xC8BE6DBCEC8C0000L },
            { 0xE3A44C104D160000L, 0xCA84A59443760000L },
            { 0xC7E84953A11B0000L, 0xD9D4F6AA02CB0000L },
            { 0xC26CDD1C9A358000L, 0x8BE8478434328000L },
            { 0xAE125DBEB198C000L, 0xFCC5DBFD5AAAC000L },
            { 0x86DE52A079A6A000L, 0xC5F16BD883816000L },
            { 0xDF82486AAFE37000L, 0xA293EC735692D000L },
            { 0xE91ABA275C272800L, 0xD686192874E3C800L },
            { 0x963D0960DAB3FC00L, 0xBA9DE62072621400L },
            { 0xA2188C4E8A46CE00L, 0xD31F75BCB4977E00L },
            { 0xC43A416020A6CB00L, 0x99F57FECA12B3900L },
            { 0xA4F72EF82A58AE80L, 0xCECE4391B81DA380L },
            { 0xB39F119264BC0940L, 0x80A277D20DABB9C0L },
            { 0xFD6616C0CBFA0B20L, 0xED16E64117DC11A0L },
            { 0xFFA8BC44327B5390L, 0xEDFB13DB3B66C210L },
            { 0xCAE8EB99E73AB548L, 0xC86135B6EA2F0B98L },
            { 0xBA49BADCDD19B16CL, 0x8F1944AFB18564C4L },
            { 0xECFC86D765EABBEEL, 0x9190E1C46CC13702L },
            { 0xE1F8D6B3195D6D97L, 0xDF70267FF5E4C979L },
            { 0xD74307D3FD3382DBL, 0x9999B3FFDC769B48L } };

    /**
     * The standard 64-bit fingerprint generator using
     * <code>polynomials[0][64]</code>.
     */
    public static final FPGenerator std64 = make(polynomials[64][0], 64);

    /**
     * A standard 32-bit fingerprint generator using
     * <code>polynomials[0][32]</code>.
     */
    public static final FPGenerator std32 = make(polynomials[32][0], 32);
}