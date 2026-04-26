# bitcorej

Java library for cryptocurrency address generation and transaction creation/signing.

## Project Status

This is an old project that has not been actively maintained for a long time.
Many supported blockchains have upgraded their protocols, transaction formats,
address rules, fee models, or signing requirements since this code was written.
Parts of this library may no longer be applicable to current mainnet rules.

Before using this project in production or with real funds, verify every target
chain against current network rules and add transaction-signing test vectors for
the exact use case.

## What It Provides

- Key pair generation for multiple blockchain families.
- Raw transaction creation and signing helpers.
- UTXO helpers for Bitcoin-like chains.
- Token transaction helpers for selected account-based chains.
- Example programs under the `examples` module.

## Repository Layout

- `core`: library source code and tests.
- `examples`: runnable examples that depend on `core`.
- `gradle/wrapper`: Gradle wrapper used for reproducible builds.

## Requirements

- JDK 8+ source compatibility.
- Gradle Wrapper is included, so a local Gradle installation is not required.

The project has recently been verified with JDK 17 and Gradle 7.6.4.

## Build And Test

```bash
./gradlew test --no-daemon
```

## Basic Usage

```java
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.chain.KeyPair;

import java.util.Collections;
import java.util.List;

ChainStateProxy provider = new ChainStateProxy("btc", "main");
KeyPair key = provider.generateKeyPair();

List<String> keys = Collections.singletonList(key.getSecret());
String signedTx = provider.signRawTransaction("<transaction-json>", keys);

System.out.println(key.getPublic());
System.out.println(signedTx);
```

## ERC20 Usage

ERC20 support is initialized through the Ethereum provider constructor with a
contract address and token decimals:

```java
String contractAddress = "0x...";
String tokenDecimals = "18";

ChainStateProxy provider = new ChainStateProxy("eth", contractAddress, tokenDecimals);
```

## Historical Support List

The following list reflects the chains this project was originally built to
support. It is not a guarantee of compatibility with current network versions.

| Family | Chains |
| --- | --- |
| Bitcoin / UTXO-like | BTC, BCH, BSV, OmniLayer, LTC, DASH, ZEC, ZCL, DOGE, QTUM, BHD, NRG, CZZ, IQ, PLC, GRS, RVN, RVC, DIVI, PMEER, MTR |
| Ethereum / EVM-like | ETH, ERC20 |
| EOS-like | EOS, ABBC, YTA, FIO |
| Cosmos-like | ATOM, IRIS, Binance Chain, LUNA, AKT |
| Account-based / other | XRP, XLM, NAS, ONT, VET, IOST, VSYS, PI, NAKA, GXC, TRX, CENT, MCH, XMR, ADA, XNS, BIP, XTZ, SOL, ADK, FIL, DOT, CKB, STG, KIN |

## Security Notes

- Do not log private keys, mnemonics, raw secrets, or unsigned transaction payloads
  that contain sensitive metadata.
- Always validate generated addresses and signed transactions against independent
  chain-specific test vectors.
- Treat the existing examples as integration references, not production-ready
  wallet flows.
- For any chain that changed signing rules, replay protection, fee calculation,
  address format, or token contract ABI, update the implementation before use.

## License

No license file is currently included in this repository.
