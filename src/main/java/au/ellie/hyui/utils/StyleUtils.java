package au.ellie.hyui.utils;

import au.ellie.hyui.builders.HyUIPatchStyle;

public final class StyleUtils {
    private StyleUtils() {}

    public record BackgroundParts(String value, Integer horizontalBorder, Integer verticalBorder, Integer border) {
    }

    public static BackgroundParts parseBackgroundParts(String rawValue, boolean isImage) {
        if (rawValue == null) {
            return new BackgroundParts(null, null, null, null);
        }
        String trimmed = rawValue.trim();
        String valuePart = trimmed;
        String remainder = "";

        if (isImage) {
            if (trimmed.startsWith("url(")) {
                int endIndex = trimmed.indexOf(')');
                if (endIndex > 0) {
                    valuePart = trimmed.substring(0, endIndex + 1);
                    remainder = trimmed.substring(endIndex + 1);
                }
                valuePart = valuePart.replace("url(", "")
                        .replace(")", "")
                        .replace("\"", "")
                        .replace("'", "")
                        .trim();
            } else {
                int spaceIndex = trimmed.indexOf(' ');
                if (spaceIndex > 0) {
                    valuePart = trimmed.substring(0, spaceIndex);
                    remainder = trimmed.substring(spaceIndex + 1);
                }
            }
        } else {
            if (trimmed.startsWith("rgb")) {
                int endIndex = trimmed.indexOf(')');
                if (endIndex > 0) {
                    valuePart = trimmed.substring(0, endIndex + 1);
                    remainder = trimmed.substring(endIndex + 1);
                }
            } else {
                int spaceIndex = trimmed.indexOf(' ');
                if (spaceIndex > 0) {
                    valuePart = trimmed.substring(0, spaceIndex);
                    remainder = trimmed.substring(spaceIndex + 1);
                }
            }
        }

        Integer horizontalBorder = null;
        Integer verticalBorder = null;
        Integer border = null;
        if (!remainder.isBlank()) {
            String[] parts = remainder.trim().split("\\s+");
            if (parts.length >= 2) {
                try {
                    horizontalBorder = Integer.parseInt(parts[0]);
                    verticalBorder = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {}
            } else if (parts.length == 1) {
                try {
                    border = Integer.parseInt(parts[0]);
                } catch (NumberFormatException ignored) {}
            }
        }

        return new BackgroundParts(valuePart.trim(), horizontalBorder, verticalBorder, border);
    }

    public static void applyBorders(HyUIPatchStyle background, BackgroundParts parts) {
        if (background == null || parts == null) {
            return;
        }
        if (parts.horizontalBorder() != null && parts.verticalBorder() != null) {
            background.setHorizontalBorder(parts.horizontalBorder());
            background.setVerticalBorder(parts.verticalBorder());
        } else if (parts.border() != null) {
            background.setBorder(parts.border());
        }
    }

    public static String normalizeBackgroundColor(String rawColor) {
        if (rawColor == null) {
            return null;
        }
        String value = rawColor.trim();
        if (value.isEmpty()) {
            return value;
        }
        if (value.startsWith("#")) {
            int opacityIndex = value.indexOf('(');
            if (opacityIndex > 0 && value.endsWith(")")) {
                String hex = value.substring(0, opacityIndex);
                String opacityText = value.substring(opacityIndex + 1, value.length() - 1);
                try {
                    double opacity = Double.parseDouble(opacityText);
                    String normalizedHex = normalizeHexColor(hex);
                    if (normalizedHex == null) {
                        return value;
                    }
                    if (opacity < 1.0) {
                        return applyHexOpacity(normalizedHex, opacity);
                    }
                    return normalizedHex;
                } catch (NumberFormatException ignored) {
                    return value;
                }
            }
            String normalizedHex = normalizeHexColor(value);
            return normalizedHex != null ? normalizedHex : value;
        }
        if (value.startsWith("rgb")) {
            String channelText = value.replace("rgba(", "")
                    .replace("rgb(", "")
                    .replace(")", "");
            String[] parts = channelText.split(",");
            if (parts.length >= 3) {
                Integer r = parseColorChannel(parts[0]);
                Integer g = parseColorChannel(parts[1]);
                Integer b = parseColorChannel(parts[2]);
                if (r != null && g != null && b != null) {
                    String hex = String.format("#%02x%02x%02x", r, g, b);
                    if (parts.length >= 4) {
                        Double opacity = parseOpacity(parts[3]);
                        if (opacity != null && opacity < 1.0) {
                            return applyHexOpacity(hex, opacity);
                        }
                    }
                    return hex;
                }
            }
        }
        return value;
    }

    private static String normalizeHexColor(String value) {
        if (value == null || !value.startsWith("#")) {
            return null;
        }
        String hex = value.substring(1).trim();
        if (hex.length() == 3) {
            String expanded = "" + hex.charAt(0) + hex.charAt(0)
                    + hex.charAt(1) + hex.charAt(1)
                    + hex.charAt(2) + hex.charAt(2);
            return "#" + expanded.toLowerCase();
        }
        if (hex.length() == 4) {
            String expanded = "" + hex.charAt(0) + hex.charAt(0)
                    + hex.charAt(1) + hex.charAt(1)
                    + hex.charAt(2) + hex.charAt(2)
                    + hex.charAt(3) + hex.charAt(3);
            return "#" + expanded.toLowerCase();
        }
        if (hex.length() == 6 || hex.length() == 8) {
            return "#" + hex.toLowerCase();
        }
        return null;
    }

    private static String applyHexOpacity(String hex, double opacity) {
        String normalized = normalizeHexColor(hex);
        if (normalized == null) {
            return hex;
        }
        String rgb = normalized.length() == 9 ? normalized.substring(0, 7) : normalized;
        int alpha = (int) Math.round(Math.max(0.0, Math.min(1.0, opacity)) * 255.0);
        return rgb + String.format("%02x", alpha);
    }

    private static Integer parseColorChannel(String value) {
        try {
            int channel = Integer.parseInt(value.trim());
            return Math.max(0, Math.min(255, channel));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Double parseOpacity(String value) {
        try {
            double opacity = Double.parseDouble(value.trim());
            return Math.max(0.0, Math.min(1.0, opacity));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
