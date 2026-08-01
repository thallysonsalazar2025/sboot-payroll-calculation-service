# Payroll Calculation Service

Componente Java autocontido para cálculo de tributos tradicionais de holerite no Brasil.

## O que o componente calcula

- INSS do empregado com faixas progressivas por competência.
- IRRF mensal com escolha automática entre deduções legais e desconto simplificado.
- Redução adicional do IRRF aplicável a partir de 2026 para rendimentos tributáveis mensais até R$ 7.350,00.
- FGTS mensal como encargo patronal exibível no holerite.

## Estrutura

- `application`: orquestra o cálculo.
- `domain`: contratos e modelos imutáveis.
- `infrastructure`: catálogo versionado de tabelas tributárias.
- `api`: componente de entrada/saída para integração com outros módulos.
- `test`: suíte unitária e integrada em estilo JUnit 5.

## Competências suportadas

- Janeiro a abril de 2025.
- Maio a dezembro de 2025.
- Janeiro de 2026 em diante.

## Execução dos testes

```bash
mvn test
# ou
./scripts/test.sh
```

## Gates de qualidade

`mvn clean verify` executa testes unitários (Surefire), testes de integração
(Failsafe), mescla as duas execuções do JaCoCo e bloqueia regressões nos limites de
cobertura. O mesmo ciclo executa SpotBugs 4.9.3.0 com severidade média ou superior.
Na CI, Gitleaks v2 pinado por SHA inspeciona segredos e OWASP Dependency-Check
bloqueia vulnerabilidades com CVSS 8 ou maior. O job de Pull Request consulta a
NVD anonimamente e nunca disponibiliza uma chave da NVD ao código da branch.

A exclusão SpotBugs em `config/spotbugs-exclude.xml` limita-se à dívida preexistente
de mutabilidade das listas de faixas de `TaxTables` (`EI_EXPOSE_REP*`). Ela evita
misturar alteração de domínio com o hardening da CI; qualquer outro achado médio ou
superior continua bloqueando o build.
