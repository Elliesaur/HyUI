package au.ellie.hyui.assets;

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.setup.AssetFinalize;
import com.hypixel.hytale.protocol.packets.setup.AssetInitialize;
import com.hypixel.hytale.protocol.packets.setup.AssetPart;
import com.hypixel.hytale.protocol.packets.setup.RequestCommonAssetsRebuild;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.io.PacketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// Originally sourced and modified from the SimpleClaims mod: https://github.com/Buuz135/SimpleClaims
/*
MIT License

Copyright (c) 2025 Buuz135

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

public class DynamicImageAsset extends CommonAsset {

    private static final String[] HASHES = {
            "00456c6c696541555f4879554901000000000000000000000000000000000000",
            "00456c6c696541555f4879554902000000000000000000000000000000000000",
            "00456c6c696541555f4879554903000000000000000000000000000000000000",
            "00456c6c696541555f4879554904000000000000000000000000000000000000",
            "00456c6c696541555f4879554905000000000000000000000000000000000000",
            "00456c6c696541555f4879554906000000000000000000000000000000000000",
            "00456c6c696541555f4879554907000000000000000000000000000000000000",
            "00456c6c696541555f4879554908000000000000000000000000000000000000",
            "00456c6c696541555f4879554909000000000000000000000000000000000000",
            "00456c6c696541555f487955490A000000000000000000000000000000000000"
    };
    private static final String[] PATHS = {
            "UI/Custom/Pages/Elements/DynamicImage1.png",
            "UI/Custom/Pages/Elements/DynamicImage2.png",
            "UI/Custom/Pages/Elements/DynamicImage3.png",
            "UI/Custom/Pages/Elements/DynamicImage4.png",
            "UI/Custom/Pages/Elements/DynamicImage5.png",
            "UI/Custom/Pages/Elements/DynamicImage6.png",
            "UI/Custom/Pages/Elements/DynamicImage7.png",
            "UI/Custom/Pages/Elements/DynamicImage8.png",
            "UI/Custom/Pages/Elements/DynamicImage9.png",
            "UI/Custom/Pages/Elements/DynamicImage10.png"
    };
    private static final UUID DEFAULT_PLAYER_UUID = new UUID(0L, 0L);
    private static final Map<UUID, boolean[]> USED_SLOTS = new HashMap<>();
    
    private final byte[] data;
    private final int slotIndex;
    private final UUID playerUuid;
    
    public DynamicImageAsset(byte[] data) {
        this(data, DEFAULT_PLAYER_UUID);
    }

    public DynamicImageAsset(byte[] data, UUID playerUuid) {
        this(data, claimSlot(playerUuid), playerUuid);
    }

    private DynamicImageAsset(byte[] data, int slotIndex, UUID playerUuid) {
        super(PATHS[slotIndex], HASHES[slotIndex], data);
        this.data = data;
        this.slotIndex = slotIndex;
        this.playerUuid = normalizePlayerUuid(playerUuid);
        HyUIPlugin.getLog().logInfo("Dynamic image slot allocated: " + slotIndex + " path=" + PATHS[slotIndex]);
    }
    
    public static CommonAsset empty() {
        return CommonAssetRegistry.getByName(PATHS[PATHS.length - 1]);
    }

    public static CommonAsset empty(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PATHS.length) {
            throw new IllegalArgumentException("Invalid dynamic image slot index: " + slotIndex);
        }
        return CommonAssetRegistry.getByName(PATHS[slotIndex]);
    }

    public String getPath() {
        return PATHS[slotIndex];
    }

    public String getHash() {
        return HASHES[slotIndex];
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    protected CompletableFuture<byte[]> getBlob0() {
        return CompletableFuture.completedFuture(data);
    }
    
    // Copy of CommonAssetModule#sendAssets but adapted to only send 1 asset to a single player
    public static void sendToPlayer(PacketHandler handler, CommonAsset asset) {
        byte[] allBytes = asset.getBlob().join();
        byte[][] parts = ArrayUtil.split(allBytes, 2621440);
        Packet[] packets = new Packet[2 + parts.length];
        packets[0] = new AssetInitialize(asset.toPacket(), allBytes.length);

        for(int partIndex = 0; partIndex < parts.length; ++partIndex) {
            packets[1 + partIndex] = new AssetPart(parts[partIndex]);
        }

        packets[packets.length - 1] = new AssetFinalize();
        handler.write(packets);
        handler.writeNoCache(new RequestCommonAssetsRebuild());
    }

    private static int claimSlot(UUID playerUuid) {
        synchronized (USED_SLOTS) {
            boolean[] usedSlots = getSlots(playerUuid);
            for (int i = usedSlots.length - 1; i >= 0; i--) {
                if (!usedSlots[i]) {
                    usedSlots[i] = true;
                    HyUIPlugin.getLog().logInfo("Claimed dynamic image slot: " + i);
                    return i;
                }
            }
        }
        throw new IllegalStateException("No dynamic image slots available (max 10).");
    }

    private static void releaseSlot(UUID playerUuid, int slotIndex) {
        synchronized (USED_SLOTS) {
            boolean[] usedSlots = getSlots(playerUuid);
            if (slotIndex < 0 || slotIndex >= usedSlots.length) {
                return;
            }
            usedSlots[slotIndex] = false;
            HyUIPlugin.getLog().logInfo("Released dynamic image slot: " + slotIndex);
        }
    }

    public static void releaseSlotIndex(UUID playerUuid, int slotIndex) {
        releaseSlot(playerUuid, slotIndex);
    }

    private static boolean[] getSlots(UUID playerUuid) {
        UUID normalized = normalizePlayerUuid(playerUuid);
        return USED_SLOTS.computeIfAbsent(normalized, key -> new boolean[PATHS.length]);
    }

    private static UUID normalizePlayerUuid(UUID playerUuid) {
        return playerUuid != null ? playerUuid : DEFAULT_PLAYER_UUID;
    }
}
