package org.bitcorej.chain.ckb;

import org.nervos.ckb.transaction.ScriptGroup;

import java.util.List;

/** Copyright © 2019 Nervos Foundation. All rights reserved. */
public class ScriptGroupWithPrivateKeys {

    public ScriptGroup scriptGroup;
    public List<String> privateKeys;

    public ScriptGroupWithPrivateKeys(ScriptGroup scriptGroup, List<String> privateKeys) {
        this.scriptGroup = scriptGroup;
        this.privateKeys = privateKeys;
    }
}