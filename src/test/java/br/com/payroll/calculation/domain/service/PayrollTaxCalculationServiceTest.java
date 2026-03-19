package br.com.payroll.calculation.domain.service;

import br.com.payroll.calculation.application.PayrollTaxCalculationService;
import br.com.payroll.calculation.domain.model.CompanyInfo;
import br.com.payroll.calculation.domain.model.EmployeeInfo;
import br.com.payroll.calculation.domain.model.PayrollInput;
import br.com.payroll.calculation.domain.model.PayrollTaxBreakdown;
import br.com.payroll.calculation.infrastructure.rules.InMemoryBrazilianTaxRuleCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertBigDecimalEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PayrollTaxCalculationServiceTest {

    private PayrollTaxCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PayrollTaxCalculationService(new InMemoryBrazilianTaxRuleCatalog());
    }

    @Test
    void shouldCalculateTraditionalPayrollTaxesFor2025AndPreferSimplifiedDeductionWhenBetter() {
        PayrollTaxBreakdown result = service.calculate(baseInput()
                .grossSalary(new BigDecimal("6000.00"))
                .competenceDate(LocalDate.of(2025, 6, 1))
                .build());

        assertBigDecimalEquals("6000.00", result.taxableIncome());
        assertBigDecimalEquals("649.59", result.inssAmount());
        assertBigDecimalEquals("4743.21", result.irrfBase());
        assertBigDecimalEquals("395.65", result.irrfAmount());
        assertBigDecimalEquals("480.00", result.fgtsAmount());
        assertBigDecimalEquals("1045.24", result.totalEmployeeDeductions());
        assertBigDecimalEquals("4954.76", result.netSalary());
        assertTrue(result.simplifiedDeductionApplied());
    }

    @Test
    void shouldApply2026IncomeTaxReductionForIncomeUpToFiveThousand() {
        PayrollTaxBreakdown result = service.calculate(baseInput()
                .grossSalary(new BigDecimal("5000.00"))
                .competenceDate(LocalDate.of(2026, 2, 1))
                .build());

        assertBigDecimalEquals("501.52", result.inssAmount());
        assertBigDecimalEquals("0.00", result.irrfAmount());
        assertBigDecimalEquals("4498.48", result.netSalary());
        assertTrue(result.simplifiedDeductionApplied());
    }

    @Test
    void shouldRejectNegativeDependents() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.calculate(baseInput()
                .employee(new EmployeeInfo("Maria", "11111111111", -1))
                .build()));

        assertTrue(exception.getMessage().contains("dependentes"));
    }

    private PayrollInputBuilder baseInput() {
        return new PayrollInputBuilder()
                .company(new CompanyInfo("Empresa XPTO", "12345678000199", null))
                .employee(new EmployeeInfo("Maria Silva", "12345678900", 0))
                .grossSalary(new BigDecimal("0.00"))
                .taxableBenefits(new BigDecimal("0.00"))
                .alimonyDeduction(new BigDecimal("0.00"))
                .privatePensionDeduction(new BigDecimal("0.00"))
                .simplifiedDeductionPreferred(null)
                .competenceDate(LocalDate.of(2025, 6, 1));
    }

    private static final class PayrollInputBuilder {
        private CompanyInfo company;
        private EmployeeInfo employee;
        private BigDecimal grossSalary;
        private BigDecimal taxableBenefits;
        private BigDecimal alimonyDeduction;
        private BigDecimal privatePensionDeduction;
        private Boolean simplifiedDeductionPreferred;
        private LocalDate competenceDate;

        PayrollInputBuilder company(CompanyInfo company) {
            this.company = company;
            return this;
        }

        PayrollInputBuilder employee(EmployeeInfo employee) {
            this.employee = employee;
            return this;
        }

        PayrollInputBuilder grossSalary(BigDecimal grossSalary) {
            this.grossSalary = grossSalary;
            return this;
        }

        PayrollInputBuilder taxableBenefits(BigDecimal taxableBenefits) {
            this.taxableBenefits = taxableBenefits;
            return this;
        }

        PayrollInputBuilder alimonyDeduction(BigDecimal alimonyDeduction) {
            this.alimonyDeduction = alimonyDeduction;
            return this;
        }

        PayrollInputBuilder privatePensionDeduction(BigDecimal privatePensionDeduction) {
            this.privatePensionDeduction = privatePensionDeduction;
            return this;
        }

        PayrollInputBuilder simplifiedDeductionPreferred(Boolean simplifiedDeductionPreferred) {
            this.simplifiedDeductionPreferred = simplifiedDeductionPreferred;
            return this;
        }

        PayrollInputBuilder competenceDate(LocalDate competenceDate) {
            this.competenceDate = competenceDate;
            return this;
        }

        PayrollInput build() {
            return new PayrollInput(
                    company,
                    employee,
                    grossSalary,
                    taxableBenefits,
                    alimonyDeduction,
                    privatePensionDeduction,
                    simplifiedDeductionPreferred,
                    competenceDate
            );
        }
    }
}
