package org.junit.jupiter.api;

import java.math.BigDecimal;
import java.util.Objects;

public final class Assertions {

    private Assertions() {
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            fail("Expected <" + expected + "> but was <" + actual + ">.");
        }
    }

    public static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        BigDecimal expectedValue = new BigDecimal(expected);
        if (expectedValue.compareTo(actual) != 0) {
            fail("Expected <" + expectedValue + "> but was <" + actual + ">.");
        }
    }

    public static void assertTrue(boolean condition) {
        if (!condition) {
            fail("Expected condition to be true.");
        }
    }

    public static void assertFalse(boolean condition) {
        if (condition) {
            fail("Expected condition to be false.");
        }
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            if (expectedType.isInstance(throwable)) {
                return expectedType.cast(throwable);
            }
            fail("Expected exception <" + expectedType.getName() + "> but was <" + throwable.getClass().getName() + ">.");
        }
        fail("Expected exception <" + expectedType.getName() + "> but nothing was thrown.");
        return null;
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    @FunctionalInterface
    public interface Executable {
        void execute() throws Throwable;
    }
}
