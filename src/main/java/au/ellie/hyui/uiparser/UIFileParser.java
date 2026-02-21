package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.Parser;
import app.ultradev.hytaleuiparser.Tokenizer;
import app.ultradev.hytaleuiparser.Validator;
import app.ultradev.hytaleuiparser.ast.RootNode;
import app.ultradev.hytaleuiparser.source.AssetSource;
import app.ultradev.hytaleuiparser.source.AssetSources;
import au.ellie.hyui.builders.InterfaceBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UIFileParser {
    private final UIParseOptions options;

    public UIFileParser() {
        this(UIParseOptions.builder().build());
    }

    public UIFileParser(UIParseOptions options) {
        this.options = options == null ? UIParseOptions.builder().build() : options;
    }

    public UIParseResult parseToInterface(InterfaceBuilder<?> builder, String uiFilePath) {
        UIParseResult result = parse(uiFilePath);
        if (builder != null) {
            for (UIElementBuilder<?> element : result.getElements()) {
                builder.addElement(element);
            }
        }
        return result;
    }

    public UIParseResult parse(String uiFilePath) {
        UIParseResult result = new UIParseResult();
        if (uiFilePath == null || uiFilePath.isBlank()) {
            result.addConversionWarning("UI file path was empty.");
            return result;
        }

        Map<String, RootNode> files = new LinkedHashMap<>();
        AssetSource assetSource = options.getAssetSource();
        AssetSource filteredSource = assetSource != null
                ? new FilteredAssetSource(assetSource, options.getUiRootPath())
                : null;
        if (filteredSource != null) {
            try {
                files.putAll(AssetSources.INSTANCE.parseUIFiles(filteredSource));
            } catch (RuntimeException e) {
                result.addConversionWarning("Failed to parse UI files from asset source: " + e.getMessage());
            }
        }

        String documentPath = normalizePath(uiFilePath);
        if (!Paths.get(uiFilePath).isAbsolute()) {
            documentPath = prependRoot(documentPath, options.getUiRootPath());
        }
        result.setDocumentPath(documentPath);

        if (!files.containsKey(documentPath)) {
            UiFileSource source = resolveSource(uiFilePath, filteredSource, options.getUiRootPath());
            if (source == null) {
                result.addConversionWarning("UI file not found: " + uiFilePath);
                return result;
            }
            ParsedRoot parsed = parseRoot(source, result);
            if (parsed == null) {
                return result;
            }
            files.put(source.documentPath, parsed.root);
        }

        Validator validator = new Validator(files, options.isValidateUnusedVariables(), filteredSource);
        try {
            validator.validateRoot(documentPath, false);
        } catch (RuntimeException e) {
            result.addConversionWarning("Validation failed for " + documentPath + ": " + e.getMessage());
        }

        result.addValidationErrors(validator.getValidationErrors());
        result.addValidationWarnings(validator.getValidationWarnings());

        RootNode root = files.get(documentPath);
        if (root == null) {
            result.addConversionWarning("No root node available for " + documentPath);
            return result;
        }

        UIAstConverter converter = new UIAstConverter(result, documentPath);
        result.addElements(converter.convert(root));
        return result;
    }

    public UIParseResult parse(Path uiFile) {
        UIParseResult result = new UIParseResult();
        if (uiFile == null) {
            result.addConversionWarning("UI file path was null.");
            return result;
        }
        if (!Files.isRegularFile(uiFile)) {
            result.addConversionWarning("UI file not found: " + uiFile);
            return result;
        }
        return parse(uiFile.toAbsolutePath().toString());
    }

    private ParsedRoot parseRoot(UiFileSource source, UIParseResult result) {
        try (InputStream input = source.streamSupplier.open()) {
            if (input == null) {
                result.addConversionWarning("Failed to open UI file stream for " + source.documentPath);
                return null;
            }
            Tokenizer tokenizer = new Tokenizer(new InputStreamReader(input, StandardCharsets.UTF_8));
            Parser parser = new Parser(tokenizer);
            RootNode root = parser.finish();
            result.addParserErrors(parser.getParserErrors());
            return new ParsedRoot(root);
        } catch (IOException e) {
            result.addConversionWarning("Failed to read UI file " + source.documentPath + ": " + e.getMessage());
            return null;
        } catch (RuntimeException e) {
            result.addConversionWarning("Failed to parse UI file " + source.documentPath + ": " + e.getMessage());
            return null;
        }
    }

    private UiFileSource resolveSource(String uiFilePath, AssetSource assetSource, String uiRootPath) {
        String normalized = normalizePath(uiFilePath);
        Path rawPath = Paths.get(uiFilePath);
        if (rawPath.isAbsolute()) {
            if (Files.isRegularFile(rawPath)) {
                return new UiFileSource(normalizePath(rawPath.toString()), () -> Files.newInputStream(rawPath));
            }
            return null;
        }

        List<String> candidates = new ArrayList<>();
        candidates.add(normalized);
        String rooted = prependRoot(normalized, uiRootPath);
        if (!rooted.equals(normalized)) {
            candidates.add(rooted);
        }

        if (assetSource != null) {
            for (String candidate : candidates) {
                InputStream stream = assetSource.getAsset(Paths.get(candidate));
                if (stream != null) {
                    return new UiFileSource(candidate, () -> stream);
                }
            }
        }

        for (String candidate : candidates) {
            Path local = resolveLocalFile(candidate);
            if (local != null) {
                String documentPath = candidate;
                return new UiFileSource(documentPath, () -> Files.newInputStream(local));
            }
        }

        return null;
    }

    private Path resolveLocalFile(String candidate) {
        List<Path> searchPaths = List.of(
                Paths.get("src/main/resources").resolve(candidate),
                Paths.get("..", "src", "main", "resources").resolve(candidate),
                Paths.get("build/resources/main").resolve(candidate),
                Paths.get("..", "build", "resources", "main").resolve(candidate),
                Paths.get(candidate)
        );
        for (Path path : searchPaths) {
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }

    private String prependRoot(String path, String uiRootPath) {
        if (uiRootPath == null || uiRootPath.isBlank()) {
            return path;
        }
        String normalizedRoot = normalizeRoot(uiRootPath);
        if (path.startsWith(normalizedRoot)) {
            return path;
        }
        return normalizedRoot + "/" + path;
    }

    private String normalizeRoot(String uiRootPath) {
        if (uiRootPath == null) {
            return null;
        }
        String normalized = normalizePath(uiRootPath);
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static final class FilteredAssetSource implements AssetSource {
        private final AssetSource delegate;
        private final String root;

        private FilteredAssetSource(AssetSource delegate, String rootPath) {
            this.delegate = delegate;
            this.root = rootPath == null || rootPath.isBlank() ? null : normalizeRootStatic(rootPath);
        }

        @NotNull
        @Override
        public List<Path> listUIFiles() {
            List<Path> files = delegate.listUIFiles();
            if (root == null || root.isBlank()) {
                return files;
            }
            List<Path> filtered = new ArrayList<>();
            for (Path path : files) {
                String normalized = normalizePathStatic(path.toString());
                if (normalized.startsWith(root)) {
                    filtered.add(path);
                }
            }
            return filtered;
        }

        @Override
        public InputStream getAsset(@NotNull Path path) {
            if (root != null && !root.isBlank()) {
                String normalized = normalizePathStatic(path.toString());
                if (!normalized.startsWith(root)) {
                    return null;
                }
            }
            return delegate.getAsset(path);
        }

        private static String normalizeRootStatic(String uiRootPath) {
            String normalized = normalizePathStatic(uiRootPath);
            if (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            return normalized;
        }

        private static String normalizePathStatic(String path) {
            String normalized = path.replace("\\", "/");
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            return normalized;
        }
    }

    private String normalizePath(String path) {
        String normalized = path.replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private record ParsedRoot(RootNode root) {}

    private record UiFileSource(String documentPath, StreamSupplier streamSupplier) {}

    @FunctionalInterface
    private interface StreamSupplier {
        InputStream open() throws IOException;
    }
}
