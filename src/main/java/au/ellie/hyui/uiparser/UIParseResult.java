package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.ParserError;
import app.ultradev.hytaleuiparser.ValidatorError;
import app.ultradev.hytaleuiparser.warning.ValidatorWarning;
import au.ellie.hyui.builders.UIElementBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result container for UI parsing, including elements and validation output.
 */
public final class UIParseResult {
    private final List<UIElementBuilder<?>> elements = new ArrayList<>();
    private final List<ParserError> parserErrors = new ArrayList<>();
    private final List<ValidatorError> validationErrors = new ArrayList<>();
    private final List<ValidatorWarning> validationWarnings = new ArrayList<>();
    private final List<String> conversionWarnings = new ArrayList<>();
    private String documentPath;

    /**
     * Sets the resolved document path.
     */
    void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    /**
     * Adds parsed elements to the result.
     */
    void addElements(List<UIElementBuilder<?>> elements) {
        if (elements != null) {
            this.elements.addAll(elements);
        }
    }

    /**
     * Adds parser errors to the result.
     */
    void addParserErrors(List<ParserError> errors) {
        if (errors != null) {
            this.parserErrors.addAll(errors);
        }
    }

    /**
     * Adds validation errors to the result.
     */
    void addValidationErrors(List<ValidatorError> errors) {
        if (errors != null) {
            this.validationErrors.addAll(errors);
        }
    }

    /**
     * Adds validation warnings to the result.
     */
    void addValidationWarnings(List<ValidatorWarning> warnings) {
        if (warnings != null) {
            this.validationWarnings.addAll(warnings);
        }
    }

    /**
     * Adds a conversion warning message.
     */
    void addConversionWarning(String warning) {
        if (warning != null && !warning.isBlank()) {
            this.conversionWarnings.add(warning);
        }
    }

    /**
     * @return the resolved document path
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * @return immutable list of parsed elements
     */
    public List<UIElementBuilder<?>> getElements() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * @return immutable list of parser errors
     */
    public List<ParserError> getParserErrors() {
        return Collections.unmodifiableList(parserErrors);
    }

    /**
     * @return immutable list of validation errors
     */
    public List<ValidatorError> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    /**
     * @return immutable list of validation warnings
     */
    public List<ValidatorWarning> getValidationWarnings() {
        return Collections.unmodifiableList(validationWarnings);
    }

    /**
     * @return immutable list of conversion warnings
     */
    public List<String> getConversionWarnings() {
        return Collections.unmodifiableList(conversionWarnings);
    }

    /**
     * @return true if parser or validation errors were recorded
     */
    public boolean hasErrors() {
        return !parserErrors.isEmpty() || !validationErrors.isEmpty();
    }
}
