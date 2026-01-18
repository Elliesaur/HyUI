package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A CustomUIHud that can aggregate multiple HyUIHud instances.
 * This allows multiple HUD elements to coexist in the single HUD slot provided by Hytale.
 */
public class HyUIMultiHud extends CustomUIHud {
    private final Map<String, HyUIHud> huds = new LinkedHashMap<>();
    private final Map<String, HyUIHud> removedHuds = new LinkedHashMap<>();
    
    private final Map<String, Long> lastRefreshTimes = new LinkedHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask;

    private int unauthorizedBuildAttempts = 0;
    private int authorizedBuildAttempts = 0;
    private boolean periodicShowEnabled = true;

    public HyUIMultiHud(PlayerRef playerRef) {
        super(playerRef);
        startRefreshTask();
    }

    private void startRefreshTask() {
        if (refreshTask == null || refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleAtFixedRate(this::checkRefreshes, 100, 100, TimeUnit.MILLISECONDS);
        }
    }

    private void checkRefreshes() {
        PlayerRef playerRef = getPlayerRef();
        if (playerRef.getReference() == null) {
            // Player is no longer valid, cancel task and cleanup.
            if (refreshTask != null) {
                HyUIPlugin.getLog().logInfo("Player is invalid, cancelling refresh task for multi-hud.");
                refreshTask.cancel(false);
            }
            return;
        }

        boolean needsRefresh = false;
        long now = System.currentTimeMillis();
        
        synchronized (huds) {
            for (Map.Entry<String, HyUIHud> entry : huds.entrySet()) {
                String name = entry.getKey();
                HyUIHud hud = entry.getValue();
                long rate = hud.getRefreshRateMs();
                
                if (rate > 0) {
                    long lastRefresh = lastRefreshTimes.getOrDefault(name, 0L);
                    if (now - lastRefresh >= rate) {
                        hud.triggerRefresh();
                        needsRefresh = true;
                        lastRefreshTimes.put(name, now);
                    }
                }
            }
        }
        
        if (needsRefresh) {
            HyUIPlugin.getLog().logInfo("REFRESH.");
            //this.build(new UICommandBuilder());
            
            // Only show if we are the only ones really using this.
            if (periodicShowEnabled) {
                this.show();
            }
        }
    }

    /**
     * Adds or updates a HUD in this multi-hud.
     * @param hud The HyUIHud instance.
     */
    public void setHud(String name, HyUIHud hud) {
        synchronized (huds) {
            huds.put(name, hud);
            lastRefreshTimes.put(name, System.currentTimeMillis());
        }
        hud.showWithMultiHud(this);
    }

    /**
     * Removes a HUD from this multi-hud.
     * @param name The name of the HUD to remove.
     */
    public void removeHud(String name) {
        HyUIHud removed;
        synchronized (huds) {
            removed = huds.remove(name);
            lastRefreshTimes.remove(name);
        }
        if (removed != null) {
            removed.showWithMultiHud(null);
            HyUIPlugin.getLog().logInfo("REDRAW: HUD removed from multi-hud: " + name);
            // Redraw self.
            this.show();
        }
    }
    
    /**
     * Removes a HUD from this multi-hud.
     * @param instance The instance to remove.
     */
    public void removeHud(HyUIHud instance) {
        String keyToRemove = null;
        synchronized (huds) {
            for (Map.Entry<String, HyUIHud> entry : huds.entrySet()) {
                if (entry.getValue() == instance) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
        }
        if (keyToRemove != null) {
            removeHud(keyToRemove);
            return;
        }

        for (Map.Entry<String, HyUIHud> entry : removedHuds.entrySet()) {
            if (entry.getValue() == instance) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null) {
            removedHuds.remove(keyToRemove);
        }
        HyUIPlugin.getLog().logInfo("REDRAW: HUD removed from multi-hud: " + keyToRemove);
        // Redraw self.
        this.show();
    }

    /**
     * Hides a HUD from display by moving it to the removed list.
     * @param name The name of the HUD to hide.
     */
    public void hideHud(String name) {
        HyUIHud hud;
        synchronized (huds) {
            hud = huds.remove(name);
            lastRefreshTimes.remove(name);
        }
        if (hud != null) {
            removedHuds.put(name, hud);
            HyUIPlugin.getLog().logInfo("REDRAW: HUD hidden from multi-hud: " + name);
            // Redraw self.
            this.show();
        }
    }

    /**
     * Hides a HUD from display by moving it to the removed list.
     * @param instance The HUD instance to hide.
     */
    public void hideHud(HyUIHud instance) {
        String keyToHide = null;
        synchronized (huds) {
            for (Map.Entry<String, HyUIHud> entry : huds.entrySet()) {
                if (entry.getValue() == instance) {
                    keyToHide = entry.getKey();
                    break;
                }
            }
        }
        if (keyToHide != null) {
            hideHud(keyToHide);
        }
    }

    /**
     * Shows a HUD by moving it back to the main list.
     * @param name The name of the HUD to show.
     */
    public void showHud(String name) {
        HyUIHud hud = removedHuds.remove(name);
        if (hud != null) {
            synchronized (huds) {
                huds.put(name, hud);
                lastRefreshTimes.put(name, System.currentTimeMillis());
            }
            HyUIPlugin.getLog().logInfo("REDRAW: HUD shown from multi-hud: " + name);
            // Redraw self.
            this.show();
        }
    }

    /**
     * Shows a HUD by moving it back to the main list.
     * @param instance The HUD instance to show.
     */
    public void showHud(HyUIHud instance) {
        String keyToShow = null;
        for (Map.Entry<String, HyUIHud> entry : removedHuds.entrySet()) {
            if (entry.getValue() == instance) {
                keyToShow = entry.getKey();
                break;
            }
        }
        if (keyToShow != null) {
            showHud(keyToShow);
        }
    }

    @Override
    public void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        if (periodicShowEnabled && authorizedBuildAttempts < 10) {
            // Check who calls this.
            // If it is being called by something else that is 
            // not within the com.hypixel.* package or this class, track how many times it is called.
            boolean allowed = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .walk(frames -> frames
                            .skip(1)
                            .findFirst()
                            .map(StackWalker.StackFrame::getDeclaringClass)
                            .map(clazz -> clazz.getName().startsWith("com.hypixel.") || clazz.equals(HyUIMultiHud.class))
                            .orElse(false));

            if (!allowed) {
                unauthorizedBuildAttempts++;
                if (unauthorizedBuildAttempts < 1) {
                    HyUIPlugin.getLog().logInfo("BUILD IGNORED: Build called from outside Hypixel package (Attempt " + unauthorizedBuildAttempts + ")");
                    return;
                } else {
                    HyUIPlugin.getLog().logInfo("BUILD ALLOWED: Detected external HUD manager. Disabling periodic show.");
                    periodicShowEnabled = false;
                }
            } else {
                authorizedBuildAttempts++;
            }
        }

        synchronized (huds) {
            for (HyUIHud hud : huds.values()) {
                hud.build(uiCommandBuilder);
            }
        }
    }
}
