package au.ellie.hyui.builders;

import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;

public class HyUIAnchor {
    private int left = -1;
    private int right = -1;
    private int top = -1;
    private int bottom = -1;
    private int height = -1;
    private int full = -1;
    private int horizontal = -1;
    private int vertical = -1;
    private int width = -1;
    private int minWidth = -1;
    private int maxWidth = -1;
    public HyUIAnchor() {
    }

    public HyUIAnchor(int left, int right, int top, int bottom, int height, int full, int horizontal, int vertical, int width, int minWidth, int maxWidth) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.height = height;
        this.full = full;
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.width = width;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
    }

    public HyUIAnchor setLeft(int left) {
        this.left = left;
        return this;
    }

    public HyUIAnchor setRight(int right) {
        this.right = right;
        return this;
    }

    public HyUIAnchor setTop(int top) {
        this.top = top;
        return this;
    }

    public HyUIAnchor setBottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public HyUIAnchor setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public HyUIAnchor setFull(int full) {
        this.full = full;
        return this;
    }

    public HyUIAnchor setHorizontal(int horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public HyUIAnchor setVertical(int vertical) {
        this.vertical = vertical;
        return this;
    }

    public HyUIAnchor setWidth(int width) {
        this.width = width;
        return this;
    }

    public HyUIAnchor setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public HyUIAnchor setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Anchor toHytaleAnchor() {
        Anchor anchor = new Anchor();
        if (left >= 0) anchor.setLeft(Value.of(left));
        if (right >= 0) anchor.setRight(Value.of(right));
        if (top >= 0) anchor.setTop(Value.of(top));
        if (bottom >= 0) anchor.setBottom(Value.of(bottom));
        if (height >= 0) anchor.setHeight(Value.of(height));
        if (full >= 0) anchor.setFull(Value.of(full));
        if (horizontal >= 0) anchor.setHorizontal(Value.of(horizontal));
        if (vertical >= 0) anchor.setVertical(Value.of(vertical));
        if (width >= 0) anchor.setWidth(Value.of(width));
        if (minWidth >= 0) anchor.setMinWidth(Value.of(minWidth));
        if (maxWidth >= 0) anchor.setMaxWidth(Value.of(maxWidth));
        return anchor;
    }
}
