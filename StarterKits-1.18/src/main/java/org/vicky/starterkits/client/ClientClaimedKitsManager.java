package org.vicky.starterkits.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientClaimedKitsManager {
    public static final ClientClaimedKitsManager INSTANCE = new ClientClaimedKitsManager();
    private final Set<String> claimedKits = new HashSet<>();

    public void updateClaimed(List<String> kits) {
        claimedKits.clear();
        claimedKits.addAll(kits);
    }

    public boolean isClaimed(String kitName) {
        return claimedKits.contains(kitName);
    }

    public void clear() {
        claimedKits.clear();
    }
}
