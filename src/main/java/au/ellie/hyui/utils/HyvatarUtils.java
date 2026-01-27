package au.ellie.hyui.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class HyvatarUtils {
    public static final String BASE_URL = "https://hyvatar.io";

    public enum RenderType {
        HEAD("render"),
        FULL("render/full"),
        CAPE("render/cape");

        private final String path;

        RenderType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    private HyvatarUtils() {}

    public static RenderType parseRenderType(String value) {
        if (value == null || value.isBlank()) {
            return RenderType.HEAD;
        }
        return switch (value.trim().toLowerCase()) {
            case "full" -> RenderType.FULL;
            case "cape" -> RenderType.CAPE;
            default -> RenderType.HEAD;
        };
    }

    public static String buildRenderUrl(String username, RenderType renderType, Integer size, Integer rotate, String capeOverride) {
        if (username == null || username.isBlank()) {
            return null;
        }
        RenderType resolvedType = renderType != null ? renderType : RenderType.HEAD;
        String encodedUsername = encodePathSegment(username.trim());
        StringBuilder url = new StringBuilder(BASE_URL)
                .append("/")
                .append(resolvedType.getPath())
                .append("/")
                .append(encodedUsername);

        boolean hasQuery = false;
        Integer normalizedSize = normalizeSize(size);
        if (normalizedSize != null) {
            hasQuery = appendQueryParam(url, "size", normalizedSize.toString(), hasQuery);
        }
        Integer normalizedRotate = normalizeRotate(rotate);
        if (normalizedRotate != null) {
            hasQuery = appendQueryParam(url, "rotate", normalizedRotate.toString(), hasQuery);
        }
        if (resolvedType == RenderType.CAPE && capeOverride != null && !capeOverride.isBlank()) {
            hasQuery = appendQueryParam(url, "cape", encodeQueryParam(capeOverride.trim()), hasQuery);
        }

        return url.toString();
    }

    public static byte[] downloadRenderPng(String username, RenderType renderType, Integer size, Integer rotate, String capeOverride)
            throws IOException, InterruptedException {
        String url = buildRenderUrl(username, renderType, size, rotate, capeOverride);
        if (url == null) {
            throw new IllegalArgumentException("Username is required to build a Hyvatar render URL.");
        }
        return PngDownloadUtils.downloadPng(url);
    }

    private static Integer normalizeSize(Integer size) {
        if (size == null) {
            return null;
        }
        return Math.max(64, Math.min(2048, size));
    }

    private static Integer normalizeRotate(Integer rotate) {
        if (rotate == null) {
            return null;
        }
        return Math.max(0, Math.min(360, rotate));
    }

    private static boolean appendQueryParam(StringBuilder url, String key, String value, boolean hasQuery) {
        url.append(hasQuery ? "&" : "?").append(key).append("=").append(value);
        return true;
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String encodeQueryParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
