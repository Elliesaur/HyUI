package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.source.ArchiveAssetSource;
import app.ultradev.hytaleuiparser.source.AssetSource;

import java.nio.file.Path;

/**
 * Options for parsing UI files into HyUI elements.
 */
public final class UIParseOptions {
    private static volatile AssetSource defaultAssetSource;

    private final AssetSource assetSource;
    private final Path assetsZipPath;
    private final boolean validateUnusedVariables;
    private final String uiRootPath;

    private UIParseOptions(AssetSource assetSource, Path assetsZipPath, boolean validateUnusedVariables, String uiRootPath) {
        AssetSource resolved = assetSource;
        if (resolved == null && assetsZipPath != null) {
            resolved = new ArchiveAssetSource(assetsZipPath);
        }
        if (resolved == null) {
            resolved = defaultAssetSource;
        }
        this.assetSource = resolved;
        this.assetsZipPath = assetsZipPath;
        this.validateUnusedVariables = validateUnusedVariables;
        this.uiRootPath = uiRootPath == null || uiRootPath.isBlank() ? "Common/UI/Custom" : uiRootPath;
    }

    /**
     * @return the resolved asset source used for parsing
     */
    public AssetSource getAssetSource() {
        return assetSource;
    }

    /**
     * @return whether to validate unused variables
     */
    public boolean isValidateUnusedVariables() {
        return validateUnusedVariables;
    }

    /**
     * @return UI root path used for resolving relative files
     */
    public String getUiRootPath() {
        return uiRootPath;
    }

    /**
     * @return a new builder for parse options
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Sets the default asset source used by new builders.
     */
    public static void setDefaultAssetSource(AssetSource assetSource) {
        defaultAssetSource = assetSource;
    }

    /**
     * @return the default asset source used by new builders
     */
    public static AssetSource getDefaultAssetSource() {
        return defaultAssetSource;
    }

    /**
     * Creates parse options with an explicit asset source.
     */
    public static UIParseOptions withAssetSource(AssetSource assetSource) {
        return builder().assetSource(assetSource).build();
    }

    /**
     * Creates parse options using an assets archive.
     */
    public static UIParseOptions withAssetsZip(Path assetsZipPath) {
        return builder().assetsZipPath(assetsZipPath).build();
    }

    /**
     * Builder for {@link UIParseOptions}.
     */
    public static final class Builder {
        private AssetSource assetSource = UIParseOptions.getDefaultAssetSource();
        private Path assetsZipPath;
        private boolean validateUnusedVariables;
        private String uiRootPath = "Common/UI/Custom";

        /**
         * Sets the asset source to use.
         */
        public Builder assetSource(AssetSource assetSource) {
            this.assetSource = assetSource;
            return this;
        }

        /**
         * Sets a path to an assets ZIP/JAR.
         */
        public Builder assetsZipPath(Path assetsZipPath) {
            this.assetsZipPath = assetsZipPath;
            return this;
        }

        /**
         * Enables or disables unused variable validation.
         */
        public Builder validateUnusedVariables(boolean validateUnusedVariables) {
            this.validateUnusedVariables = validateUnusedVariables;
            return this;
        }

        /**
         * Sets the UI root path used when resolving relative paths.
         */
        public Builder uiRootPath(String uiRootPath) {
            this.uiRootPath = uiRootPath;
            return this;
        }

        /**
         * Builds immutable parse options.
         */
        public UIParseOptions build() {
            return new UIParseOptions(assetSource, assetsZipPath, validateUnusedVariables, uiRootPath);
        }
    }
}
