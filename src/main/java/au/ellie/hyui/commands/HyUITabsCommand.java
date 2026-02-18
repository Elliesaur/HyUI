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

import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.builders.TabNavigationBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class HyUITabsCommand extends AbstractDevCommand {

    public HyUITabsCommand() {
        super("tabs", "Opens the HyUI tabs tutorial demo");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeDev(Player player, Ref<EntityStore> ref, CommandContext commandContext) {
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null)
                openTabsDemo(playerRef, store);
        }, world);
    }

    private void openTabsDemo(PlayerRef playerRef, Store<EntityStore> store) {
        var html = """
                <div class="page-overlay">
                    <div class="decorated-container" data-hyui-title="Workshop Tabs" style="anchor-width: 720; anchor-height: 480;">
                        <div class="container-contents" style="layout-mode: Top; padding: 6;">
                            <nav id="workshop-tabs" class="tabs"
                                 data-tabs="blueprints:Blueprints:blueprints-content,materials:Materials:materials-content{{if $isAdmin}},tools:Tools:tools-content{{/if}}"
                                 data-selected="blueprints">
                            </nav>
                
                            <div id="blueprints-content" class="tab-content" data-hyui-tab-id="blueprints">
                                <p>Blueprint drafts live here.</p>
                            </div>
                
                            <div id="materials-content" class="tab-content" data-hyui-tab-id="materials">
                                <p>Material stacks and salvage.</p>
                                <myComponent />
                            </div>
                
                            <div if="$isAdmin" id="tools-content" class="tab-content" data-hyui-tab-id="tools">
                                <p>Workbench tools and kits.</p>
                            </div>
                
                            <button id="upgrade-tabs" class="secondary-button">Upgrade Materials Tab</button>
                        </div>
                    </div>
                </div>
                """;

        var template = new TemplateProcessor()
                .setVariable("isAdmin", false)
                .registerComponent("mySubComponent",
                        """
                                <p style="padding-top: 20;">Hello subComponent!</p>
                                """)
                .registerComponent("myComponent",
                        """
                                <div>
                                    <mySubComponent />
                                </div>
                                """);

        PageBuilder.detachedPage()
                .withLifetime(CustomPageLifetime.CanDismiss)
                .fromHtml(html)
                .open(playerRef, store);

        var builder = PageBuilder.pageForPlayer(playerRef)
                .fromTemplate(html, template)
                .enableRuntimeTemplateUpdates(true)
                .withLifetime(CustomPageLifetime.CanDismiss);

        builder.addEventListener("upgrade-tabs", CustomUIEventBindingType.Activating, (data, ctx) -> {
            ctx.getById("workshop-tabs", TabNavigationBuilder.class).ifPresent(nav -> {
                var existing = nav.getTab("materials");
                if (existing == null)
                    return;

                ButtonBuilder customButton = ButtonBuilder.smallTertiaryTextButton();
                var updated = new TabNavigationBuilder.Tab(
                        existing.id(),
                        "Materials+",
                        existing.contentId(),
                        existing.selected(),
                        customButton
                );

                nav.updateTab("materials", updated);
            });

            ctx.updatePage(true);
        });

        builder.open(store);
    }
}
