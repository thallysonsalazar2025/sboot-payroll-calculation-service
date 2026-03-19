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
./scripts/test.sh
```
