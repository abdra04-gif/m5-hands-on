# m5-pub

## Description

Teaching starter (CS5013, Module 5): a simplified Indian income-tax/GST
calculator with a JUnit 5 suite, plus a Spring-style order controller that
compiles against fake annotations and is not served over HTTP.

What is in `src/`:

- `TaxCalculator`: old-regime income-tax slabs (0% / 5% / 20% / 30%), GST at
  0, 5, 12, 18 or 28 percent rounded to paise, an 80C-style exemption capped
  at ₹1,50,000, rounding to paise, and a check for whether a return must be
  filed (limits of ₹2,50,000 below age 60, ₹3,00,000 from 60, ₹5,00,000 from 80).
- `OrderApi`: controller with four endpoints (`GET /orders`,
  `GET /orders/{id}`, `POST /orders`, `DELETE /orders/{id}`), documented in
  [`openapi.yaml`](openapi.yaml).
- `OrderService`: in-memory order store (a `ConcurrentHashMap`; state is lost
  when the JVM exits). `Order` / `OrderDto`: the entity and its transfer record.
- `SpringStubs`: no-op stand-ins for the Spring Web annotations and
  `ResponseEntity`, so no Spring jars are needed.

## Build

Requires JDK 17+, `make` and `curl`. JUnit 5, JaCoCo and PIT are downloaded
into `libs/` on first use.

    make test        # compile src/ + test/ and run the JUnit 5 suite
    make coverage    # JaCoCo report at coverage/index.html (XML: coverage/report.xml)
    make mutation    # PIT report for TaxCalculator at build/reports/pitest/index.html
    make clean       # remove build/, libs/, coverage/, jacoco.exec

## Quick example

All classes are in the default package, so call them from code compiled
alongside `build/`:

```java
TaxCalculator calc = new TaxCalculator();
calc.computeIncomeTax(new BigDecimal("700000"));          // 52500.00
calc.computeVAT(new BigDecimal("1000"), 18);              // 180.00
calc.isEligibleForReturn(new BigDecimal("280000"), 65);   // false (senior limit 3,00,000)

OrderApi api = new OrderApi(new OrderService());
api.create(new OrderDto(null, "c1", new BigDecimal("99.50"), null))
   .getStatusCode();                                      // 201
```

## Contributing

Add or change tests in `test/`, then run `make test`, `make coverage` and
`make mutation` before submitting. Record LLM prompts and replies in
`PROMPTS.md`.

## License

MIT License — TODO: add a `LICENSE` file with the copyright holder and year.
