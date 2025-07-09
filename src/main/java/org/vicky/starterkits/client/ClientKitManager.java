package org.vicky.starterkits.client;

import org.vicky.starterkits.data.Kit;

import java.util.Collections;
import java.util.List;

public class ClientKitManager {
    public static final ClientKitManager INSTANCE = new ClientKitManager();
    public String selectorItemName = "§6Starter Kit";
    public List<String> selectorItemLore = List.of("§7Choose your kit");

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

    public void setSelectorItemNameAndLore(String name, List<String> lore) {

    }
}
