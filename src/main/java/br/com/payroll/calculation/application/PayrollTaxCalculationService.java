package br.com.payroll.calculation.application;

import br.com.payroll.calculation.domain.model.PayrollInput;
import br.com.payroll.calculation.domain.model.PayrollTaxBreakdown;
import br.com.payroll.calculation.domain.model.ProgressiveTaxBracket;
import br.com.payroll.calculation.domain.model.TaxTables;
import br.com.payroll.calculation.domain.service.PayrollTaxCalculator;
import br.com.payroll.calculation.domain.service.TaxRuleCatalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class PayrollTaxCalculationService implements PayrollTaxCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final TaxRuleCatalog taxRuleCatalog;

    public PayrollTaxCalculationService(TaxRuleCatalog taxRuleCatalog) {
        this.taxRuleCatalog = taxRuleCatalog;
    }

    @Override
    public PayrollTaxBreakdown calculate(PayrollInput input) {
        validate(input);

        LocalDate competenceDate = input.competenceDate() != null ? input.competenceDate() : LocalDate.now();
        TaxTables tables = taxRuleCatalog.resolve(competenceDate);
        BigDecimal taxableIncome = scale(input.totalTaxableIncome());
        BigDecimal inssBase = taxableIncome.min(tables.inssCeiling());
        BigDecimal inssAmount = progressiveContribution(inssBase, tables.inssBrackets());

        BigDecimal dependentDeduction = tables.dependentDeduction().multiply(BigDecimal.valueOf(input.employee().dependents()));
        BigDecimal legalDeductions = inssAmount
                .add(scale(input.alimonyDeduction()))
                .add(scale(input.privatePensionDeduction()))
                .add(scale(dependentDeduction));

        BigDecimal irrfBaseWithoutSimplified = nonNegative(taxableIncome.subtract(legalDeductions));
        BigDecimal simplifiedBase = nonNegative(irrfBaseWithoutSimplified.subtract(tables.simplifiedMonthlyDeduction()));

        boolean useSimplified = chooseSimplified(input, irrfBaseWithoutSimplified, simplifiedBase, tables);
        BigDecimal irrfBase = useSimplified ? simplifiedBase : irrfBaseWithoutSimplified;
        BigDecimal irrfAmount = progressiveIncomeTax(irrfBase, taxableIncome, tables);

        BigDecimal fgtsRate = input.company().fgtsRate() != null ? input.company().fgtsRate() : tables.defaultFgtsRate();
        BigDecimal fgtsBase = taxableIncome;
        BigDecimal fgtsAmount = percentageOf(fgtsBase, fgtsRate);

        BigDecimal totalEmployeeDeductions = inssAmount.add(irrfAmount);
        BigDecimal netSalary = taxableIncome.subtract(totalEmployeeDeductions);

        return new PayrollTaxBreakdown(
                competenceDate,
                taxableIncome,
                inssBase,
                inssAmount,
                irrfBase,
                irrfAmount,
                fgtsBase,
                fgtsAmount,
                scale(totalEmployeeDeductions),
                scale(netSalary),
                useSimplified
        );
    }

    private boolean chooseSimplified(PayrollInput input,
                                     BigDecimal irrfBaseWithoutSimplified,
                                     BigDecimal simplifiedBase,
                                     TaxTables tables) {
        if (Boolean.TRUE.equals(input.simplifiedDeductionPreferred())) {
            return true;
        }
        if (Boolean.FALSE.equals(input.simplifiedDeductionPreferred())) {
            return false;
        }
        BigDecimal taxableIncome = input.totalTaxableIncome();
        BigDecimal regularIrrf = progressiveIncomeTax(irrfBaseWithoutSimplified, taxableIncome, tables);
        BigDecimal simplifiedIrrf = progressiveIncomeTax(simplifiedBase, taxableIncome, tables);
        return simplifiedIrrf.compareTo(regularIrrf) <= 0;
    }

    private BigDecimal progressiveIncomeTax(BigDecimal base, BigDecimal taxableIncome, TaxTables tables) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }

        ProgressiveTaxBracket bracket = tables.irrfBrackets().stream()
                .filter(item -> item.upperLimit() == null || base.compareTo(item.upperLimit()) <= 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma faixa de IRRF encontrada."));

        BigDecimal grossTax = percentageOf(base, bracket.rate());
        BigDecimal calculatedTax = nonNegative(grossTax.subtract(bracket.fixedDeduction()));
        return nonNegative(calculatedTax.subtract(incomeTaxReduction(scale(taxableIncome), calculatedTax, tables)));
    }

    private BigDecimal incomeTaxReduction(BigDecimal taxableIncome, BigDecimal calculatedTax, TaxTables tables) {
        if (tables.incomeTaxReductionRule() == null) {
            return ZERO;
        }

        var rule = tables.incomeTaxReductionRule();
        if (taxableIncome.compareTo(rule.fullExemptionIncomeLimit()) <= 0) {
            return calculatedTax;
        }
        if (taxableIncome.compareTo(rule.partialReductionIncomeLimit()) <= 0) {
            BigDecimal reduction = rule.linearReductionConstant()
                    .subtract(taxableIncome.multiply(rule.linearReductionRate()));
            return scale(reduction.min(calculatedTax).max(BigDecimal.ZERO));
        }
        return ZERO;
    }

    private BigDecimal progressiveContribution(BigDecimal base, List<ProgressiveTaxBracket> brackets) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }

        BigDecimal total = ZERO;
        BigDecimal lowerLimit = BigDecimal.ZERO;

        for (ProgressiveTaxBracket bracket : brackets) {
            BigDecimal upperLimit = bracket.upperLimit();
            BigDecimal taxableSlice = base.min(upperLimit).subtract(lowerLimit);
            if (taxableSlice.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(percentageOf(taxableSlice, bracket.rate()));
            }
            if (base.compareTo(upperLimit) <= 0) {
                break;
            }
            lowerLimit = upperLimit;
        }

        return scale(total);
    }

    private BigDecimal percentageOf(BigDecimal value, BigDecimal rate) {
        return scale(value.multiply(rate).divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP));
    }

    private BigDecimal nonNegative(BigDecimal value) {
        return scale(value.max(BigDecimal.ZERO));
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void validate(PayrollInput input) {
        if (input == null) {
            throw new IllegalArgumentException("O input para cálculo é obrigatório.");
        }
        if (input.company() == null) {
            throw new IllegalArgumentException("Os dados da empresa são obrigatórios.");
        }
        if (input.employee() == null) {
            throw new IllegalArgumentException("Os dados do empregado são obrigatórios.");
        }
        if (input.employee().dependents() < 0) {
            throw new IllegalArgumentException("A quantidade de dependentes não pode ser negativa.");
        }
        if (input.grossSalary() == null || input.grossSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O salário bruto deve ser maior ou igual a zero.");
        }
        if (input.taxableBenefits() != null && input.taxableBenefits().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Os proventos tributáveis adicionais devem ser maiores ou iguais a zero.");
        }
        if (input.alimonyDeduction() != null && input.alimonyDeduction().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A pensão alimentícia deve ser maior ou igual a zero.");
        }
        if (input.privatePensionDeduction() != null && input.privatePensionDeduction().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A previdência privada deve ser maior ou igual a zero.");
        }
    }
}
