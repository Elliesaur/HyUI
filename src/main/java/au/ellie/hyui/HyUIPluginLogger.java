package au.ellie.hyui;

import com.hypixel.hytale.logger.HytaleLogger;

public class HyUIPluginLogger {
    
    private final HytaleLogger internalLogger = HytaleLogger.forEnclosingClass();
    
    public static final boolean IS_DEV = "true".equals(System.getenv("HYUI_DEV"));

    public HyUIPluginLogger() {
        
    }

    public void logWarn(String message) {
        internalLogger.atWarning().log(message);
    }
    
    public void logFinest(String message) {
        internalLogger.atFinest().log(message);
    }
}
