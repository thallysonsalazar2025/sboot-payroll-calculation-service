package br.com.payroll.calculation.domain.service;

import br.com.payroll.calculation.domain.model.TaxTables;

import java.time.LocalDate;

public interface TaxRuleCatalog {

    TaxTables resolve(LocalDate competenceDate);
}
