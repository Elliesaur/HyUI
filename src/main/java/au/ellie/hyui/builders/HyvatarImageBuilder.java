package au.ellie.hyui.builders;

import au.ellie.hyui.utils.HyvatarUtils;

public class HyvatarImageBuilder extends DynamicImageBuilder {
    private String username;
    private HyvatarUtils.RenderType renderType = HyvatarUtils.RenderType.HEAD;
    private Integer size;
    private Integer rotate;
    private String cape;

    public HyvatarImageBuilder() {
        super();
    }

    public static HyvatarImageBuilder hyvatar() {
        return new HyvatarImageBuilder();
    }

    public HyvatarImageBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public HyvatarImageBuilder withRenderType(HyvatarUtils.RenderType renderType) {
        if (renderType != null) {
            this.renderType = renderType;
        }
        return this;
    }

    public HyvatarUtils.RenderType getRenderType() {
        return renderType;
    }

    public HyvatarImageBuilder withSize(Integer size) {
        this.size = size;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public HyvatarImageBuilder withRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }

    public Integer getRotate() {
        return rotate;
    }

    public HyvatarImageBuilder withCape(String cape) {
        this.cape = cape;
        return this;
    }

    public String getCape() {
        return cape;
    }

    public boolean hasCustomImageUrl() {
        String customUrl = super.getImageUrl();
        return customUrl != null && !customUrl.isBlank();
    }

    @Override
    public String getImageUrl() {
        String customUrl = super.getImageUrl();
        if (customUrl != null && !customUrl.isBlank()) {
            return customUrl;
        }
        return HyvatarUtils.buildRenderUrl(username, renderType, size, rotate, cape);
    }
}
