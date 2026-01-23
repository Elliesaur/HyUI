package au.ellie.hyui;

import com.hypixel.hytale.logger.HytaleLogger;

public class HyUIPluginLogger {
    
    private final HytaleLogger internalLogger = HytaleLogger.forEnclosingClass();
    
    public static final boolean LOGGING_ENABLED = true;
    
    public HyUIPluginLogger() {
        
    }
    
    public void logInfo(String message) {
        if (LOGGING_ENABLED) {
            internalLogger.atInfo().log(message);
        }
    }
}
