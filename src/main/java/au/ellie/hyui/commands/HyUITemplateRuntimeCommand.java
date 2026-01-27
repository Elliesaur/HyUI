package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.builders.HyvatarImageBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class HyUITemplateRuntimeCommand extends AbstractAsyncCommand {

    public HyUITemplateRuntimeCommand() {
        super("tr", "Shows live template values based on element IDs");
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
                        openTemplateRuntimeDemo(playerRef, store);
                    }
                }, world);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private void openTemplateRuntimeDemo(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return;
        }

        String html = """
                <div class="page-overlay">
                    <div class="container" style="anchor-width: 900; anchor-height: 980">
                        <div style="background-color: #1a1a2e(0.95); anchor: 0; padding: 20; flex-direction: column;">
                            <p style="color: #4CAF50; font-size: 24; font-weight: bold;">Template Runtime Values</p>
                            <p style="color: #cccccc;">Other value: {{$other|unset}}</p>
            
                            <div style="flex-direction: row; padding-top: 12;">
                                <div style="flex-direction: column; anchor-width: 250;">
                                    <p style="color: #888888; font-size: 18;">Can Move</p>
                                    <select id="canMove" data-hyui-showlabel="true">
                                        <option value="0" {{#if !other || other == 0}}selected{{/if}}>True</option>
                                        <option value="1" {{#if other == 1}}selected{{/if}}>False</option>
                                    </select>
                                </div>
                                <div style="flex-direction: column; anchor-width: 250; padding-left: 20;">
                                    <p style="color: #888888; font-size: 18;">Other</p>
                                    <select id="other" data-hyui-showlabel="true" value="0">
                                        <option value="0">Attached</option>
                                        <option value="1">Unattached</option>
                                    </select>
                                </div>
                            </div>
            
                            <div style="flex-direction: row; background-color: #16213e(0.6); padding: 12; margin-top: 12;">
                                <div style="anchor-width: 160; anchor-height: 160; align-items: center; justify-content: center;">
                                    <hyvatar id="profile-hyvatar" username="{{$displayName|Guest}}" render="head" size="128" rotate="15"></hyvatar>
                                </div>
                                <div style="flex-direction: column; flex-weight: 1; padding-left: 20;">
                                    <p style="color: #4CAF50; font-size: 20; font-weight: bold;">Live Form</p>
                                    <p style="color: #cccccc;">Name: {{$displayName|Unknown}}</p>
                                    <p style="color: #cccccc;">Level: {{$level|0}}</p>
                                    <p style="color: #cccccc;">Volume: {{$volume|0}}</p>
                                    <p style="color: #cccccc;">Color: {{$favColor|#ffffff}}</p>
                                    {{#if termsAccepted}}
                                    <p style="color: #4CAF50;">Terms accepted</p>
                                    {{else}}
                                    <p style="color: #ff7777;">Terms not accepted</p>
                                    {{/if}}
                                </div>
                            </div>
            
                            <div style="flex-direction: column; padding-top: 12;">
                                <div style="flex-direction: row; align-items: center;">
                                    <label style="anchor-width: 120; color: #888888;">Display Name</label>
                                    <input type="text" id="displayName" value="Elyra" placeholder="Player name" style="flex-weight: 1;" />
                                </div>
                                <div style="flex-direction: row; align-items: center; padding-top: 8;">
                                    <label style="anchor-width: 120; color: #888888;">Level</label>
                                    <input type="number" id="level" value="12" min="1" max="100" step="1" style="anchor-width: 140;" />
                                    <label style="anchor-width: 100; color: #888888; padding-left: 10;">Volume</label>
                                    <input type="range" id="volume" value="35" min="0" max="100" step="5" style="flex-weight: 1;" />
                                </div>
                                <div style="flex-direction: row; align-items: center; padding-top: 8;">
                                    <label style="anchor-width: 120; color: #888888;">Favorite Color</label>
                                    <input type="color" id="favColor" value="#44ccff" style="anchor-width: 50; anchor-height: 50;"/>
                                    <label style="anchor-width: 140; color: #888888; padding-left: 10;">Accept Terms</label>
                                    <input type="checkbox" id="termsAccepted" checked="false" />
                                </div>
                            </div>
            
                            <p style="color: #888888; padding-top: 10;">Change any field to reprocess the template and update live values.</p>
                        </div>
                    </div>
                </div>
                
                """;

        TemplateProcessor template = new TemplateProcessor()
                .setVariable("playerName", playerRef.getUsername())
                .setVariable("displayName", playerRef.getUsername());

        PageBuilder builder = PageBuilder.pageForPlayer(playerRef)
                .fromTemplate(html, template)
                .enableRuntimeTemplateUpdates(true)
                .withLifetime(CustomPageLifetime.CanDismiss);

        builder.addEventListener("other", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            HyUIPlugin.getLog().logInfo("REBUILD: template runtime update (other)");
            ctx.updatePage(false);
        });
        builder.addEventListener("displayName", CustomUIEventBindingType.FocusLost, (_, ctx) -> {
            HyUIPlugin.getLog().logInfo("REBUILD: template runtime update (displayName)");
            ctx.getValue("displayName", String.class).ifPresent(name -> {
                ctx.getByIdAs("profile-hyvatar", HyvatarImageBuilder.class)
                        .ifPresent(image -> {
                            if (!name.equals(image.getUsername())) {
                                image.withUsername(name);
                                ctx.getPage().ifPresent(page -> page.reloadImage("profile-hyvatar", false));
                            }
                        });
            });
        });
        builder.addEventListener("level", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            HyUIPlugin.getLog().logInfo("REBUILD: template runtime update (level)");
            ctx.updatePage(false);
        });
        builder.addEventListener("volume", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            HyUIPlugin.getLog().logInfo("REBUILD: template runtime update (volume)");
            ctx.updatePage(false);
        });
        builder.addEventListener("favColor", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            HyUIPlugin.getLog().logInfo("REBUILD: template runtime update (favColor)");
            ctx.updatePage(false);
        });
        builder.addEventListener("termsAccepted", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            HyUIPlugin.getLog().logInfo("REBUILD: template runtime update (termsAccepted)");
            ctx.updatePage(false);
        });

        builder.open(store);
    }
}
