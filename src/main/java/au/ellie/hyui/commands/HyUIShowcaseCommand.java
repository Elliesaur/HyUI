package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.builders.DynamicImageBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HyUIShowcaseCommand extends AbstractAsyncCommand {

    public HyUIShowcaseCommand() {
        super("showcase", "Opens the HyUI 0.5.0 feature showcase");
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return;
        }
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return CompletableFuture.completedFuture(null);
        }
        var sender = commandContext.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        openShowcase(player, playerRef, store);
                    }
                }, world);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private void openShowcase(Player player, PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return;
        }

        // Track click count for demo button
        AtomicInteger clickCount = new AtomicInteger(0);

        // Create the template processor with variables and components
        TemplateProcessor template = createTemplateProcessor(playerRef);

        String html = createShowcaseHtml();

        PageBuilder builder = PageBuilder.pageForPlayer(playerRef)
                .fromTemplate(html, template)
                .withLifetime(CustomPageLifetime.CanDismiss);

        // Interactive demo button
        builder.addEventListener("demo-button", CustomUIEventBindingType.Activating, (data, uiCtx) -> {
            int count = clickCount.incrementAndGet();
            playerRef.sendMessage(Message.raw("[Showcase] Button clicked " + count + " times!"));
            uiCtx.updatePage(true);
        });
        builder.addEventListener("head-url-button", CustomUIEventBindingType.Activating, (data, ctx) -> {
            ctx.getValue("head-url-input", String.class).ifPresent(url -> {
                if (url.isBlank()) {
                    return;
                }
                ctx.getById("player-head-image", DynamicImageBuilder.class)
                        .ifPresent(image -> image.withImageUrl(url));
                ctx.getPage().ifPresent(page -> page.reloadImage("player-head-image") );
                ctx.updatePage(true);
            });
        });

        builder.open(store);
    }

    private TemplateProcessor createTemplateProcessor(PlayerRef playerRef) {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return null;
        }
        List<ShowcaseItem> items = List.of(
                new ShowcaseItem("Crude Pickaxe", 6, "Common", new ShowcaseMeta("Starter", "Crafted")),
                new ShowcaseItem("Stone Hammer", 12, "Rare", new ShowcaseMeta("Journeyman", "Loot")),
                new ShowcaseItem("Voidblade", 22, "Epic", new ShowcaseMeta("Legend", "Boss Drop"))
        );

        return new TemplateProcessor()
                // Player variables
                .setVariable("playerName", playerRef.getUsername())
                .setVariable("playerLevel", 42)
                .setVariable("playerXp", 8750)
                .setVariable("playerGold", 12500)
                .setVariable("playerHealth", 85)
                .setVariable("playerMaxHealth", 100)

                // Showcase list data
                .setVariable("items", items)
                .setVariable("minPower", 10)
                .setVariable("isAdmin", false)
                .setVariable("showcaseTags", List.of("Legend", "Crafted", "Starter"))

                // Stats for demo (Hytale-themed)
                .setVariable("blocksPlaced", 12847)
                .setVariable("creaturesFound", 23)
                .setVariable("recipesLearned", 156)
                .setVariable("zonesExplored", 4)

                // Time variables
                .setVariable("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))

                // Register reusable components
                .registerComponent("statCard", """
                <div style="background-color: #2a2a3e; padding: 10; anchor-width: 120; anchor-height: 60; layout-mode: top;">
                    <p style="color: #888888; font-size: 19;">{{$label}}</p>
                    <p style="color: #ffffff; font-size: 26; font-weight: bold;">{{$value}}</p>
                </div>
                """)

                .registerComponent("featureItem", """
                <div style="flex-direction: row; padding: 5; anchor-height: 30;">
                    <p style="color: #4CAF50; anchor-width: 20;">*</p>
                    <p style="color: #cccccc; flex-weight: 1;">{{$text}}</p>
                </div>
                """)
                .registerComponent("showcaseItem", """
                <div style="background-color: #2a2a3e; padding: 8; anchor-height: 40; flex-direction: row;">
                    <p style="color: #ffffff; flex-weight: 2;">{{$name}} (Tier: {{$meta.tier}})</p>
                    {{#if power >= minPower && rarity != Common}}
                    <p style="color: #4CAF50; flex-weight: 1;">Power {{$power}}</p>
                    {{else}}
                    <p style="color: #888888; flex-weight: 1;">Power {{$power}}</p>
                    {{/if}}
                    {{#if meta.source contains "Craft" || rarity == Epic}}
                    <p style="color: #FFD54F; flex-weight: 1;">Highlight</p>
                    {{/if}}
                </div>
                """);
    }

    private String createShowcaseHtml() {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return "";
        }
        return """
                <div style="background-color: #1a1a2e(0.95); anchor: 0; padding: 20; flex-direction: column;">
                    <!-- Header -->
                    <div style="anchor-height: 80; flex-direction: row; align-items: center; gap: 10;">
                        <img class="dynamic-image" src="https://media.forgecdn.net/avatars/thumbnails/1616/805/64/64/639041389520913670.png" style="anchor-width: 48; anchor-height: 48;" />
                        <div style="flex-direction: column;">
                            <p style="color: #4CAF50; font-size: 32; font-weight: bold;">HyUI 0.5.0 Feature Showcase</p>
                            <p style="color: #888888; font-size: 22;">Welcome, {{$playerName|Guest}}!</p>
                        </div>
                    </div>
                
                    <!-- Tab Navigation -->
                    <nav class="tabs" data-tabs="templates:Templates:templates-content,timers:Timers:timers-content,components:Components:components-content,inputs:Inputs:inputs-content,items:Items:items-content" data-selected="templates" style="anchor-height: 50;">
                    </nav>
                
                    <!-- Content Area -->
                    <div style="flex-weight: 1; padding: 15; background-color: #16213e(0.5); flex-direction: column;">
                
                        <div id="templates-content" class="tab-content" data-hyui-tab-id="templates" style="flex-direction: column;">
                            <!-- Section 1: Template Variables -->
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">1. Template Variables</p>
                            <div style="flex-direction: row; anchor-height: 130;">
                                <div style="background-color: #2a2a3e; padding: 10; anchor-width: 140; align-items: center; justify-content: center;">
                                    <img id="player-head-image" class="dynamic-image" src="https://hyvatar.io/render/{{$playerName}}" style="anchor-width: 120; anchor-height: 120;" />
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; anchor-width: 140; align-items: center; justify-content: center;">
                                    <img class="dynamic-image" src="https://hyvatar.io/render/full/{{$playerName}}" style="anchor-width: 120; anchor-height: 120;" />
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #ffffff; font-size: 20;">Player: {{$playerName}}</p>
                                    <p style="color: #ffffff; font-size: 20;">Level: {{$playerLevel}}</p>
                                    <p style="color: #ffffff; font-size: 20;">Gold: {{$playerGold|number}}</p>
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #ffffff; font-size: 20;">Upper: {{$playerName|upper}}</p>
                                    <p style="color: #ffffff; font-size: 20;">Lower: {{$playerName|lower}}</p>
                                    <p style="color: #ffffff; font-size: 20;">Default: {{$missing|Not Set}}</p>
                                </div>
                            </div>
                            <div style="flex-direction: row; anchor-height: 40; gap: 10; padding-top: 5;">
                                <input type="text" id="head-url-input" value="https://hyvatar.io/render/{{$playerDisplayName}}" style="anchor-height: 30; flex-weight: 1;" />
                                <button id="head-url-button" style="anchor-width: 140;">Reload Head</button>
                            </div>
                
                            <!-- Section 2: Loops + Conditionals -->
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">2. Loops + Conditionals</p>
                            <div style="flex-direction: column; anchor-height: 200;">
                                {{#each items}}
                                {{@showcaseItem:name={{$name}},meta.tier={{$meta.tier}},power={{$power}},rarity={{$rarity}},meta.source={{$meta.source}}}}
                                {{/each}}
                                <div style="background-color: #2a2a3e; padding: 8; anchor-height: 40; flex-direction: row;">
                                    {{#if !isAdmin}}
                                    <p style="color: #888888; flex-weight: 1;">Admin mode disabled</p>
                                    {{else}}
                                    <p style="color: #4CAF50; flex-weight: 1;">Admin mode enabled</p>
                                    {{/if}}
                                    {{#if showcaseTags contains "Legend"}}
                                    <p style="color: #FFD54F; flex-weight: 1;">Legend tag active</p>
                                    {{/if}}
                                </div>
                            </div>
                        </div>
                
                        <div id="timers-content" class="tab-content" data-hyui-tab-id="timers" style="flex-direction: column;">
                            <!-- Section 2: Timers -->
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">2. Timer Formats</p>
                            <div style="flex-direction: row; anchor-height: 80;">
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 19;">MM:SS</p>
                                    <timer style="color: #00BCD4; font-size: 26;" value="185000" format="ms"></timer>
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 19;">HH:MM:SS</p>
                                    <timer style="color: #00BCD4; font-size: 26;" value="7325000" format="hms"></timer>
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 19;">Human</p>
                                    <timer style="color: #00BCD4; font-size: 26;" value="7325000" format="human"></timer>
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 19;">Prefix/Suffix</p>
                                    <timer style="color: #00BCD4; font-size: 26;" value="300000" format="ms" prefix="Time: " suffix=" left"></timer>
                                </div>
                            </div>
                        </div>
                
                        <div id="components-content" class="tab-content" data-hyui-tab-id="components" style="flex-direction: column;">
                            <!-- Section 3: Components -->
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">3. Reusable Components</p>
                            <div style="flex-direction: row; anchor-height: 70;">
                                {{@statCard:label=Blocks Placed,value=12.847}}
                                {{@statCard:label=Creatures Found,value=23}}
                                {{@statCard:label=Recipes Learned,value=156}}
                                {{@statCard:label=Zones Explored,value=4/6}}
                            </div>
                
                            <!-- Section 4: Features -->
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">4. New in HyUI 0.5.0</p>
                            <div style="flex-direction: column; anchor-height: 130;">
                                {{@featureItem:text=TemplateProcessor for variable interpolation}}
                                {{@featureItem:text=Built-in filters (upper/lower/number/percent)}}
                                {{@featureItem:text=Reusable components with parameters}}
                                {{@featureItem:text=TimerLabelBuilder with multiple formats}}
                            </div>
                
                            <!-- Section 5: Input Elements -->
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">5. Input Elements</p>
                            <div style="flex-direction: row; anchor-height: 150; gap: 10;">
                                <div style="background-color: #2a2a3e; padding: 15; anchor-width: 200; flex-direction: column; align-items: center;">
                                    <p style="color: #888888; font-size: 19; anchor-height: 20;">Color Picker</p>
                                    <input type="color" id="demo-color" value="#4CAF50" style="anchor-width: 170; anchor-height: 110;" />
                                </div>
                                <div style="background-color: #2a2a3e; padding: 15; flex-weight: 2; flex-direction: column;">
                                    <p style="color: #888888; font-size: 19; anchor-height: 20;">Volume Slider</p>
                                    <input type="range" id="demo-slider" min="0" max="100" value="75" style="anchor-height: 30;" />
                                </div>
                                <div style="background-color: #2a2a3e; padding: 15; flex-weight: 1; flex-direction: column; align-items: center;">
                                    <p style="color: #888888; font-size: 19; anchor-height: 20;">Checkbox</p>
                                    <input type="checkbox" id="demo-checkbox" checked style="anchor-height: 30;" />
                                </div>
                            </div>
                
                            <!-- Interactive Demo -->
                            <div style="flex-direction: row; anchor-height: 60;">
                                <button id="demo-button" style="flex-weight: 1;">Click Me!</button>
                                <p style="color: #888888; flex-weight: 2; padding: 15;">Click to test event handling</p>
                            </div>
                        </div>
                
                        <div id="items-content" class="tab-content" data-hyui-tab-id="items" style="flex-direction: column;">
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">5. Item Display</p>
                            <div style="flex-direction: row; anchor-height: 200; gap: 10;">
                                <div style="background-color: #2a2a3e; padding: 15; flex-direction: column; align-items: center;">
                                    <p style="color: #888888; font-size: 19; anchor-height: 20;">Single Item</p>
                                    <span class="item-slot" data-hyui-item-id="Tool_Pickaxe_Crude" style="anchor-width: 64; anchor-height: 64;"></span>
                                </div>
                                <div style="background-color: #2a2a3e; padding: 15; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 19; anchor-height: 20;">Item Grid (scrollable)</p>
                                    <div class="item-grid" data-hyui-style="SlotSpacing: 8" data-hyui-slots-per-row="4" data-hyui-show-scrollbar="true" style="anchor-height: 150;">
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="1"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="2"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="3"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="4"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="5"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="6"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="7"></div>
                                        <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-quantity="8"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                
                        <div id="inputs-content" class="tab-content" data-hyui-tab-id="inputs" style="flex-direction: column;">
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold; anchor-height: 30;">6. Input Types</p>
                            <div style="flex-direction: row; anchor-height: 320; gap: 10;">
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Text</p>
                                    <input type="text" value="Hello" placeholder="Text input" style="anchor-height: 30;" />
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Number</p>
                                    <input type="number" value="12" min="0" max="100" step="1" data-hyui-max-decimal-places="0" style="anchor-height: 30; anchor-width: 50;" />
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Dropdown</p>
                                    <select value="Entry1" data-hyui-showlabel="true" style="anchor-height: 40;">
                                        <option value="Entry1">First Entry</option>
                                        <option value="Entry2">Second Entry</option>
                                        <option value="Entry3">Third Entry</option>
                                    </select>
                                </div>
                                <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Range</p>
                                    <input type="range" min="0" max="100" step="5" value="40" style="anchor-height: 30;" />
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Checkbox</p>
                                    <input type="checkbox" checked style="anchor-height: 30;" />
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Color</p>
                                    <input type="color" value="#4CAF50" style="anchor-width: 120; anchor-height: 40;" />
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Circular Progress</p>
                                    <progress class="circular-progress"
                                              value="0.65"
                                              data-hyui-mask-texture-path="Assets/FrameAbilityOnUseState.png"
                                              data-hyui-color="#5fd4e5"
                                              style="anchor-width: 64; anchor-height: 64;">
                                    </progress>
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Reset</p>
                                    <input type="reset" value="Reset" style="anchor-height: 30;" />
                                    <p style="color: #888888; font-size: 18; anchor-height: 18;">Sprite</p>
                                    <sprite src="Common/Spinner.png"
                                            data-hyui-frame-width="32"
                                            data-hyui-frame-height="32"
                                            data-hyui-frame-per-row="8"
                                            data-hyui-frame-count="72"
                                            data-hyui-fps="30"
                                            style="anchor-width: 32; anchor-height: 32;">
                                    </sprite>
                                </div>
                            </div>
                        </div>
                    </div>
                
                    <!-- Footer -->
                    <div style="anchor-height: 30;">
                        <p style="color: #666666; font-size: 18;">HyUI 0.5.0 | {{$currentTime}} on {{$currentDate}}</p>
                    </div>
                </div>
                """;
    }

    private static final class ShowcaseItem {
        private final String name;
        private final int power;
        private final String rarity;
        private final ShowcaseMeta meta;

        private ShowcaseItem(String name, int power, String rarity, ShowcaseMeta meta) {
            this.name = name;
            this.power = power;
            this.rarity = rarity;
            this.meta = meta;
        }

        public String getName() {
            if (!HyUIPluginLogger.LOGGING_ENABLED) {
                return null;
            }
            return name;
        }

        public int getPower() {
            if (!HyUIPluginLogger.LOGGING_ENABLED) {
                return 0;
            }
            return power;
        }

        public String getRarity() {
            if (!HyUIPluginLogger.LOGGING_ENABLED) {
                return null;
            }
            return rarity;
        }

        public ShowcaseMeta getMeta() {
            if (!HyUIPluginLogger.LOGGING_ENABLED) {
                return null;
            }
            return meta;
        }
    }

    private static final class ShowcaseMeta {
        private final String tier;
        private final String source;

        private ShowcaseMeta(String tier, String source) {
            this.tier = tier;
            this.source = source;
        }

        public String getTier() {
            if (!HyUIPluginLogger.LOGGING_ENABLED) {
                return null;
            }
            return tier;
        }

        public String getSource() {
            if (!HyUIPluginLogger.LOGGING_ENABLED) {
                return null;
            }
            return source;
        }
    }
}
