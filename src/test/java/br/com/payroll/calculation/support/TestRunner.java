package br.com.payroll.calculation.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class TestRunner {

    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        List<Class<?>> testClasses = List.of(
                br.com.payroll.calculation.domain.service.PayrollTaxCalculationServiceTest.class,
                br.com.payroll.calculation.api.PayrollTaxCalculationComponentIT.class
        );

        int executed = 0;
        int failed = 0;

        for (Class<?> testClass : testClasses) {
            Method beforeEach = findBeforeEach(testClass);
            for (Method method : testClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Test.class)) {
                    continue;
                }
                executed++;
                Object instance = testClass.getDeclaredConstructor().newInstance();
                try {
                    if (beforeEach != null) {
                        beforeEach.setAccessible(true);
                        beforeEach.invoke(instance);
                    }
                    method.setAccessible(true);
                    method.invoke(instance);
                    System.out.println("[PASS] " + testClass.getSimpleName() + "." + method.getName());
                } catch (InvocationTargetException exception) {
                    failed++;
                    Throwable cause = exception.getCause();
                    System.err.println("[FAIL] " + testClass.getSimpleName() + "." + method.getName() + " -> " + cause);
                }
            }
        }

        System.out.println("Executed: " + executed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static Method findBeforeEach(Class<?> type) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeEach.class)) {
                return method;
            }
        }
        return null;
    }
}
