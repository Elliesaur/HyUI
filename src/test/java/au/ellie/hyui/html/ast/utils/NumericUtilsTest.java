package au.ellie.hyui.html.ast.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Numeric Utils Tests")
class NumericUtilsTest {

    @Test
    @DisplayName("Convert to numbers")
    void testToNumber() {
        assertEquals(42L, NumericUtils.toNumber(42L));
        assertEquals(42.5, NumericUtils.toNumber(42.5));
        assertEquals(100L, NumericUtils.toNumber("100"));
        assertEquals(100.5, NumericUtils.toNumber("100.5"));

        assertNull(NumericUtils.toNumber("not a number"));
        assertNull(NumericUtils.toNumber(null));
    }

    @Test
    @DisplayName("Compare with Epsilon")
    void testCompareWithEpsilon() {
        // Exact compare
        assertEquals(0, NumericUtils.compare(5, 5));
        assertEquals(0, NumericUtils.compare(5.0, 5.0));

        // Epsilon compare
        assertEquals(0, NumericUtils.compare(0.1 + 0.2, 0.3));

        // Difference compare
        assertTrue(NumericUtils.compare(5, 10) < 0);
        assertTrue(NumericUtils.compare(10, 5) > 0);
        assertTrue(NumericUtils.compare(5.5, 10.5) < 0);
    }

    @Test
    @DisplayName("Ensure equality")
    void testEquals() {
        assertTrue(NumericUtils.equals(5, 5));
        assertTrue(NumericUtils.equals(5, 5.0));
        assertTrue(NumericUtils.equals(5.0, 5));
        assertTrue(NumericUtils.equals(0.1 + 0.2, 0.3));

        assertFalse(NumericUtils.equals(5, 6));
        assertFalse(NumericUtils.equals(5.0, 6.0));

        assertTrue(NumericUtils.equals(null, null));
        assertFalse(NumericUtils.equals(5, null));
        assertFalse(NumericUtils.equals(null, 5));
    }

    @Test
    @DisplayName("Handle mixed types")
    void testMixedTypes() {
        assertTrue(NumericUtils.equals(42, 42L));
        assertTrue(NumericUtils.equals(42, 42.0));
        assertTrue(NumericUtils.equals(42L, 42.0));

        assertEquals(0, NumericUtils.compare(42, 42L));
        assertEquals(0, NumericUtils.compare(42, 42.0));
        assertEquals(0, NumericUtils.compare(42L, 42.0));
    }
}