package br.com.payroll.calculation.infrastructure.rules;

import br.com.payroll.calculation.domain.model.IncomeTaxReductionRule;
import br.com.payroll.calculation.domain.model.ProgressiveTaxBracket;
import br.com.payroll.calculation.domain.model.TaxTables;
import br.com.payroll.calculation.domain.service.TaxRuleCatalog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class InMemoryBrazilianTaxRuleCatalog implements TaxRuleCatalog {

    private final List<TaxTables> versions = List.of(
            januaryToApril2025(),
            mayToDecember2025(),
            january2026Onwards()
    );

    @Override
    public TaxTables resolve(LocalDate competenceDate) {
        return versions.stream()
                .filter(version -> !version.effectiveFrom().isAfter(competenceDate))
                .max(Comparator.comparing(TaxTables::effectiveFrom))
                .orElseThrow(() -> new IllegalArgumentException("Não há tabela tributária configurada para a competência " + competenceDate + "."));
    }

    private TaxTables januaryToApril2025() {
        return new TaxTables(
                LocalDate.of(2025, 1, 1),
                inssBrackets("1518.00", "2793.88", "4190.83", "8157.41"),
                new BigDecimal("8157.41"),
                irrfBrackets(
                        bracket("2259.20", "0.0", "0.00"),
                        bracket("2826.65", "7.5", "169.44"),
                        bracket("3751.05", "15.0", "381.44"),
                        bracket("4664.68", "22.5", "662.77"),
                        bracket(null, "27.5", "896.00")
                ),
                new BigDecimal("189.59"),
                new BigDecimal("564.80"),
                new BigDecimal("8.0"),
                null
        );
    }

    private TaxTables mayToDecember2025() {
        return new TaxTables(
                LocalDate.of(2025, 5, 1),
                inssBrackets("1518.00", "2793.88", "4190.83", "8157.41"),
                new BigDecimal("8157.41"),
                irrfBrackets(
                        bracket("2428.80", "0.0", "0.00"),
                        bracket("2826.65", "7.5", "182.16"),
                        bracket("3751.05", "15.0", "394.16"),
                        bracket("4664.68", "22.5", "675.49"),
                        bracket(null, "27.5", "908.73")
                ),
                new BigDecimal("189.59"),
                new BigDecimal("607.20"),
                new BigDecimal("8.0"),
                null
        );
    }

    private TaxTables january2026Onwards() {
        return new TaxTables(
                LocalDate.of(2026, 1, 1),
                inssBrackets("1621.00", "2902.84", "4354.27", "8475.55"),
                new BigDecimal("8475.55"),
                irrfBrackets(
                        bracket("2428.80", "0.0", "0.00"),
                        bracket("2826.65", "7.5", "182.16"),
                        bracket("3751.05", "15.0", "394.16"),
                        bracket("4664.68", "22.5", "675.49"),
                        bracket(null, "27.5", "908.73")
                ),
                new BigDecimal("189.59"),
                new BigDecimal("607.20"),
                new BigDecimal("8.0"),
                new IncomeTaxReductionRule(
                        new BigDecimal("5000.00"),
                        new BigDecimal("7350.00"),
                        new BigDecimal("312.89"),
                        new BigDecimal("978.62"),
                        new BigDecimal("0.133145")
                )
        );
    }

    private List<ProgressiveTaxBracket> inssBrackets(String firstLimit, String secondLimit, String thirdLimit, String fourthLimit) {
        return List.of(
                bracket(firstLimit, "7.5", "0.00"),
                bracket(secondLimit, "9.0", "0.00"),
                bracket(thirdLimit, "12.0", "0.00"),
                bracket(fourthLimit, "14.0", "0.00")
        );
    }

    private List<ProgressiveTaxBracket> irrfBrackets(ProgressiveTaxBracket... brackets) {
        return List.of(brackets);
    }

    private ProgressiveTaxBracket bracket(String upperLimit, String rate, String deduction) {
        return new ProgressiveTaxBracket(
                upperLimit != null ? new BigDecimal(upperLimit) : null,
                new BigDecimal(rate),
                new BigDecimal(deduction)
        );
    }
}
