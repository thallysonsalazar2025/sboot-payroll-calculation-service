# CI security and coverage gates

Pull requests run without repository secrets and with read-only repository
permissions. GitHub Actions are pinned to immutable commit SHAs and Maven plugins
are pinned to explicit versions.

`mvn clean verify` aggregates Surefire and Failsafe execution in the same JaCoCo
data file. On 2026-08-01, the baseline measured from `clean verify` was 174/195
lines (89.23%) and 54/90 branches (60.00%). The build therefore fails below 89%
line coverage or 60% branch coverage. `check-diff-coverage.py` separately requires
100% line and branch coverage for executable Java lines added or changed in the
pull request; the bundle thresholds prevent baseline regression.

OWASP Dependency-Check 12.1.3 produces mandatory HTML and JSON reports and keeps
the CVSS 8 failure threshold. A missing report, a scanner/configuration failure,
or a vulnerability at the threshold fails CI. NVD availability remains an
external dependency; no NVD credential is exposed to pull requests.

OSS Index is not claimed as a separate gate. Dependency-Check/NVD is the current
approved dependency-vulnerability source; adding a second data provider requires
credential provisioning and a supply-chain decision outside pull-request code.

## Controlled negative checks

- Coverage: run `python scripts/check-diff-coverage.py --self-test`; its controlled
  uncovered-line fixture must be rejected without changing production code.
- Reports: run the workflow-equivalent `test -s` commands after removing a copy
  of one report in a temporary directory; the command must fail. Never weaken or
  commit a gate to demonstrate this behavior.
