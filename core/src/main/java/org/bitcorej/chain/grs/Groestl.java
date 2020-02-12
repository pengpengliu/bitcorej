package org.bitcorej.chain.grs;

public class Groestl {
    private static final Groestl512 digestGroestl = new Groestl512();

    public static byte[] digest(byte[] input, int offset, int length)
    {
        //return groestl(input, offset, length);
        try {
            return groestl(input, offset, length);
            /*byte[] r1 = groestld_native(input, offset, length);
            byte[] r2 = groestl(input, offset, length);
            if(r1.equals(r2))
            {
                int x = 0;
                ++x;
            }
            return r2;*/
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }
    }

    static byte [] groestl(byte header[])
    {
        //digestGroestl.reset();
        //byte [] hash512 = digestGroestl.digest(header);
        //digestGroestl.reset();
        //byte [] doubleHash512 = digestGroestl.digest(hash512);
        //Initialize
        //return new Sha512Hash(doubleHash512).trim256().getBytes();

        Groestl512 hasher1 = new Groestl512();
        Groestl512 hasher2 = new Groestl512();

        /*digestGroestl.reset();
        byte [] hash512 = digestGroestl.digest(header);
        //digestGroestl.reset();
        byte [] doubleHash512 = digestGroestl.digest(hash512);
        //Initialize
        return new Sha512Hash(doubleHash512).trim256().getBytes();
        */
        byte [] hash1 = hasher1.digest(header);
        byte [] hash2 = hasher2.digest(hash1);
        return new Sha512Hash(hash2).trim256().getBytes();
    }

    public static byte[] digest(byte[] input) {
        //long start = System.currentTimeMillis();
        try {
            return groestl(input);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }

        //return groestl(input);
    }


    static byte [] groestl(byte header[], int offset, int length)
    {
        digestGroestl.reset();
        digestGroestl.update(header, offset, length);
        byte [] hash512 = digestGroestl.digest();

        //digestGroestl.update(hash512);
        Sha512Hash doubleHash512 = new Sha512Hash(digestGroestl.digest(hash512));
        //Initialize

        return doubleHash512.trim256().getBytes();
    }
}
