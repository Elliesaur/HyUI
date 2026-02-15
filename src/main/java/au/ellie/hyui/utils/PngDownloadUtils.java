/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.utils;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.assets.DynamicImageAsset;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class PngDownloadUtils {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final long CACHE_TTL_MS = 15_000L;
    private static final java.util.Map<String, CacheEntry> CACHE = new java.util.HashMap<>();
    private static final java.util.Map<PlayerUrlKey, PlayerCacheEntry> PLAYER_CACHE = new java.util.HashMap<>();

    private PngDownloadUtils() {}

    public static byte[] downloadPng(String url) throws IOException, InterruptedException {
        return downloadPng(url, null);
    }

    public static byte[] downloadPng(String url, java.util.UUID playerUuid) throws IOException, InterruptedException {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank.");
        }
        String normalizedUrl = normalizeUrl(url);
        long now = System.currentTimeMillis();
        if (playerUuid != null) {
            PlayerCacheEntry playerCache = getPlayerCache(playerUuid, normalizedUrl, now);
            if (playerCache != null) {
                HyUIPlugin.getLog().logFinest("PNG player cache hit: " + normalizedUrl);
                return playerCache.bytes();
            }
        }
        CacheEntry cached = getCache(normalizedUrl, now);
        if (cached != null) {
            HyUIPlugin.getLog().logFinest("PNG cache hit: " + normalizedUrl);
            return cached.bytes();
        }
        HyUIPlugin.getLog().logFinest("Downloading PNG: " + normalizedUrl);
        HttpRequest request = HttpRequest.newBuilder(URI.create(normalizedUrl))
                .GET()
                .header("Accept", "image/png")
                .build();
        HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download PNG. HTTP status: " + response.statusCode());
        }
        putCache(normalizedUrl, response.body(), now);
        HyUIPlugin.getLog().logFinest("Downloaded PNG bytes: " + response.body().length);
        return response.body();
    }

    public static void cachePngForPlayer(java.util.UUID playerUuid, String url, byte[] bytes, long ttlSeconds) {
        if (playerUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null.");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank.");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("Image bytes cannot be null.");
        }
        String normalizedUrl = normalizeUrl(url);
        long now = System.currentTimeMillis();
        if (ttlSeconds <= 0) {
            removePlayerCache(playerUuid, normalizedUrl);
            return;
        }
        long expiresAtMs = now + (ttlSeconds * 1000L);
        PlayerCacheEntry existing = getPlayerCache(playerUuid, normalizedUrl, now);
        Integer slotIndex = existing != null ? existing.slotIndex() : null;
        String assetPath = existing != null ? existing.assetPath() : null;
        putPlayerCache(new PlayerUrlKey(playerUuid, normalizedUrl),
                new PlayerCacheEntry(bytes, expiresAtMs, slotIndex, assetPath));
    }

    public static CachedAssetInfo cachePngForPlayer(PlayerRef pRef, String url, byte[] bytes, long ttlSeconds) {
        if (pRef == null || !pRef.isValid()) {
            throw new IllegalArgumentException("Player reference cannot be null or invalid.");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank.");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("Image bytes cannot be null.");
        }
        String normalizedUrl = normalizeUrl(url);
        long now = System.currentTimeMillis();
        if (ttlSeconds <= 0) {
            removePlayerCache(pRef.getUuid(), normalizedUrl);
            return null;
        }

        DynamicImageAsset asset = new DynamicImageAsset(bytes, pRef.getUuid());
        DynamicImageAsset.sendToPlayer(pRef.getPacketHandler(), DynamicImageAsset.empty(asset.getSlotIndex()));
        DynamicImageAsset.sendToPlayer(pRef.getPacketHandler(), asset);
        HyUIPlugin.getLog().logFinest("Dynamic image sent using path: " + asset.getPath());

        long expiresAtMs = now + (ttlSeconds * 1000L);
        putPlayerCache(new PlayerUrlKey(pRef.getUuid(), normalizedUrl),
                new PlayerCacheEntry(bytes, expiresAtMs, asset.getSlotIndex(), asset.getPath()));
        return new CachedAssetInfo(asset.getPath(), asset.getSlotIndex());
    }

    public static byte[] prefetchPngForPlayer(java.util.UUID playerUuid, String url, long ttlSeconds)
            throws IOException, InterruptedException {
        byte[] bytes = downloadPng(url);
        cachePngForPlayer(playerUuid, url, bytes, ttlSeconds);
        return bytes;
    }

    public static CachedAssetInfo prefetchPngForPlayer(PlayerRef pRef, String url, long ttlSeconds)
            throws IOException, InterruptedException {
        if (pRef == null || !pRef.isValid()) {
            throw new IllegalArgumentException("Player reference cannot be null or invalid.");
        }
        java.util.UUID playerUuid = pRef.getUuid();
        byte[] bytes = downloadPng(url, playerUuid);
        return cachePngForPlayer(pRef, url, bytes, ttlSeconds);
    }

    public static void clearCachedPngForPlayer(java.util.UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        synchronized (PLAYER_CACHE) {
            PLAYER_CACHE.entrySet().removeIf(entry -> {
                if (!entry.getKey().playerUuid().equals(playerUuid)) {
                    return false;
                }
                releaseSlot(entry.getKey().playerUuid(), entry.getValue());
                return true;
            });
        }
    }

    private static CacheEntry getCache(String url, long now) {
        synchronized (CACHE) {
            CacheEntry entry = CACHE.get(url);
            if (entry != null && now - entry.createdAtMs() <= CACHE_TTL_MS) {
                return entry;
            }
            CACHE.remove(url);
            return null;
        }
    }

    private static void putCache(String url, byte[] bytes, long now) {
        synchronized (CACHE) {
            CACHE.put(url, new CacheEntry(bytes, now));
        }
    }

    private static PlayerCacheEntry getPlayerCache(java.util.UUID playerUuid, String url, long now) {
        synchronized (PLAYER_CACHE) {
            PlayerUrlKey key = new PlayerUrlKey(playerUuid, url);
            PlayerCacheEntry entry = PLAYER_CACHE.get(key);
            if (entry != null && now <= entry.expiresAtMs()) {
                return entry;
            }
            PLAYER_CACHE.remove(key);
            releaseSlot(playerUuid, entry);
            return null;
        }
    }

    private static void removePlayerCache(java.util.UUID playerUuid, String url) {
        synchronized (PLAYER_CACHE) {
            PlayerCacheEntry entry = PLAYER_CACHE.remove(new PlayerUrlKey(playerUuid, url));
            releaseSlot(playerUuid, entry);
        }
    }

    public static CachedAssetInfo getCachedAssetInfo(java.util.UUID playerUuid, String url) {
        if (playerUuid == null) {
            return null;
        }
        if (url == null || url.isBlank()) {
            return null;
        }
        String normalizedUrl = normalizeUrl(url);
        long now = System.currentTimeMillis();
        PlayerCacheEntry entry = getPlayerCache(playerUuid, normalizedUrl, now);
        if (entry == null || entry.slotIndex() == null || entry.assetPath() == null) {
            return null;
        }
        return new CachedAssetInfo(entry.assetPath(), entry.slotIndex());
    }

    private static void putPlayerCache(PlayerUrlKey key, PlayerCacheEntry entry) {
        synchronized (PLAYER_CACHE) {
            PlayerCacheEntry existing = PLAYER_CACHE.put(key, entry);
            if (existing != null && existing.slotIndex() != null) {
                Integer newSlot = entry.slotIndex();
                if (newSlot == null || !newSlot.equals(existing.slotIndex())) {
                    releaseSlot(key.playerUuid(), existing);
                }
            }
        }
    }

    private static void releaseSlot(java.util.UUID playerUuid, PlayerCacheEntry entry) {
        if (entry == null || entry.slotIndex() == null) {
            return;
        }
        DynamicImageAsset.releaseSlotIndex(playerUuid, entry.slotIndex());
    }

    private static String normalizeUrl(String url) {
        return url.trim();
    }

    private record CacheEntry(byte[] bytes, long createdAtMs) {}

    private record PlayerUrlKey(java.util.UUID playerUuid, String url) {}

    private record PlayerCacheEntry(byte[] bytes, long expiresAtMs, Integer slotIndex, String assetPath) {}

    public record CachedAssetInfo(String path, int slotIndex) {}
}
