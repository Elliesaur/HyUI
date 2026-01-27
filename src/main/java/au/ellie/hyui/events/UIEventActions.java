package au.ellie.hyui.events;

import java.util.List;

public class UIEventActions {
    public static final String BUTTON_CLICKED = "ButtonClicked";
    public static final String VALUE_CHANGED = "ValueChanged";
    public static final String FOCUS_LOST = "FocusLost";
    public static final String FOCUS_GAINED = "FocusGained";

    public static final List<String> VALUE_ACTIONS = List.of(VALUE_CHANGED, FOCUS_LOST, FOCUS_GAINED);
    
    private UIEventActions() {}
}
