package au.ellie.hyui.utils.multiplehud;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Map;

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
public class MultipleHUD {

    private static MultipleHUD instance;

    public static MultipleHUD getInstance() {
        if (instance == null) {
            instance = new MultipleHUD();
        }
        return instance;
    }
    
    public HytaleLogger getLogger() {
        return HytaleLogger.forEnclosingClass();
    }

    public MultipleHUD() {
        instance = this;
    }


    public void setCustomHud(Player player, PlayerRef playerRef, String hudIdentifier, CustomUIHud customHud) {
        player.getHudManager().addCustomHud(playerRef, customHud);
    }

    @Deprecated
    public void hideCustomHud(Player player, PlayerRef playerRef, String hudIdentifier) {
        hideCustomHud(player, hudIdentifier);
    }
    public void hideCustomHud(Player player, String hudIdentifier) {
        player.getHudManager().removeCustomHud(player.getPlayerRef(), hudIdentifier);
    }
    
    public Map<String, CustomUIHud> getCustomHuds(Player player) {
        return player.getHudManager().getCustomHuds();
    }
}