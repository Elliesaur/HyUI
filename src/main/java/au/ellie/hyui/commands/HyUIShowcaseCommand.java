package au.ellie.hyui.commands;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HyUIShowcaseCommand extends AbstractAsyncCommand {

    public HyUIShowcaseCommand() {
        super("showcase", "Opens the HyUI 0.5.0 feature showcase");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
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

        builder.open(store);
    }

    private TemplateProcessor createTemplateProcessor(PlayerRef playerRef) {
        return new TemplateProcessor()
                // Player variables
                .setVariable("playerName", playerRef.getUsername())
                .setVariable("playerLevel", 42)
                .setVariable("playerXp", 8750)
                .setVariable("playerGold", 12500)
                .setVariable("playerHealth", 85)
                .setVariable("playerMaxHealth", 100)

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
                    <p style="color: #888888; font-size: 15;">{{$label}}</p>
                    <p style="color: #ffffff; font-size: 22; font-weight: bold;">{{$value}}</p>
                </div>
                """)

                .registerComponent("featureItem", """
                <div style="flex-direction: row; padding: 5; anchor-height: 30;">
                    <p style="color: #4CAF50; anchor-width: 20;">*</p>
                    <p style="color: #cccccc; flex-weight: 1;">{{$text}}</p>
                </div>
                """);
    }

    private String createShowcaseHtml() {
        return """
            <div style="background-color: #1a1a2e(0.95); anchor: 0; padding: 20; flex-direction: column;">
                <!-- Header -->
                <div style="anchor-height: 80; flex-direction: column;">
                    <p style="color: #4CAF50; font-size: 28; font-weight: bold;">HyUI 0.5.0 Feature Showcase</p>
                    <p style="color: #888888; font-size: 18;">Welcome, {{$playerName|Guest}}!</p>
                </div>

                <!-- Tab Navigation -->
                <nav class="tabs" data-tabs="templates:Templates:templates-content,timers:Timers:timers-content,components:Components:components-content,items:Items:items-content" data-selected="templates" style="anchor-height: 50;">
                </nav>

                <!-- Content Area -->
                <div style="flex-weight: 1; padding: 15; background-color: #16213e(0.5); flex-direction: column;">

                    <div id="templates-content" class="tab-content" data-hyui-tab-id="templates" style="flex-direction: column;">
                        <!-- Section 1: Template Variables -->
                        <p style="color: #4CAF50; font-size: 20; font-weight: bold; anchor-height: 30;">1. Template Variables</p>
                        <div style="flex-direction: row; anchor-height: 100;">
                            <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                <p style="color: #ffffff; font-size: 16;">Player: {{$playerName}}</p>
                                <p style="color: #ffffff; font-size: 16;">Level: {{$playerLevel}}</p>
                                <p style="color: #ffffff; font-size: 16;">Gold: {{$playerGold|number}}</p>
                            </div>
                            <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                <p style="color: #ffffff; font-size: 16;">Upper: {{$playerName|upper}}</p>
                                <p style="color: #ffffff; font-size: 16;">Lower: {{$playerName|lower}}</p>
                                <p style="color: #ffffff; font-size: 16;">Default: {{$missing|Not Set}}</p>
                            </div>
                        </div>
                    </div>

                    <div id="timers-content" class="tab-content" data-hyui-tab-id="timers" style="flex-direction: column;">
                        <!-- Section 2: Timers -->
                        <p style="color: #4CAF50; font-size: 20; font-weight: bold; anchor-height: 30;">2. Timer Formats</p>
                        <div style="flex-direction: row; anchor-height: 80;">
                            <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                <p style="color: #888888; font-size: 15;">MM:SS</p>
                                <timer style="color: #00BCD4; font-size: 22;" value="185000" format="ms"></timer>
                            </div>
                            <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                <p style="color: #888888; font-size: 15;">HH:MM:SS</p>
                                <timer style="color: #00BCD4; font-size: 22;" value="7325000" format="hms"></timer>
                            </div>
                            <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                <p style="color: #888888; font-size: 15;">Human</p>
                                <timer style="color: #00BCD4; font-size: 22;" value="7325000" format="human"></timer>
                            </div>
                            <div style="background-color: #2a2a3e; padding: 10; flex-weight: 1; flex-direction: column;">
                                <p style="color: #888888; font-size: 15;">Prefix/Suffix</p>
                                <timer style="color: #00BCD4; font-size: 22;" value="300000" format="ms" prefix="Time: " suffix=" left"></timer>
                            </div>
                        </div>
                    </div>

                    <div id="components-content" class="tab-content" data-hyui-tab-id="components" style="flex-direction: column;">
                        <!-- Section 3: Components -->
                        <p style="color: #4CAF50; font-size: 20; font-weight: bold; anchor-height: 30;">3. Reusable Components</p>
                        <div style="flex-direction: row; anchor-height: 70;">
                            {{@statCard:label=Blocks Placed,value=12.847}}
                            {{@statCard:label=Creatures Found,value=23}}
                            {{@statCard:label=Recipes Learned,value=156}}
                            {{@statCard:label=Zones Explored,value=4/6}}
                        </div>

                        <!-- Section 4: Features -->
                        <p style="color: #4CAF50; font-size: 20; font-weight: bold; anchor-height: 30;">4. New in HyUI 0.5.0</p>
                        <div style="flex-direction: column; anchor-height: 130;">
                            {{@featureItem:text=TemplateProcessor for variable interpolation}}
                            {{@featureItem:text=Built-in filters (upper/lower/number/percent)}}
                            {{@featureItem:text=Reusable components with parameters}}
                            {{@featureItem:text=TimerLabelBuilder with multiple formats}}
                        </div>

                        <!-- Section 5: Input Elements -->
                        <p style="color: #4CAF50; font-size: 20; font-weight: bold; anchor-height: 30;">5. Input Elements</p>
                        <div style="flex-direction: row; anchor-height: 150; gap: 10;">
                            <div style="background-color: #2a2a3e; padding: 15; anchor-width: 200; flex-direction: column; align-items: center;">
                                <p style="color: #888888; font-size: 15; anchor-height: 20;">Color Picker</p>
                                <input type="color" id="demo-color" value="#4CAF50" style="anchor-width: 170; anchor-height: 110;" />
                            </div>
                            <div style="background-color: #2a2a3e; padding: 15; flex-weight: 2; flex-direction: column;">
                                <p style="color: #888888; font-size: 15; anchor-height: 20;">Volume Slider</p>
                                <input type="range" id="demo-slider" min="0" max="100" value="75" style="anchor-height: 30;" />
                            </div>
                            <div style="background-color: #2a2a3e; padding: 15; flex-weight: 1; flex-direction: column; align-items: center;">
                                <p style="color: #888888; font-size: 15; anchor-height: 20;">Checkbox</p>
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
                        <p style="color: #4CAF50; font-size: 20; font-weight: bold; anchor-height: 30;">5. Item Display</p>
                        <div style="flex-direction: row; anchor-height: 200; gap: 10;">
                            <div style="background-color: #2a2a3e; padding: 15; flex-direction: column; align-items: center;">
                                <p style="color: #888888; font-size: 15; anchor-height: 20;">Single Item</p>
                                <span class="item-slot" data-hyui-item-id="Tool_Pickaxe_Crude" style="anchor-width: 64; anchor-height: 64;"></span>
                            </div>
                            <div style="background-color: #2a2a3e; padding: 15; flex-weight: 1; flex-direction: column;">
                                <p style="color: #888888; font-size: 15; anchor-height: 20;">Item Grid (scrollable)</p>
                                <div class="item-grid" data-hyui-slots-per-row="4" data-hyui-show-scrollbar="true" style="anchor-height: 150;">
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
                </div>

                <!-- Footer -->
                <div style="anchor-height: 30;">
                    <p style="color: #666666; font-size: 14;">HyUI 0.5.0 | {{$currentTime}} on {{$currentDate}}</p>
                </div>
            </div>
            """;
    }
}
