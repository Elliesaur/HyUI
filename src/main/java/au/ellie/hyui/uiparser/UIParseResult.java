package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.ParserError;
import app.ultradev.hytaleuiparser.ValidatorError;
import app.ultradev.hytaleuiparser.warning.ValidatorWarning;
import au.ellie.hyui.builders.UIElementBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UIParseResult {
    private final List<UIElementBuilder<?>> elements = new ArrayList<>();
    private final List<ParserError> parserErrors = new ArrayList<>();
    private final List<ValidatorError> validationErrors = new ArrayList<>();
    private final List<ValidatorWarning> validationWarnings = new ArrayList<>();
    private final List<String> conversionWarnings = new ArrayList<>();
    private String documentPath;

    void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    void addElements(List<UIElementBuilder<?>> elements) {
        if (elements != null) {
            this.elements.addAll(elements);
        }
    }

    void addParserErrors(List<ParserError> errors) {
        if (errors != null) {
            this.parserErrors.addAll(errors);
        }
    }

    void addValidationErrors(List<ValidatorError> errors) {
        if (errors != null) {
            this.validationErrors.addAll(errors);
        }
    }

    void addValidationWarnings(List<ValidatorWarning> warnings) {
        if (warnings != null) {
            this.validationWarnings.addAll(warnings);
        }
    }

    void addConversionWarning(String warning) {
        if (warning != null && !warning.isBlank()) {
            this.conversionWarnings.add(warning);
        }
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public List<UIElementBuilder<?>> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public List<ParserError> getParserErrors() {
        return Collections.unmodifiableList(parserErrors);
    }

    public List<ValidatorError> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    public List<ValidatorWarning> getValidationWarnings() {
        return Collections.unmodifiableList(validationWarnings);
    }

    public List<String> getConversionWarnings() {
        return Collections.unmodifiableList(conversionWarnings);
    }

    public boolean hasErrors() {
        return !parserErrors.isEmpty() || !validationErrors.isEmpty();
    }
}
