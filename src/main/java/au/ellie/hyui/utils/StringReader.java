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

import java.util.function.Predicate;

/**
 * A comprehensive string reader utility for parsing text character-by-character.
 * Provides both low-level character access and high-level reading operations.
 * <p>
 * Example usage:
 * <pre>
 * var reader = new StringReader("hello world");
 * reader.skip(6);                    // skip "hello "
 * var word = reader.readWord();      // reads "world"
 * </pre>
 */
public class StringReader {
    private final String input;
    private final int length;
    private int position;

    /**
     * Create a new StringReader for the given input
     *
     * @param input The string to read from
     */
    public StringReader(String input) {
        this.input = input;
        this.length = input.length();
        this.position = 0;
    }

    // ========== Navigation ==========

    /**
     * Check if we've reached the end of the input
     *
     * @return false if at end, true otherwise
     */
    public boolean hasNext() {
        return position < length;
    }

    /**
     * Get the current position in the string
     *
     * @return The current position index
     */
    public int getPosition() {
        return position;
    }

    /**
     * Set the position to a specific index
     * Clamps the position to be within the bounds of the string (0 to length)
     *
     * @param newPosition The new position
     */
    public void setPosition(int newPosition) {
        this.position = Math.max(0, Math.min(newPosition, length));
    }

    /**
     * Get the remaining length from current position
     *
     * @return Number of characters remaining
     */
    public int remaining() {
        return length - position;
    }

    // ========== Character Access ==========

    /**
     * Get the current character without advancing position
     *
     * @return The current character, or '\0' if at end
     */
    public char current() {
        return position < length ? input.charAt(position) : '\0';
    }

    /**
     * Alias for current() - peek at current character
     *
     * @return The current character, or '\0' if at end
     */
    public char peek() {
        return current();
    }

    /**
     * Peek at the character at a specific offset from current position
     *
     * @param offset The offset from current position (0 = current, 1 = next, etc.)
     * @return The character at that offset, or '\0' if out of bounds
     */
    public char peek(int offset) {
        int index = position + offset;
        return index >= 0 && index < length ? input.charAt(index) : '\0';
    }

    /**
     * Get the next character (1 position ahead) without advancing
     *
     * @return The next character, or '\0' if at or past end
     */
    public char next() {
        return peek(1);
    }

    /**
     * Get the previous character (1 position back) without changing position
     *
     * @return The previous character, or '\0' if at start
     */
    public char previous() {
        return peek(-1);
    }

    /**
     * Advance position by one and return the character we just passed
     *
     * @return The character at the previous position, or '\0' if at end
     */
    public char advance() {
        if (!hasNext())
            return '\0';

        return input.charAt(position++);
    }

    // ========== String Matching ==========

    /**
     * Check if the input starts with the given string at current position
     *
     * @param str The string to check for
     * @return true if the string matches at current position
     */
    public boolean startsWith(String str) {
        return input.startsWith(str, position);
    }

    /**
     * Check if the input starts with any of the given strings at current position
     *
     * @param strings The strings to check for
     * @return true if any string matches at current position
     */
    public boolean startsWith(String... strings) {
        for (var str : strings)
            if (input.startsWith(str, position))
                return true;
        return false;
    }

    /**
     * Find which string (if any) matches at current position
     *
     * @param strings The strings to check for
     * @return The first matching string, or null if none match
     */
    public String filter(String... strings) {
        for (var str : strings)
            if (input.startsWith(str, position))
                return str;
        return null;
    }

    /**
     * Check if current character matches any of the given characters
     *
     * @param chars The characters to check for
     * @return true if current character matches any of them
     */
    public boolean match(char... chars) {
        char current = current();
        for (char c : chars)
            if (current == c)
                return true;

        return false;
    }

    // ========== Skip/Consume Operations ==========

    /**
     * Move forward by one character
     *
     * @return The current character after skipping, or '\0' if at end
     */
    public char skip() {
        return skip(1);
    }

    /**
     * Move forward by the specified number of characters
     *
     * @param count Number of characters to skip
     * @return The current character after skipping, or '\0' if at end
     */
    public char skip(int count) {
        position = Math.min(position + count, length);
        return current();
    }

    /**
     * Move forward by the length of the given string
     *
     * @param str The string whose length determines how many chars to skip
     * @return The current character after skipping, or '\0' if at end
     */
    public char skip(String str) {
        return skip(str.length());
    }

    /**
     * Consume the given string if it matches at current position
     * Returns true and advances position if matched, otherwise returns false
     *
     * @param str The string to consume
     * @return true if consumed, false if not matched
     */
    public boolean consume(String str) {
        if (startsWith(str)) {
            skip(str);
            return true;
        }

        return false;
    }

    /**
     * Consume any of the given strings if one matches at current position
     *
     * @param strings The strings to try to consume
     * @return The consumed string, or null if none matched
     */
    public String consumeAny(String... strings) {
        var matched = filter(strings);
        if (matched != null)
            skip(matched);

        return matched;
    }

    /**
     * Consume the given character if it matches the current character
     *
     * @param expected The character to consume
     * @return true if consumed, false if not matched
     */
    public boolean consume(char expected) {
        if (current() == expected) {
            advance();
            return true;
        }

        return false;
    }

    // ========== Whitespace Operations ==========

    /**
     * Skip all whitespace characters from current position
     *
     * @return The number of whitespace characters skipped
     */
    public int skipWhitespace() {
        int start = position;
        while (hasNext() && Character.isWhitespace(current()))
            advance();

        return position - start;
    }

    /**
     * Skip all non-whitespace characters from current position
     *
     * @return The number of characters skipped
     */
    public int skipNonWhitespace() {
        int start = position;
        while (hasNext() && !Character.isWhitespace(current()))
            advance();

        return position - start;
    }

    /**
     * Skip characters while the predicate is true
     *
     * @param predicate Function that returns true to continue skipping
     * @return The number of characters skipped
     */
    public int skipWhile(Predicate<Character> predicate) {
        int start = position;
        while (hasNext() && predicate.test(current()))
            advance();

        return position - start;
    }

    /**
     * Skip characters until the predicate becomes true
     *
     * @param predicate Function that returns true to stop skipping
     * @return The number of characters skipped
     */
    public int skipUntil(Predicate<Character> predicate) {
        return skipWhile(c -> !predicate.test(c));
    }

    // ========== Reading Operations ==========

    /**
     * Read characters while the predicate is true
     *
     * @param predicate Function that returns true to continue reading
     * @return The accumulated string
     */
    public String readWhile(Predicate<Character> predicate) {
        int start = position;
        while (hasNext() && predicate.test(current()))
            advance();

        return input.substring(start, position);
    }

    /**
     * Read characters until the predicate becomes true
     *
     * @param predicate Function that returns true to stop reading
     * @return The accumulated string
     */
    public String readUntil(Predicate<Character> predicate) {
        return readWhile(c -> !predicate.test(c));
    }

    /**
     * Read a word (sequence of non-whitespace characters)
     *
     * @return The word, or empty string if at whitespace or end
     */
    public String readWord() {
        return readWhile(c -> !Character.isWhitespace(c));
    }

    /**
     * Read while characters are letters
     *
     * @return The accumulated letters
     */
    public String readLetters() {
        return readWhile(Character::isLetter);
    }

    /**
     * Read while characters are digits
     *
     * @return The accumulated digits
     */
    public String readDigits() {
        return readWhile(Character::isDigit);
    }

    /**
     * Read while characters are alphanumeric
     *
     * @return The accumulated alphanumeric characters
     */
    public String readAlphanumeric() {
        return readWhile(Character::isLetterOrDigit);
    }

    /**
     * Read a string value (quoted or unquoted)
     * Handles both single and double quotes
     *
     * @return The string value
     */
    public String readValue() {
        char current = current();

        // Quoted value
        if (current == '"' || current == '\'')
            return readQuotedValue(current);

        // Unquoted value - read until whitespace
        return readWord();
    }

    /**
     * Read a quoted string value
     *
     * @param quote The quote character (" or ')
     * @return The string content without quotes
     */
    public String readQuotedValue(char quote) {
        if (current() != quote)
            return "";

        advance();

        int start = position;

        // Read until closing quote
        while (hasNext() && current() != quote)
            advance();

        String value = input.substring(start, position);

        // Consume closing quote if present
        if (hasNext())
            advance();

        return value;
    }

    /**
     * Read a specific number of characters
     *
     * @param count Number of characters to read
     * @return The substring of the specified length (or remaining if less available)
     */
    public String read(int count) {
        int start = position;
        int end = Math.min(position + count, length);
        position = end;

        return input.substring(start, end);
    }

    /**
     * Read from current position to the end
     *
     * @return The remaining string
     */
    public String readRemaining() {
        int start = position;
        position = length;

        return input.substring(start);
    }

    /**
     * Read until any of the given strings is found
     *
     * @param delimiters The strings to stop at
     * @return The string up to (but not including) the delimiter
     */
    public String readUntilAny(String... delimiters) {
        int start = position;

        while (hasNext()) {
            if (startsWith(delimiters))
                break;

            advance();
        }

        return input.substring(start, position);
    }

    // ========== Utility Methods ==========

    /**
     * Get a substring from start to current position
     *
     * @param start The start index
     * @return The substring
     */
    public String substring(int start) {
        return input.substring(start, position);
    }

    /**
     * Get a substring between two positions
     *
     * @param start The start index
     * @param end   The end index
     * @return The substring
     */
    public String substring(int start, int end) {
        return input.substring(start, end);
    }

    /**
     * Reset position to the beginning
     */
    public void reset() {
        position = 0;
    }

    /**
     * Get the entire input string
     *
     * @return The original input string
     */
    public String getInput() {
        return input;
    }

    /**
     * Get the length of the input string
     *
     * @return The total length
     */
    public int getLength() {
        return length;
    }

    /**
     * Create a string representation showing current position
     *
     * @return A debug string showing position in the input
     */
    @Override
    public String toString() {
        if (!hasNext())
            return String.format("StringReader[pos=%d, at end]: \"%s\"", position, input);

        // Show context around current position
        int contextStart = Math.max(0, position - 10);
        int contextEnd = Math.min(length, position + 10);

        String before = input.substring(contextStart, position);
        String after = input.substring(position, contextEnd);

        return String.format("StringReader[pos=%d/%d]: \"%s▶%s\"",
                position, length, before, after);
    }
}
