package br.com.payroll.calculation.api;

import br.com.payroll.calculation.domain.model.CompanyInfo;
import br.com.payroll.calculation.domain.model.EmployeeInfo;
import br.com.payroll.calculation.domain.model.PayrollInput;
import br.com.payroll.calculation.domain.model.PayrollTaxBreakdown;
import br.com.payroll.calculation.domain.service.PayrollTaxCalculator;

import java.util.Objects;

public class PayrollTaxCalculationComponent {

    private final PayrollTaxCalculator payrollTaxCalculator;

    public PayrollTaxCalculationComponent(PayrollTaxCalculator payrollTaxCalculator) {
        this.payrollTaxCalculator = payrollTaxCalculator;
    }

    public PayrollTaxCalculationResponse calculate(PayrollTaxCalculationRequest request) {
        validate(request);
        PayrollTaxBreakdown result = payrollTaxCalculator.calculate(toDomain(request));
        return new PayrollTaxCalculationResponse(
                result.competenceDate(),
                result.taxableIncome(),
                result.inssBase(),
                result.inssAmount(),
                result.irrfBase(),
                result.irrfAmount(),
                result.fgtsBase(),
                result.fgtsAmount(),
                result.totalEmployeeDeductions(),
                result.netSalary(),
                result.simplifiedDeductionApplied()
        );
    }

    private PayrollInput toDomain(PayrollTaxCalculationRequest request) {
        return new PayrollInput(
                new CompanyInfo(
                        request.company().legalName(),
                        request.company().taxIdentifier(),
                        request.company().fgtsRate()
                ),
                new EmployeeInfo(
                        request.employee().fullName(),
                        request.employee().taxIdentifier(),
                        request.employee().dependents()
                ),
                request.grossSalary(),
                request.taxableBenefits(),
                request.alimonyDeduction(),
                request.privatePensionDeduction(),
                request.simplifiedDeductionPreferred(),
                request.competenceDate()
        );
    }

    private void validate(PayrollTaxCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("A requisição é obrigatória.");
        }
        if (request.company() == null) {
            throw new IllegalArgumentException("Os dados da empresa são obrigatórios.");
        }
        if (request.employee() == null) {
            throw new IllegalArgumentException("Os dados do empregado são obrigatórios.");
        }
        if (isBlank(request.company().legalName()) || isBlank(request.company().taxIdentifier())) {
            throw new IllegalArgumentException("Razão social e identificador fiscal da empresa são obrigatórios.");
        }
        if (isBlank(request.employee().fullName()) || isBlank(request.employee().taxIdentifier())) {
            throw new IllegalArgumentException("Nome e identificador fiscal do empregado são obrigatórios.");
        }
        if (Objects.isNull(request.grossSalary()) || Objects.isNull(request.taxableBenefits())
                || Objects.isNull(request.alimonyDeduction()) || Objects.isNull(request.privatePensionDeduction())) {
            throw new IllegalArgumentException("Os valores monetários da folha são obrigatórios.");
        }
        if (request.competenceDate() == null) {
            throw new IllegalArgumentException("A competência é obrigatória.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
