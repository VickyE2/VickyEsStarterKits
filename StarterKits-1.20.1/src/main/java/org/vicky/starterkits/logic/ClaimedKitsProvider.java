package org.vicky.starterkits.logic;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;

import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClaimedKitsProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<ClaimedKitsStorage> CLAIMED_KITS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private final ClaimedKitsStorage backend = new ClaimedKitsStorage();
    private final LazyOptional<ClaimedKitsStorage> optional = LazyOptional.of(() -> backend);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CLAIMED_KITS_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.saveNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.loadNBT(nbt);
    }
}
