package org.vicky.starterkits.logic;

import net.minecraft.nbt.CompoundTag;
import java.util.HashSet;
import java.util.Set;

public class ClaimedKitsStorage {
    private final Set<String> claimedKits = new HashSet<>();
    private boolean hasGottenFirstJoinKit = false;

    public boolean hasClaimed(String kitName) {
        return claimedKits.contains(kitName);
    }

    public void claimKit(String kitName) {
        claimedKits.add(kitName);
    }

    public void setHasGottenFirstJoinKit(boolean hasGottenFirstJoinKit) {
        this.hasGottenFirstJoinKit = hasGottenFirstJoinKit;
    }

    public boolean hasGottenFirstJoinKit() {
        return hasGottenFirstJoinKit;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        int index = 0;
        for (String kit : claimedKits) {
            tag.putString("Kit_" + index++, kit);
        }
        tag.putInt("Count", claimedKits.size());
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        claimedKits.clear();
        int count = tag.getInt("Count");
        for (int i = 0; i < count; i++) {
            claimedKits.add(tag.getString("Kit_" + i));
        }
    }

    public Set<String> getClaimedKits() {
        return new HashSet<>(claimedKits);
    }
}
