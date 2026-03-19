package br.com.payroll.calculation.domain.service;

import br.com.payroll.calculation.domain.model.PayrollInput;
import br.com.payroll.calculation.domain.model.PayrollTaxBreakdown;

public interface PayrollTaxCalculator {

    PayrollTaxBreakdown calculate(PayrollInput input);
}
