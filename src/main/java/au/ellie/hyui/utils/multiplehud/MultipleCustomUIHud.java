package au.ellie.hyui.utils.multiplehud;

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/*
MIT License

Copyright (c) 2025 Buuz135

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
public class MultipleCustomUIHud extends CustomUIHud {

    private static Method BUILD_METHOD;
    private static Field COMMANDS_FIELD;

    static {
        try {
            BUILD_METHOD = CustomUIHud.class.getDeclaredMethod("build", UICommandBuilder.class);
            BUILD_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            BUILD_METHOD = null;
            MultipleHUD.getInstance().getLogger().at(Level.SEVERE).log("Could not find method 'build' in CustomUIHud");
            MultipleHUD.getInstance().getLogger().at(Level.SEVERE).log(e.getMessage());
        }

        try {
            COMMANDS_FIELD = UICommandBuilder.class.getDeclaredField("commands");
            COMMANDS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            COMMANDS_FIELD = null;
            MultipleHUD.getInstance().getLogger().at(Level.SEVERE).log("Could not find field 'commands' in UICommandBuilder");
            MultipleHUD.getInstance().getLogger().at(Level.SEVERE).log(e.getMessage());
        }
    }

    private static class PrefixedUICommandBuilder extends UICommandBuilder {
        private final List<CustomUICommand> wrappedCommands = new ObjectArrayList();
        private final String prefix;

        public PrefixedUICommandBuilder(@NonNullDecl String id) {
            this.prefix = "#MultipleHUD #" + id;;
        }

        public String getPrefix() {
            return this.prefix;
        }

        private void prefixCommands() throws IllegalAccessException {
            final List<CustomUICommand> commands =
                    (List<CustomUICommand>) COMMANDS_FIELD.get(this);

            for (int i = 0, n = commands.size(); i < n; i++) {
                CustomUICommand command = commands.get(i);
                if (command.selector == null) {
                    command.selector = this.prefix;
                } else {
                    command.selector = this.prefix + ' ' + command.selector;
                }
                wrappedCommands.add(command);
            }
            commands.clear();
        }

        // this will be called by update method of CustomUIHud
        // here as a workaround of buggy huds. shouldn't be necessary normally.
        @Override
        @Nonnull
        public CustomUICommand[] getCommands() {
            try {
                this.prefixCommands();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            CustomUICommand[] commands = wrappedCommands.toArray(new CustomUICommand[0]);
            // we need to clear the commands if the hud mod author decided to call update himself.
            wrappedCommands.clear();
            return commands;
        }

        void appendCommandsTo (UICommandBuilder builder) throws IllegalAccessException {
            this.prefixCommands();
            final List<CustomUICommand> commands =
                    (List<CustomUICommand>) COMMANDS_FIELD.get(builder);
            commands.addAll(this.wrappedCommands);
        }

        void addCustomCommand (CustomUICommandType type, String selector, String document) {
            this.wrappedCommands.add(new CustomUICommand(type, selector, null, document));
        }
    }

    static void buildHud (
            @Nonnull UICommandBuilder uiCommandBuilder,
            @NonNullDecl String normalizedId,
            @Nonnull CustomUIHud hud,
            boolean hudExists
    ) {
        try {
            if (BUILD_METHOD == null || COMMANDS_FIELD == null) return;
            PrefixedUICommandBuilder singleHudBuilder = new PrefixedUICommandBuilder(normalizedId);
            if (hudExists) {
                singleHudBuilder.addCustomCommand(CustomUICommandType.Clear, singleHudBuilder.getPrefix(), null);
            } else {
                singleHudBuilder.addCustomCommand(CustomUICommandType.AppendInline, "#MultipleHUD","Group #" + normalizedId + " {}");
            }
            BUILD_METHOD.invoke(hud, singleHudBuilder);
            singleHudBuilder.appendCommandsTo(uiCommandBuilder);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // key is the id as provided by mod, value is normalized id to be compatible with hud.
    private final HashMap<String, String> normalizedIds = new HashMap<>();
    private final HashMap<String, CustomUIHud> customHuds = new HashMap<>();

    public MultipleCustomUIHud(@NonNullDecl PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("HUD/MultipleHUD.ui");
        // individual hud renders will be handled by the `add` method.
        // full re-renders can be triggered by the `show` method.
    }

    @Override
    public void show() {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        this.build(commandBuilder);
        for (String identifier : customHuds.keySet()) {
            String normalizedId = normalizedIds.get(identifier);
            CustomUIHud hud = customHuds.get(identifier);
            buildHud(commandBuilder, normalizedId, hud, false);
        }
        this.update(true, commandBuilder);
    }

    public void add (@NonNullDecl String identifier, @NonNullDecl CustomUIHud hud) {
        UICommandBuilder commandBuilder = new UICommandBuilder();

        String normalizedId = normalizedIds.computeIfAbsent(identifier, i -> i.replaceAll("[^a-zA-Z0-9]", ""));
        CustomUIHud existingHud = customHuds.put(identifier, hud);
        if (existingHud != hud) {
            customHuds.put(identifier, hud);
        }

        buildHud(commandBuilder, normalizedId, hud, existingHud != null);
        update(false, commandBuilder);
    }

    public void remove (@NonNullDecl String identifier) {
        String normalizedId = normalizedIds.get(identifier);
        boolean shownBefore = normalizedId != null;
        if (!shownBefore) return;
        normalizedIds.remove(identifier);
        customHuds.remove(identifier);
        UICommandBuilder commandBuilder = new UICommandBuilder();
        commandBuilder.remove("#MultipleHUD #" + normalizedId);
        update(false, commandBuilder);
    }
}
