package org.bitcorej.chain;

import org.bitcorej.chain.erc20.ERC20StateProvider;
import org.bitcorej.chain.gleec2.GLEEC2StateProvider;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class CoreSafetyTest {
    private static final String FROM = "0x2222222222222222222222222222222222222222";
    private static final String TO = "0x1111111111111111111111111111111111111111";
    private static final String CONTRACT_ONE = "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String CONTRACT_TWO = "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

    @Test
    public void transactionEqualityRejectsSubsetOutputs() {
        Transaction decoded = new Transaction("{\"from\":[{\"address\":\"from\",\"amount\":\"1\"}],\"to\":[{\"address\":\"to1\",\"amount\":\"0.4\"},{\"address\":\"to2\",\"amount\":\"0.5\"}],\"fee\":\"0.1\"}");
        Transaction request = new Transaction("{\"from\":[{\"address\":\"from\",\"amount\":\"1\"}],\"to\":[{\"address\":\"to1\",\"amount\":\"0.4\"}],\"fee\":\"0.1\"}");

        assertFalse(decoded.equals(request));
    }

    @Test
    public void keyPairToStringDoesNotExposeSecret() {
        KeyPair keyPair = new KeyPair("secret-value", "public-value");

        assertEquals("public-value", keyPair.toString());
        assertFalse(keyPair.toString().contains("secret-value"));
    }

    @Test
    public void erc20BuildsAbiTransferWithEthGasUnits() {
        ERC20StateProvider provider = new ERC20StateProvider(CONTRACT_ONE, 18);
        String raw = provider.buildTranscation(FROM, TO, new BigDecimal("1"), 21000, new BigDecimal("0.00000002"), 7);
        JSONObject json = new JSONObject(raw);

        String expectedAmount = NumericUtil.bigIntegerToHexWithZeroPadded(new BigInteger("1000000000000000000"), 64);
        assertEquals(CONTRACT_ONE, json.getString("to"));
        assertEquals("0x4a817c800", json.getString("gasPrice"));
        assertEquals("0xa9059cbb000000000000000000000000" + NumericUtil.cleanHexPrefix(TO) + expectedAmount, json.getString("data"));
        assertEquals(138, json.getString("data").length());
    }

    @Test
    public void erc20DecodeUsesEthFeeUnits() {
        ERC20StateProvider provider = new ERC20StateProvider(CONTRACT_ONE, 18);
        Transaction decoded = provider.decodeRawTransaction(provider.buildTranscation(FROM, TO, new BigDecimal("1"), 21000, new BigDecimal("0.00000002"), 7));

        assertEquals(0, decoded.getFee().compareTo(new BigDecimal("0.00042")));
    }

    @Test
    public void erc20ProxyInstancesDoNotShareMutableTokenConfig() throws Exception {
        ChainStateProxy first = new ChainStateProxy("ETH", CONTRACT_ONE, "6");
        ChainStateProxy second = new ChainStateProxy("ETH", CONTRACT_TWO, "18");

        assertTrue(first.getProvider() instanceof ERC20StateProvider);
        assertTrue(second.getProvider() instanceof ERC20StateProvider);
        assertNotSame(first.getProvider(), second.getProvider());
    }

    @Test
    public void chainRegistryUsesExpectedProviders() throws Exception {
        assertTrue(new ChainStateProxy("GLEEC2", "MAIN").getProvider() instanceof GLEEC2StateProvider);
        assertNotNull(new ChainStateProxy("ZCL", "MAIN").getProvider());
    }

    @Test
    public void adaOnlyOverloadsDoNotCastNonAdaProviders() throws Exception {
        ChainStateProxy btc = new ChainStateProxy("BTC", "MAIN");

        assertNull(btc.encodeTransaction(null, null, null, null, 1L));
        assertNull(btc.encodeTransaction(null, null, null, null, BigDecimal.ONE, 1L));
    }
}
