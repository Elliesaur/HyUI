package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.source.ArchiveAssetSource;
import app.ultradev.hytaleuiparser.source.AssetSource;
import app.ultradev.hytaleuiparser.source.DirectoryAssetSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Asset source that aggregates multiple directories, archives, and loose UI files.
 */
public final class UnifiedAssetSource implements AssetSource {
    private final List<AssetSource> sources = new ArrayList<>();
    private final Map<String, Path> uiFiles = new LinkedHashMap<>();

    /**
     * Adds a path to the source list. Directories and archives are added as asset sources,
     * while loose .ui files are tracked directly.
     *
     * @param assetPath path to add
     */
    public void addPath(Path assetPath) {
        if (assetPath == null) {
            return;
        }
        if (Files.isDirectory(assetPath)) {
            sources.add(new DirectoryAssetSource(assetPath));
            return;
        }
        if (!Files.isRegularFile(assetPath)) {
            return;
        }
        String name = assetPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".zip") || name.endsWith(".jar")) {
            sources.add(new ArchiveAssetSource(assetPath));
            return;
        }
        if (name.endsWith(".ui")) {
            String assetKey = toAssetPath(assetPath);
            uiFiles.putIfAbsent(assetKey, assetPath);
        }
    }

    @Override
    /**
     * Lists all UI files from aggregated sources.
     */
    public List<Path> listUIFiles() {
        LinkedHashSet<Path> out = new LinkedHashSet<>();
        for (String assetKey : uiFiles.keySet()) {
            out.add(Paths.get(assetKey));
        }
        for (AssetSource source : sources) {
            out.addAll(source.listUIFiles());
        }
        return new ArrayList<>(out);
    }

    @Override
    /**
     * Resolves an asset stream from aggregated sources.
     */
    public InputStream getAsset(Path path) {
        if (path != null) {
            Path file = uiFiles.get(path.toString());
            if (file != null) {
                try {
                    return Files.newInputStream(file);
                } catch (IOException ignored) {
                    return null;
                }
            }
        }
        for (AssetSource source : sources) {
            InputStream stream = source.getAsset(path);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }

    /**
     * Converts a local file path into an asset-relative path.
     */
    private String toAssetPath(Path filePath) {
        String normalized = filePath.toString().replace("\\", "/");
        int index = normalized.indexOf("Common/UI/Custom/");
        if (index >= 0) {
            return normalized.substring(index);
        }
        return filePath.getFileName().toString();
    }
}
