package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.html.HtmlParser;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EventListenerBuilder {
    private final List<Entry<CustomUIEventBindingType, String>> eventListeners;
    private final UIElementBuilder<?> builder;
    private final HtmlParser parser;

    public EventListenerBuilder(UIElementBuilder<?> builder, HtmlParser parser) {
        this.eventListeners = new ArrayList<>();
        this.builder = builder;
        this.parser = parser;
    }

    public void add(CustomUIEventBindingType type, String callback) {
        eventListeners.add(Map.entry(builder.getEventTypeMapped(type), callback));
    }

    public void build() {
        for (var entry : eventListeners) {
            builder.addEventListenerWithContext(entry.getKey(), (data, context) -> {
                var eventCallback = parser.getEventByName(entry.getValue());
                if (eventCallback != null)
                    eventCallback.accept(data, context, entry.getKey());
                else
                    HyUIPlugin.getLog().logWarn("No event found with name: " + entry.getValue() + " for event type: " + entry.getKey());
            });
        }
    }
}
