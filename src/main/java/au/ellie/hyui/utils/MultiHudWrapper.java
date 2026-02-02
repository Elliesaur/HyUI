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

package au.ellie.hyui.utils;

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handles MultiHUD operations by checking for existing plugins using MultipleHUD and hooking their copy over ours.
 */
public class MultiHudWrapper {
    
    private static Class<?> mhudClass;
    private static Semver mhudSemver;
    private static boolean useOwnMHUD;
    
    private static Object getInstance() {
        Class<?> multipleHudClass = getMultipleHudClass();
        if (multipleHudClass == null) {
            HyUIPlugin.getLog().logFinest("Could not find MultipleHUD plugin, using own implementation. THIS MAY BREAK OTHER MODS!");
            useOwnMHUD = true;
            mhudClass = au.ellie.hyui.utils.multiplehud.MultipleHUD.class;
            mhudSemver = Semver.fromString("1.0.3");
            return au.ellie.hyui.utils.multiplehud.MultipleHUD.getInstance();
        }
        Method getInstanceMethod = null;
        try {
            getInstanceMethod = multipleHudClass.getDeclaredMethod("getInstance");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        HyUIPlugin.getLog().logFinest("getInstance method found");
        Object multipleHudInstance = null;
        try {
            multipleHudInstance = getInstanceMethod.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        HyUIPlugin.getLog().logFinest("MultipleHUD instance retrieved");
        if (multipleHudInstance == null) {
            // TODO: load MHUD ourselves... We know there is absolutely no MHUD installed.
            //  For now, we just need to write our own small implementation.
            useOwnMHUD = true;
            multipleHudInstance = au.ellie.hyui.utils.multiplehud.MultipleHUD.getInstance();
        }
        return multipleHudInstance;
    }

    @NullableDecl
    private static Class<?> getMultipleHudClass() {
        HyUIPlugin.getLog().logFinest("Attempting to find MultipleHUD class");
        Class<?> multipleHudClass = null;
        try {
            multipleHudClass = Class.forName("com.buuz135.mhud.MultipleHUD");
        } catch (ClassNotFoundException _) {
        }
        var existingPlugin = PluginManager.get().getPlugin(new PluginIdentifier("Buuz135", "MultipleHUD"));
        if (existingPlugin != null) {
            HyUIPlugin.getLog().logFinest("MultipleHUD plugin found: " + existingPlugin.getManifest().getVersion().toString());
            multipleHudClass = existingPlugin.getClass();
            mhudSemver = existingPlugin.getManifest().getVersion();
        }
        if (multipleHudClass != null) {
            HyUIPlugin.getLog().logFinest("MultipleHUD class found");
            mhudClass = multipleHudClass;
        }
        return multipleHudClass;
    }

    public static void setCustomHud(Player player, PlayerRef playerRef, String name, CustomUIHud hud) {
        if (!useOwnMHUD) {
            var instance = getInstance();
            try {
                Class<?> multipleHudClass = mhudClass;
                Method setCustomHudMethod = multipleHudClass.getDeclaredMethod("setCustomHud", Player.class, PlayerRef.class, String.class, CustomUIHud.class);
                setCustomHudMethod.invoke(instance, player, playerRef, name, hud);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            au.ellie.hyui.utils.multiplehud.MultipleHUD.getInstance().setCustomHud(player, playerRef, name, hud);
        }
    }
    
    public static void hideCustomHud(Player player, PlayerRef playerRef, String name) {
        if (!useOwnMHUD) {
            var instance = getInstance();
            try {
                Class<?> multipleHudClass = mhudClass;
                // Let's still for now with this declared deprecated method.
                // In the future, we can skip to Player, String instead.
                Method setCustomHudMethod = multipleHudClass.getDeclaredMethod("hideCustomHud", Player.class, PlayerRef.class, String.class);
                setCustomHudMethod.invoke(instance, player, playerRef, name);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            au.ellie.hyui.utils.multiplehud.MultipleHUD.getInstance().hideCustomHud(player, playerRef, name);
        }
    }

    // For greater than 1.0.3 there is a new method.
    public static void hideCustomHud(Player player, String name) {
        if (!useOwnMHUD) {
            var instance = getInstance();
            try {
                Class<?> multipleHudClass = mhudClass;
                if (mhudSemver == null || !mhudSemver.satisfies(SemverRange.fromString(">=1.0.3"))) {
                    HyUIPlugin.getLog().logFinest("MultipleHUD version does not support hideCustomHud(Player, String) method");
                    return;
                }
                // Let's still for now with this declared deprecated method.
                // In the future, we can skip to Player, String instead.
                Method setCustomHudMethod = multipleHudClass.getDeclaredMethod("hideCustomHud", Player.class, String.class);
                setCustomHudMethod.invoke(instance, player, name);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            au.ellie.hyui.utils.multiplehud.MultipleHUD.getInstance().hideCustomHud(player, name);
        }
    }
}
