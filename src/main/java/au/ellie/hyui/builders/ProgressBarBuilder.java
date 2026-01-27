package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for creating progress bar UI elements.
 * Progress bars are used to display the completion status of a task or process.
 */
public class ProgressBarBuilder extends UIElementBuilder<ProgressBarBuilder> implements BackgroundSupported<ProgressBarBuilder>, LayoutModeSupported<ProgressBarBuilder> {
    private float value = 0.0f;
    private String barTexturePath;
    private String effectTexturePath;
    private Integer effectWidth;
    private Integer effectHeight;
    private Integer effectOffset;
    private String direction;
    private String alignment;
    private String maskTexturePath;
    private boolean circular;
    private String color;
    private HyUIPatchStyle background;
    private HyUIPatchStyle bar;
    private String layoutMode;
    private HyUIAnchor outerAnchor;

    public ProgressBarBuilder() {
        super(UIElements.PROGRESS_BAR, "#HyUIProgressBar");
        withUiFile("Pages/Elements/ProgressBar.ui");
        withWrappingGroup(true);
        this.initialValue = 0.0f;
    }

    public ProgressBarBuilder(Theme theme) {
        super(theme, UIElements.PROGRESS_BAR, "#HyUIProgressBar");
        withUiFile("Pages/Elements/ProgressBar.ui");
        withWrappingGroup(true);
    }

    /**
     * Factory method to create a new instance of {@code ProgressBarBuilder}.
     *
     * @return A new {@code ProgressBarBuilder} instance.
     */
    public static ProgressBarBuilder progressBar() {
        return new ProgressBarBuilder();
    }

    /**
     * Factory method to create a new instance of {@code ProgressBarBuilder} using a circular progress bar.
     *
     * @return A new {@code ProgressBarBuilder} instance configured for CircularProgressBar.
     */
    public static ProgressBarBuilder circularProgressBar() {
        ProgressBarBuilder builder = new ProgressBarBuilder();
        builder.withCircular(true);
        return builder;
    }

    /**
     * Enables circular progress bar mode (CircularProgressBar).
     *
     * @param circular whether to use CircularProgressBar
     * @return This builder instance for method chaining.
     */
    public ProgressBarBuilder withCircular(boolean circular) {
        this.circular = circular;
        if (circular) {
            withUiFile(null);
        } else {
            withUiFile("Pages/Elements/ProgressBar.ui");
        }
        return this;
    }

    /**
     * Sets the value of the progress bar.
     *
     * @param value A float between 0.0 and 1.0.
     * @return This builder instance for method chaining.
     */
    public ProgressBarBuilder withValue(float value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    public ProgressBarBuilder withBarTexturePath(String barTexturePath) {
        this.barTexturePath = barTexturePath;
        return this;
    }

    public ProgressBarBuilder withEffectTexturePath(String effectTexturePath) {
        this.effectTexturePath = effectTexturePath;
        return this;
    }

    public ProgressBarBuilder withMaskTexturePath(String maskTexturePath) {
        this.maskTexturePath = maskTexturePath;
        return this;
    }

    /**
     * Sets the color of the progress bar fill.
     *
     * @param color the color string (e.g. #RRGGBB)
     * @return This builder instance for method chaining.
     */
    public ProgressBarBuilder withColor(String color) {
        this.color = color;
        return this;
    }

    public ProgressBarBuilder withEffectWidth(int effectWidth) {
        this.effectWidth = effectWidth;
        return this;
    }

    public ProgressBarBuilder withEffectHeight(int effectHeight) {
        this.effectHeight = effectHeight;
        return this;
    }

    public ProgressBarBuilder withEffectOffset(int effectOffset) {
        this.effectOffset = effectOffset;
        return this;
    }

    /**
     * Sets the direction of the progress bar.
     * 
     * @param direction The direction: Start, or End.
     * @return This builder instance for method chaining.
     */
    public ProgressBarBuilder withDirection(String direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public ProgressBarBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    public ProgressBarBuilder withBar(HyUIPatchStyle bar) {
        this.bar = bar;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean hasCustomInlineContent() {
        return circular || (!circular && (barTexturePath != null || effectTexturePath != null));
    }

    @Override
    protected String generateCustomInlineContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group #HyUIOuterProgressBar { ");
        if (circular) {
            sb.append("CircularProgressBar #HyUIProgressBar { ");
            if (maskTexturePath != null) {
                sb.append("MaskTexturePath: \"").append(maskTexturePath).append("\"; ");
            }
            sb.append("Value: 0.0; ");
            sb.append("} ");
        } else {
            sb.append("Background: \"../../Common/ProgressBar.png\"; ");
            sb.append("ProgressBar #HyUIProgressBar { ");
            String inlineBarTexturePath = barTexturePath != null ? barTexturePath : "../../Common/ProgressBarFill.png";
            String inlineEffectTexturePath = effectTexturePath != null ? effectTexturePath : "../../Common/ProgressBarEffect.png";
            sb.append("BarTexturePath: \"").append(inlineBarTexturePath).append("\"; ");
            sb.append("EffectTexturePath: \"").append(inlineEffectTexturePath).append("\"; ");
            sb.append("Value: 0.0; ");
            sb.append("} ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Object parseValue(String rawValue) {
        try {
            return Float.parseFloat(rawValue);
        } catch (NumberFormatException e) {
            return super.parseValue(rawValue);
        }
    }

    /**
     * Sets the alignment of the progress bar.
     *
     * @param alignment The alignment: Horizontal, or Vertical.
     * @return This builder instance for method chaining.
     */
    public ProgressBarBuilder withAlignment(String alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public ProgressBarBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    /**
     * Sets the anchor for the outer progress bar group.
     *
     * @param outerAnchor The anchor to set on the outer group.
     * @return This builder instance for method chaining.
     */
    public ProgressBarBuilder withOuterAnchor(HyUIAnchor outerAnchor) {
        this.outerAnchor = outerAnchor;
        return this;
    }
    
    @Override
    protected void buildBase(UICommandBuilder commands, UIEventBuilder events, boolean updateOnly) {
        // Temporarily hide the anchor from buildBase so it doesn't apply to the inner element
        HyUIAnchor originalAnchor = this.anchor;
        if (this.outerAnchor == null) {
            this.anchor = null;
        }

        super.buildBase(commands, events, updateOnly);

        // Restore it immediately after buildBase completes
        this.anchor = originalAnchor;
    }
    
    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        // Apply LayoutMode and Background to the outer group if it exists
        String outerSelector = "#HyUIOuterProgressBar";
        if (parentSelector != null) {
            outerSelector = parentSelector + " " + outerSelector;
        }

        applyLayoutMode(commands, outerSelector);
        applyBackground(commands, outerSelector);

        // Use outerAnchor if provided, otherwise fallback to the standard anchor
        HyUIAnchor effectiveOuterAnchor = (outerAnchor != null) ? outerAnchor : anchor;
        if (effectiveOuterAnchor != null) {
            commands.setObject(outerSelector + ".Anchor", effectiveOuterAnchor.toHytaleAnchor());
        }

        if (value != 0.0f) {
            commands.set(selector + ".Value", value);
        }
        if (barTexturePath != null) {
            commands.set(selector + ".BarTexturePath", barTexturePath);
        }
        if (effectTexturePath != null) {
            if (!circular) {
                commands.set(selector + ".EffectTexturePath", effectTexturePath);
            }
        }
        if (effectWidth != null) {
            if (!circular) {
                commands.set(selector + ".EffectWidth", effectWidth);
            }
        }
        if (effectHeight != null) {
            if (!circular) {
                commands.set(selector + ".EffectHeight", effectHeight);
            }
        }
        if (effectOffset != null) {
            if (!circular) {
                commands.set(selector + ".EffectOffset", effectOffset);
            }
        }
        if (direction != null) {
            commands.set(selector + ".Direction", direction);
        }
        if (alignment != null) {
            commands.set(selector + ".Alignment", alignment);
        }
        if (bar != null) {
            commands.setObject(selector + ".Bar", bar.getHytalePatchStyle());
        }
        if (color != null) {
            commands.set(selector + ".Color", color);
        }
    }
}
