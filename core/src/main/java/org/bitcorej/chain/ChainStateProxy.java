package org.bitcorej.chain;

import org.bitcorej.chain.ada.CardanoStateProvider;
import org.bitcorej.chain.adk.ADKStateProvider;
import org.bitcorej.chain.akt.AKTStateProvider;
import org.bitcorej.chain.ask.ASKStateProvider;
import org.bitcorej.chain.bch.BCHStateProvider;
import org.bitcorej.chain.bhd.BHDStateProvider;
import org.bitcorej.chain.binance.BinanceStateProvider;
import org.bitcorej.chain.bip.BIPStateProvider;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.chain.bsc.BSCStateProvider;
import org.bitcorej.chain.bsv.BSVStateProvider;
import org.bitcorej.chain.btcv.BTCVStateProvider;
import org.bitcorej.chain.cent.CENTStateProvider;
import org.bitcorej.chain.ckb.CKBStateProvider;
import org.bitcorej.chain.cosmos.CosmosStateProvider;
import org.bitcorej.chain.czz.CZZStateProvider;
import org.bitcorej.chain.dash.DashStateProvider;
import org.bitcorej.chain.divi.DIVIStateProvider;
import org.bitcorej.chain.dogecoin.DogecoinStateProvider;
import org.bitcorej.chain.dot.DOTStateProvider;
import org.bitcorej.chain.eos.ABBCStateProvider;
import org.bitcorej.chain.eos.EOSStateProvider;
import org.bitcorej.chain.eos.FIOStateProvider;
import org.bitcorej.chain.eos.YTAStateProvider;
import org.bitcorej.chain.erc20.ERC20StateProvider;
import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.chain.fil.FILStateProvider;
import org.bitcorej.chain.gleec.GLEECStateProvider;
import org.bitcorej.chain.grs.GRSStateProvider;
import org.bitcorej.chain.gxc.GXCStateProvider;
import org.bitcorej.chain.iost.IOSTStateProvider;
import org.bitcorej.chain.iq.IQStateProvider;
import org.bitcorej.chain.iris.IRISStateProvider;
import org.bitcorej.chain.ltc.LitecoinStateProvider;
import org.bitcorej.chain.luna.LUNAStateProvider;
import org.bitcorej.chain.mch.MCHStateProvider;
import org.bitcorej.chain.meta.METAStateProvider;
import org.bitcorej.chain.mtr.MTRStateProvider;
import org.bitcorej.chain.naka.NAKAStateProvider;
import org.bitcorej.chain.nas.NASStateProvider;
import org.bitcorej.chain.nrg.NRGStateProvider;
import org.bitcorej.chain.ong.ONGStateProvider;
import org.bitcorej.chain.ont.ONTStateProvider;
import org.bitcorej.chain.pi.PIStateProvider;
import org.bitcorej.chain.plc.PLCStateProvider;
import org.bitcorej.chain.pmeer.PMEERStateProvider;
import org.bitcorej.chain.poc.POCStateProvider;
import org.bitcorej.chain.qtum.QTUMStateProvider;
import org.bitcorej.chain.ripple.RippleStateProvider;
import org.bitcorej.chain.rvc.RVCStateProvider;
import org.bitcorej.chain.rvn.RVNStateProvider;
import org.bitcorej.chain.sol.SOLStateProvider;
import org.bitcorej.chain.sprk.SPRKStateProvider;
import org.bitcorej.chain.stellar.KINStateProvider;
import org.bitcorej.chain.stellar.StellarStateProvider;
import org.bitcorej.chain.stg.STGStateProvider;
import org.bitcorej.chain.trx.TRXStateProvider;
import org.bitcorej.chain.umi.UMIStateProvider;
import org.bitcorej.chain.usdt.USDTStateProvider;
import org.bitcorej.chain.vet.VETStateProvider;
import org.bitcorej.chain.vrsc.VRSCStateProvider;
import org.bitcorej.chain.vsys.VSYSStateProvider;
import org.bitcorej.chain.west.WESTStateProvider;
import org.bitcorej.chain.xmr.XMRStateProvider;
import org.bitcorej.chain.xns.XNSStateProvider;
import org.bitcorej.chain.xtz.XTZStateProvider;
import org.bitcorej.chain.zcash.ZcashStateProvider;
import org.bitcorej.chain.zcl.ZCLStateProvider;
import org.bitcorej.chain.zen.ZENStateProvider;
import org.bitcorej.core.Network;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class ChainStateProxy implements ChainState, UTXOState, USDTState, XMRState, ADATxBuilderState {
    private static HashMap<String, ChainState> services;

    static
    {
        services = new HashMap<>();
        services.put("BTC_MAIN", new BitcoinStateProvider(Network.MAIN));
        services.put("BTC_TEST", new BitcoinStateProvider(Network.TEST));
        services.put("BCH_MAIN", new BCHStateProvider(Network.MAIN));
        services.put("BCH_TEST", new BCHStateProvider(Network.TEST));
        BSVStateProvider bsv = new BSVStateProvider(Network.MAIN);
        services.put("BSV", bsv);
        services.put("BSV_MAIN", bsv);
        services.put("BSV_TEST", new BSVStateProvider(Network.TEST));
        services.put("USDT_MAIN", new USDTStateProvider(Network.MAIN));
        services.put("USDT_TEST", new USDTStateProvider(Network.TEST));
        services.put("ETH", new EthereumStateProvider());
        services.put("META", new METAStateProvider());
        services.put("ERC20", new ERC20StateProvider());
        services.put("EOS_MAIN", new EOSStateProvider(Network.MAIN));
        services.put("EOS_TEST", new EOSStateProvider(Network.TEST));
        services.put("ABBC", new ABBCStateProvider());
        services.put("YTA", new YTAStateProvider());
        services.put("XRP", new RippleStateProvider());
        services.put("XLM_MAIN", new StellarStateProvider(Network.MAIN));
        services.put("XLM_TEST", new StellarStateProvider(Network.TEST));
        services.put("LTC_MAIN", new LitecoinStateProvider(Network.MAIN));
        services.put("LTC_TEST", new LitecoinStateProvider(Network.TEST));
        services.put("DASH_MAIN", new DashStateProvider(Network.MAIN));
        services.put("DASH_TEST", new DashStateProvider(Network.TEST));
        services.put("ZEC_MAIN", new ZcashStateProvider(Network.MAIN));
        services.put("ZEC_TEST", new ZcashStateProvider(Network.TEST));
        ZCLStateProvider zcl = new ZCLStateProvider(Network.MAIN);
        services.put("ZCL", zcl);
        services.put("ZCL_MAIn", zcl);
        services.put("DOGE_MAIN", new DogecoinStateProvider(Network.MAIN));
        services.put("DOGE_TEST", new DogecoinStateProvider(Network.TEST));
        services.put("QTUM_MAIN", new QTUMStateProvider(Network.MAIN));
        services.put("QTUM_TEST", new QTUMStateProvider(Network.TEST));
        services.put("ATOM", new CosmosStateProvider());
        services.put("IRIS", new IRISStateProvider());
        services.put("NAS", new NASStateProvider());
        services.put("ONT", new ONTStateProvider());
        services.put("ONG", new ONGStateProvider());
        services.put("BHD", new BHDStateProvider(Network.MAIN));
        services.put("BHD_MAIN", new BHDStateProvider(Network.MAIN));
        services.put("BHD_TEST", new BHDStateProvider(Network.TEST));
        services.put("VET", new VETStateProvider(Network.MAIN));
        services.put("VET_MAIN", new VETStateProvider(Network.MAIN));
        services.put("VET_TEST", new VETStateProvider(Network.TEST));
        services.put("IOST", new IOSTStateProvider());
        services.put("VSYS", new VSYSStateProvider());
        services.put("VSYS_MAIN", new VSYSStateProvider(Network.MAIN));
        services.put("VSYS_TEST", new VSYSStateProvider(Network.TEST));
        PIStateProvider pi = new PIStateProvider();
        services.put("PI", pi);
        services.put("PI_MAIN", pi);
        services.put("NAKA", new NAKAStateProvider());
        services.put("GXC", new GXCStateProvider());
        TRXStateProvider trx = new TRXStateProvider();
        services.put("TRX", trx);
        services.put("BTT", trx);
        services.put("BNB", new BinanceStateProvider());
        CENTStateProvider cent = new CENTStateProvider(Network.MAIN);
        services.put("CENT", cent);
        services.put("CENT_MAIN", cent);
        MCHStateProvider mch = new MCHStateProvider(Network.MAIN);
        services.put("MCH", mch);
        services.put("MCH_MAIN", mch);
        services.put("XMR", new XMRStateProvider());
        NRGStateProvider nrg = new NRGStateProvider(Network.MAIN);
        services.put("NRG", nrg);
        services.put("NRG_MAIN", nrg);
        CZZStateProvider czz = new CZZStateProvider(Network.MAIN);
        services.put("CZZ", czz);
        services.put("CZZ_MAIN", czz);
        IQStateProvider iq = new IQStateProvider(Network.MAIN);
        services.put("IQ", iq);
        services.put("IQ_MAIN", iq);
        LUNAStateProvider luna = new LUNAStateProvider();
        services.put("LUNA", luna);
        services.put("LUNA_MAIN", luna);
        CKBStateProvider ckb = new CKBStateProvider();
        services.put("CKB", ckb);
        services.put("CKB_MAIN", ckb);
        STGStateProvider stg = new STGStateProvider();
        services.put("STG", stg);
        services.put("STG_MAIN", stg);
        PLCStateProvider plc = new PLCStateProvider(Network.MAIN);
        services.put("PLC", plc);
        services.put("PLC_MAIN", plc);
        GRSStateProvider grs = new GRSStateProvider(Network.MAIN);
        services.put("GRS", grs);
        services.put("GRS_MAIN", grs);
        CardanoStateProvider ada = new CardanoStateProvider();
        services.put("ADA", ada);
        services.put("ADA_MAIN", ada);
        XNSStateProvider xns = new XNSStateProvider();
        services.put("XNS", xns);
        services.put("XNS_MAIN", xns);
        BIPStateProvider bip = new BIPStateProvider();
        services.put("BIP", bip);
        services.put("BIP_MAIN", bip);
        XTZStateProvider xtz = new XTZStateProvider();
        services.put("XTZ", xtz);
        services.put("XTZ_MAIN", xtz);
        RVNStateProvider rvn = new RVNStateProvider(Network.MAIN);
        services.put("RVN", rvn);
        services.put("RVN_MAIN", rvn);
        RVCStateProvider rvc = new RVCStateProvider(Network.MAIN);
        services.put("RVC", rvc);
        services.put("RVC_MAIN", rvc);
        DIVIStateProvider divi = new DIVIStateProvider(Network.MAIN);
        services.put("DIVI", divi);
        services.put("DIVI_MAIN", divi);
        KINStateProvider kin = new KINStateProvider(Network.MAIN);
        services.put("KIN", kin);
        services.put("KIN_MAIN", kin);
        PMEERStateProvider pmeer = new PMEERStateProvider(Network.MAIN);
        services.put("PMEER", pmeer);
        services.put("PMEER_MAIN", pmeer);
        MTRStateProvider mtr = new MTRStateProvider();
        services.put("MTR", mtr);
        services.put("MTR_MAIN", mtr);
        SOLStateProvider sol = new SOLStateProvider();
        services.put("SOL", sol);
        services.put("SOL_MAIN", sol);
        ADKStateProvider adk = new ADKStateProvider();
        services.put("ADK", adk);
        services.put("ADK_MAIN", adk);
        FIOStateProvider fio = new FIOStateProvider();
        services.put("FIO", fio);
        services.put("FIO_MAIN", fio);
        FILStateProvider fil = new FILStateProvider();
        services.put("FIL", fil);
        services.put("FIL_MAIN", fil);
        DOTStateProvider dot = new DOTStateProvider();
        services.put("DOT", dot);
        services.put("DOT_MAIN", dot);
        DOTStateProvider wnd = new DOTStateProvider(Network.TEST);
        services.put("WND", wnd);
        services.put("DOT_WND", wnd);
        services.put("ASK", new ASKStateProvider());
        services.put("BSC", new BSCStateProvider());
        BTCVStateProvider btcv = new BTCVStateProvider(Network.MAIN);
        services.put("BTCV", btcv);
        services.put("BTCV_MAIN", btcv);
        VRSCStateProvider vrsc = new VRSCStateProvider(Network.MAIN);
        services.put("VRSC", vrsc);
        services.put("VRSC_MAIN", vrsc);
        ZENStateProvider zen = new ZENStateProvider(Network.MAIN);
        services.put("ZEN", zen);
        services.put("ZEN_MAIN", zen);
        WESTStateProvider west = new WESTStateProvider();
        services.put("WEST", west);
        services.put("WEST_MAIN", west);
        AKTStateProvider akt = new AKTStateProvider();
        services.put("AKT", akt);
        services.put("AKT_MAIN", akt);
        POCStateProvider poc = new POCStateProvider();
        services.put("POC", poc);
        services.put("POC_MAIN", poc);
        GLEECStateProvider gleec = new GLEECStateProvider(Network.MAIN);
        services.put("GLEEC", gleec);
        services.put("GLEEC_MAIN", gleec);
        GLEECStateProvider gleec2 = new GLEECStateProvider(Network.MAIN);
        services.put("GLEEC2", gleec2);
        services.put("GLEEC2_MAIN", gleec2);
        SPRKStateProvider sprk = new SPRKStateProvider(Network.MAIN);
        services.put("SPRK", sprk);
        services.put("SPRK_MAIN", sprk);
        UMIStateProvider umi = new UMIStateProvider();
        services.put("UMI", umi);
        services.put("UMI_MAIN", umi);
    }

    private ChainState provider;

    public ChainStateProxy(ChainState provider) {
        this.provider = provider;
    }

    public ChainState getProvider() {
        return provider;
    }

    public ChainStateProxy(String chain, String network) throws Exception {
        this.provider = services.get(chain.toUpperCase() + "_" + network.toUpperCase());
        if (this.provider == null) {
            throw new Exception("Chain " + chain + " or Network " + network + " doesn't have a ChainState registered");
        }
    }

    public ChainStateProxy(String chain, String...args) throws Exception {
        if (chain.toUpperCase().equals("ETH") && args.length == 2) {
            this.provider = services.get("ERC20");
            ((ERC20StateProvider)this.provider).setAddress(args[0]);
            ((ERC20StateProvider)this.provider).setDecimals(Integer.parseInt(args[1]));
        } else {
            this.provider = services.get(chain.toUpperCase());
        }

        if (this.provider == null) {
            throw new Exception("Chain " + chain + " doesn't have a ChainState registered");
        }
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        return provider.generateKeyPair(secret);
    }

    @Override
    public KeyPair generateKeyPair() {
        return provider.generateKeyPair();
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        return provider.validateTx(rawTx, tx);
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        return provider.signRawTransaction(rawTx, keys);
    }

    @Override
    public String toWIF(String privateKeyHex) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).toWIF(privateKeyHex);
        }
        return null;
    }

    @Override
    public String calcRedeemScript(String segWitAddress) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).calcRedeemScript(segWitAddress);
        }
        return null;
    }

    @Override
    public String calcWitnessScript(String segWitAddress) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).calcWitnessScript(segWitAddress);
        }
        return null;
    }

    @Override
    public String calcSegWitAddress(String legacyAddress) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).calcSegWitAddress(legacyAddress);
        }
        return null;
    }

    @Override
    public String generateP2PKHScript(String address) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).generateP2PKHScript(address);
        }
        return null;
    }

    @Override
    public String signSegWitTransaction(String rawTx, List<String> keys) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).signSegWitTransaction(rawTx, keys);
        }
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).encodeTransaction(utxos, recipients, changeAddress, fee);
        }
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals) {
        if (this.provider instanceof UTXOState) {
            return ((UTXOState) this.provider).encodeTransaction(utxos, recipients, changeAddress, fee, decimals);
        }
        return null;
    }

    @Override
    public List<Recipient> buildRecipients(String from, String to, BigDecimal amount) {
        if (this.provider instanceof USDTState) {
            return ((USDTState) this.provider).buildRecipients(from, to, amount);
        }
        return null;
    }

    @Override
    public List<Recipient> buildRecipients(String from, String to, BigDecimal amount, int propertyId) {
        if (this.provider instanceof USDTState) {
            return ((USDTState) this.provider).buildRecipients(from, to, amount, propertyId);
        }
        return null;
    }

    @Override
    public String generateViewKey(String secret) {
        if (this.provider instanceof XMRState) {
            return ((XMRState) this.provider).generateViewKey(secret);
        }
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, long bestSlot) {
        if (this.provider instanceof UTXOState) {
            return ((ADATxBuilderState) this.provider).encodeTransaction(utxos, recipients, changeAddress, fee, bestSlot);
        }
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals, long bestSlot) {
        if (this.provider instanceof UTXOState) {
            return ((ADATxBuilderState) this.provider).encodeTransaction(utxos, recipients, changeAddress, fee, bestSlot);
        }
        return null;
    }
}
