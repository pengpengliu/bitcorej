# bitcorej
Java library for cryptocurrency address generation and transaction creation/signing.

Usage
-------

```java
ChainStateProxy provider = new ChainStateProxy("btc", "main");
KeyPair key = provider.generateKeyPair();
List<String> keys = Collections.singletonList(key.getPrivate());
String signedTx = state.signRawTransaction("<---Transaction JSON Struct--->", keys);
System.out.println(signedTx);
```
Supported Cryptocurrencies
-------
- [x] BTC
- [x] BCH
- [x] BSV
- [x] OmniLayer
- [x] ETH
- [x] ERC20
- [x] EOS
- [x] ABBC
- [x] YTA
- [x] XRP
- [x] XLM
- [x] LTC
- [x] DASH
- [x] ZEC
- [x] ZCL
- [x] DOGE
- [x] QTUM
- [x] ATOM
- [x] IRIS
- [x] NAS
- [x] ONT
- [x] BHD
- [x] VET
- [x] IOST
- [x] VSYS
- [x] PI
- [x] NAKA
- [x] GXC
- [x] TRX
- [x] Binance Chain
- [x] CENT
- [x] MCH
- [x] XMR
- [x] NRG
- [x] CZZ
- [x] IQ
- [x] LUNA
- [x] CKB
- [x] STG
- [x] PLC
- [x] GRS
- [x] ADA
- [x] XNS
- [x] BIP
- [x] XTZ
- [x] RVN
- [x] RVC
- [x] DIVI
- [x] KIN
- [x] PMEER
- [x] MTR
- [x] SOL
- [x] ADK
- [x] FIO
- [x] FIL
- [x] DOT
