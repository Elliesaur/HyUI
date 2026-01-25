package au.ellie.hyui.builders;

import com.hypixel.hytale.server.core.ui.Area;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;

/**
 * Represents a PatchStyle which can be used for backgrounds.
 */
public class HyUIPatchStyle {
    private String color;
    private String texturePath;
    private Integer border;
    private Integer horizontalBorder;
    private Integer verticalBorder;
    private Integer areaHeight;
    private Integer areaWidth;
    private Integer areaX;
    private Integer areaY;

    public HyUIPatchStyle() {}

    /**
     * Sets the color for this PatchStyle.
     * 
     * @param color The color (e.g., #FFFFFF01). The last two characters are opacity.
     * @return This PatchStyle instance for method chaining.
     */
    public HyUIPatchStyle setColor(String color) {
        this.color = color;
        return this;
    }

    /**
     * Gets the color of this PatchStyle.
     * 
     * @return The color, or null if not set.
     */
    public String getColor() {
        return color;
    }

    public HyUIPatchStyle setTexturePath(String texturePath) {
        this.texturePath = texturePath;
        return this;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public HyUIPatchStyle setBorder(Integer border) {
        this.border = border;
        return this;
    }

    public Integer getBorder() {
        return border;
    }

    public HyUIPatchStyle setHorizontalBorder(Integer horizontalBorder) {
        this.horizontalBorder = horizontalBorder;
        return this;
    }

    public Integer getHorizontalBorder() {
        return horizontalBorder;
    }

    public HyUIPatchStyle setVerticalBorder(Integer verticalBorder) {
        this.verticalBorder = verticalBorder;
        return this;
    }

    public Integer getVerticalBorder() {
        return verticalBorder;
    }

    public HyUIPatchStyle setAreaHeight(Integer areaHeight) {
        this.areaHeight = areaHeight;
        return this;
    }

    public Integer getAreaHeight() {
        return areaHeight;
    }

    public HyUIPatchStyle setAreaWidth(Integer areaWidth) {
        this.areaWidth = areaWidth;
        return this;
    }

    public Integer getAreaWidth() {
        return areaWidth;
    }

    public HyUIPatchStyle setAreaX(Integer areaX) {
        this.areaX = areaX;
        return this;
    }

    public Integer getAreaX() {
        return areaX;
    }

    public HyUIPatchStyle setAreaY(Integer areaY) {
        this.areaY = areaY;
        return this;
    }

    public Integer getAreaY() {
        return areaY;
    }
    
    public PatchStyle getHytalePatchStyle() {
        PatchStyle hytalePatchStyle = new PatchStyle();
        if (color != null) {
            //hytalePatchStyle.setColor(Value.of("#000000(0.5)"));
            hytalePatchStyle.setColor(Value.of(color));
        }
        if (texturePath != null) {
            hytalePatchStyle.setTexturePath(Value.of(texturePath));
        }
        
        if (border != null) {
            hytalePatchStyle.setBorder(Value.of(border));
        }
        if (horizontalBorder != null) {
            hytalePatchStyle.setHorizontalBorder(Value.of(horizontalBorder));
        }
        if (verticalBorder != null) {
            hytalePatchStyle.setVerticalBorder(Value.of(verticalBorder));
        }

        if (areaHeight != null || areaWidth != null || areaX != null || areaY != null) {
            Area area = new Area();
            if (areaHeight != null) area.setHeight(areaHeight);
            if (areaWidth != null) area.setWidth(areaWidth);
            if (areaX != null) area.setX(areaX);
            if (areaY != null) area.setY(areaY);
            hytalePatchStyle.setArea(Value.of(area));
        }
        
        return hytalePatchStyle;
    }
}
