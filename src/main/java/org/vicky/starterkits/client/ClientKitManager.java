package org.vicky.starterkits.client;

import org.vicky.starterkits.data.Kit;

import java.util.Collections;
import java.util.List;

public class ClientKitManager {
    public static final ClientKitManager INSTANCE = new ClientKitManager();

    private List<Kit> kits = List.of();

    public void acceptKits(List<Kit> newKits) {
        this.kits = newKits;
    }

    public List<Kit> getKits() {
        return kits;
    }

    public void clearKits() {
        this.kits = Collections.emptyList();
    }
}
