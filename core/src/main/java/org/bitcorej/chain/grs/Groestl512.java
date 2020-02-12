package org.bitcorej.chain.grs;

public class Groestl512 extends GroestlBigCore {

    /**
     * Create the engine.
     */
    public Groestl512()
    {
        super();
    }

    /** @see Digest */
    public int getDigestLength()
    {
        return 64;
    }

    /** @see Digest */
    public Digest copy()
    {
        return copyState(new Groestl512());
    }
}