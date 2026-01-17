package au.ellie.hyui;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class HyUIPlugin extends JavaPlugin {
    private final HytaleLogger internalLogger = HytaleLogger.forEnclosingClass();

    private static HyUIPlugin instance;
    
    public static boolean loggingEnabled = false;
    
    public static HyUIPlugin getInstance() {
        return instance;
    }

    public HyUIPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        loggingEnabled = false;
    }

    public void logInfo(String message) {
        if (loggingEnabled) {
            internalLogger.atInfo().log(message);
        }
    }
    
    @Override
    protected void setup() {
        if (loggingEnabled) {
            internalLogger.atInfo().log("Setting up plugin " + this.getName());
            this.getCommandRegistry().registerCommand(new au.ellie.hyui.commands.HyUITestGuiCommand());
        }
    }
}
