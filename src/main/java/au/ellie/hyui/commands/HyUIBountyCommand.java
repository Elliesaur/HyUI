/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.GroupBuilder;
import au.ellie.hyui.builders.LabelBuilder;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

public class HyUIBountyCommand extends AbstractAsyncCommand {
    private static final DemoMode DEMO_MODE = DemoMode.TemplateRuntime;

    private enum DemoMode {
        RuntimeEditing,
        TemplateInline,
        TemplateFromFile,
        TemplateRuntime
    }

    public HyUIBountyCommand() {
        super("bounty", "Opens the HyUI Bounty Board tutorial page");
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (!HyUIPluginLogger.IS_DEV) {
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
                        openBountyBoardDemo(playerRef, store);
                    }
                }, world);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private void openBountyBoard(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

        String html = """
            <div class="page-overlay">
                <div class="decorated-container" data-hyui-title="Bounty Board" style="anchor-width: 800; anchor-height: 600;">
                    <div class="container-contents" style="layout-mode: Top;">
                        <div id="filters" style="layout-mode: Left;">
                            <select id="region" data-hyui-showlabel="true" value="Forest" style="padding: 0 10;">
                                <option value="Forest">Forest</option>
                                <option value="Desert">Desert</option>
                                <option value="Tundra">Tundra</option>
                            </select>

                            <input id="minLevel" type="number" value="1" style="anchor-width: 60; padding: 0 10;" />

                            <button id="toggle-mode" style="padding: 0 10;" class="secondary-button">Compact View</button>
                            <button id="add-bounty" style="padding: 0 10;" class="secondary-button">Add Bounty</button>
                        </div>

                        <p id="summary" style="padding: 4;">Showing 3 bounties</p>

                        <div id="list" style="layout-mode: Top; padding: 6;">
                            <div class="bounty-card" style="layout-mode: Left; padding: 4;">
                                <p style="flex-weight: 2;">Slime Cleanup</p>
                                <p style="flex-weight: 1;">Lvl 1</p>
                                <button class="small-tertiary-button">Track</button>
                            </div>

                            <div class="bounty-card" style="layout-mode: Left; padding: 4;">
                                <p style="flex-weight: 2;">Bandit Camp</p>
                                <p style="flex-weight: 1;">Lvl 4</p>
                                <button class="small-tertiary-button">Track</button>
                            </div>

                            <div class="bounty-card" style="layout-mode: Left; padding: 4;">
                                <p style="flex-weight: 2;">Wisp Hunt</p>
                                <p style="flex-weight: 1;">Lvl 7</p>
                                <button class="small-tertiary-button">Track</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            """;

        AtomicBoolean compact = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger(3);

        PageBuilder builder = PageBuilder.pageForPlayer(playerRef)
            .fromHtml(html)
            .withLifetime(CustomPageLifetime.CanDismiss);

        builder.addEventListener("toggle-mode", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
            boolean newState = !compact.get();
            compact.set(newState);

            ctx.getById("list", GroupBuilder.class).ifPresent(list -> {
                list.withLayoutMode(newState ? "LeftCenterWrap" : "Top");
            });

            ctx.getById("summary", LabelBuilder.class).ifPresent(label -> {
                label.withText(newState ? "Compact view: 3 bounties" : "Showing 3 bounties");
            });

            ctx.getById("toggle-mode", ButtonBuilder.class).ifPresent(btn -> {
                btn.withText(newState ? "Comfy View" : "Compact View");
            });

            ctx.updatePage(true);
        });

        builder.addEventListener("minLevel", CustomUIEventBindingType.ValueChanged, (data, ctx) -> {
            String level = String.valueOf(data);
            ctx.getById("summary", LabelBuilder.class).ifPresent(label -> {
                label.withText("Min level: " + level + " (still 3 bounties)");
            });
            ctx.updatePage(true);
        });

        builder.addEventListener("add-bounty", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
            int next = counter.incrementAndGet();
            String title = "Urgent Bounty #" + next;
            int level = 2 + (next % 6);

            ctx.getById("list", GroupBuilder.class).ifPresent(list -> {
                list.addChild(buildBountyCard(title, level));
            });

            ctx.getById("summary", LabelBuilder.class).ifPresent(label -> {
                label.withText("Showing " + next + " bounties");
            });

            ctx.updatePage(true);
        });

        builder.open(store);
    }

    private void openBountyBoardDemo(PlayerRef playerRef, Store<EntityStore> store) {
        switch (DEMO_MODE) {
            case RuntimeEditing -> openBountyBoard(playerRef, store);
            case TemplateInline -> openBountyBoardWithTemplateProcessor(playerRef, store);
            case TemplateFromFile -> openBountyBoardFromTemplateFile(playerRef, store);
            case TemplateRuntime -> openBountyBoardWithRuntimeTemplateUpdates(playerRef, store);
        }
    }

    private void openBountyBoardWithTemplateProcessor(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

        List<Bounty> bounties = List.of(
            new Bounty("Slime Cleanup", 1, "Common"),
            new Bounty("Bandit Camp", 4, "Uncommon"),
            new Bounty("Wisp Hunt", 7, "Rare")
        );

        String html = """
            <div class="page-overlay">
                <div class="decorated-container" data-hyui-title="{{$title}}">
                    <div class="container-contents" style="layout-mode: Top;">
                        <p id="summary" style="padding: 4;">{{$summary}}</p>

                        <div id="list" style="layout-mode: Top; padding: 6;">
                            {{#each bounties}}
                            {{@bountyCard:title={{$title}},level={{$level}},rarity={{$rarity}}}}
                            {{/each}}
                        </div>
                    </div>
                </div>
            </div>
            """;

        TemplateProcessor template = createBountyTemplate(bounties);

        PageBuilder.pageForPlayer(playerRef)
            .fromTemplate(html, template)
            .withLifetime(CustomPageLifetime.CanDismiss)
            .open(store);
    }

    private void openBountyBoardFromTemplateFile(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

        List<Bounty> bounties = List.of(
            new Bounty("Slime Cleanup", 1, "Common"),
            new Bounty("Bandit Camp", 4, "Uncommon"),
            new Bounty("Wisp Hunt", 7, "Rare")
        );

        TemplateProcessor template = createBountyTemplate(bounties);

        PageBuilder.pageForPlayer(playerRef)
            .loadHtml("Pages/BountyBoard.html", template)
            .withLifetime(CustomPageLifetime.CanDismiss)
            .open(store);
    }

    private void openBountyBoardWithRuntimeTemplateUpdates(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

        List<Bounty> bounties = List.of(
            new Bounty("Slime Cleanup", 1, "Common"),
            new Bounty("Bandit Camp", 4, "Uncommon"),
            new Bounty("Wisp Hunt", 7, "Rare")
        );

        TemplateProcessor template = createBountyTemplate(bounties);

        PageBuilder builder = PageBuilder.pageForPlayer(playerRef)
            .loadHtml("Pages/BountyRuntime.html", template)
            .enableRuntimeTemplateUpdates(true)
            .withLifetime(CustomPageLifetime.CanDismiss);

        builder.addEventListener("region", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            // If we don't save our state here for minLevel, it will reset to default upon updatePage(true) calling.
            // YOU are responsible for tracking state. NOT HYUI!
            ctx.updatePage(true);
        });
        builder.addEventListener("minLevel", CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
            ctx.updatePage(false);
        });
        builder.addEventListener("close-board", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
            ctx.getPage().ifPresent(page -> page.close());
        });

        builder.open(store);
    }

    private TemplateProcessor createBountyTemplate(List<Bounty> bounties) {
        return new TemplateProcessor()
            .setVariable("title", "Bounty Board")
            .setVariable("summary", "Showing " + bounties.size() + " bounties")
            .setVariable("bounties", bounties)
            .registerComponent("bountyCard", """
                <div class="bounty-card" style="layout-mode: Left; padding: 4;">
                    <p style="flex-weight: 2;">{{$title}}</p>
                    <p style="flex-weight: 1;">Lvl {{$level}}</p>
                    {{#if level >= 6 || rarity == Rare}}
                    <p style="color: #4CAF50; flex-weight: 1;">Priority</p>
                    {{else}}
                    <p style="color: #888888; flex-weight: 1;">Standard</p>
                    {{/if}}
                    <button class="small-tertiary-button">Track</button>
                </div>
                """);
    }

    private static GroupBuilder buildBountyCard(String title, int level) {
        return GroupBuilder.group()
            .withLayoutMode("Left")
            .addChild(LabelBuilder.label()
                .withText(title)
                .withFlexWeight(2)
            )
            .addChild(LabelBuilder.label()
                .withText("Lvl " + level)
                .withFlexWeight(1)
            )
            .addChild(ButtonBuilder.smallTertiaryTextButton()
                .withText("Track")
            );
    }

    private record Bounty(String title, int level, String rarity) {}
}
