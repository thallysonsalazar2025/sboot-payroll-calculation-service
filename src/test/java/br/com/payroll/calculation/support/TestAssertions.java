package br.com.payroll.calculation.support;

import java.math.BigDecimal;

public final class TestAssertions {

    private TestAssertions() {
    }

    public static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        BigDecimal expectedValue = new BigDecimal(expected);
        if (expectedValue.compareTo(actual) != 0) {
            throw new AssertionError("Expected <" + expectedValue + "> but was <" + actual + ">.");
        }
    }
}
