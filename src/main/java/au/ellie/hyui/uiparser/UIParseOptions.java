package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.source.ArchiveAssetSource;
import app.ultradev.hytaleuiparser.source.AssetSource;

import java.nio.file.Path;

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

    public AssetSource getAssetSource() {
        return assetSource;
    }

    public boolean isValidateUnusedVariables() {
        return validateUnusedVariables;
    }

    public String getUiRootPath() {
        return uiRootPath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void setDefaultAssetSource(AssetSource assetSource) {
        defaultAssetSource = assetSource;
    }

    public static AssetSource getDefaultAssetSource() {
        return defaultAssetSource;
    }

    public static UIParseOptions withAssetSource(AssetSource assetSource) {
        return builder().assetSource(assetSource).build();
    }

    public static UIParseOptions withAssetsZip(Path assetsZipPath) {
        return builder().assetsZipPath(assetsZipPath).build();
    }

    public static final class Builder {
        private AssetSource assetSource = UIParseOptions.getDefaultAssetSource();
        private Path assetsZipPath;
        private boolean validateUnusedVariables;
        private String uiRootPath = "Common/UI/Custom";

        public Builder assetSource(AssetSource assetSource) {
            this.assetSource = assetSource;
            return this;
        }

        public Builder assetsZipPath(Path assetsZipPath) {
            this.assetsZipPath = assetsZipPath;
            return this;
        }

        public Builder validateUnusedVariables(boolean validateUnusedVariables) {
            this.validateUnusedVariables = validateUnusedVariables;
            return this;
        }

        public Builder uiRootPath(String uiRootPath) {
            this.uiRootPath = uiRootPath;
            return this;
        }

        public UIParseOptions build() {
            return new UIParseOptions(assetSource, assetsZipPath, validateUnusedVariables, uiRootPath);
        }
    }
}
