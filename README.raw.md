# m5-pub

## Description

A small Java 17 codebase containing a simplified Indian income-tax and GST
calculator (`TaxCalculator`) and a REST API for managing orders (`OrderApi`)
backed by a thread-safe in-memory order store (`OrderService`).

Features:

- Income tax computation using old-regime slabs (0% / 5% / 20% / 30%).
- GST computation for the standard rates 0, 5, 12, 18 and 28 percent,
  rounded to paise.
- 80C-style exemption with a cap of ₹1,50,000.
- Income-tax return eligibility check with senior (60+) and
  super-senior (80+) thresholds.
- Order REST endpoints: list, fetch by id, create, cancel.
- Input validation on order creation with `400 Bad Request` responses.
- JSON request/response serialization of `OrderDto`.

## Build

Requires a JDK (17+), `make` and `curl`. Dependencies (JUnit 5, JaCoCo, PIT)
are downloaded into `libs/` on first use.

    make deps && make test
    make coverage    # JaCoCo HTML report
    make mutation    # PIT HTML report at build/reports/pitest/index.html
    make clean

## Quick example

```java
TaxCalculator calc = new TaxCalculator();
BigDecimal tax = calc.computeIncomeTax(new BigDecimal("700000"));   // 52500
BigDecimal gst = calc.computeVAT(new BigDecimal("1000"), 18);       // 180.00
```

Start the service and create an order:

    curl -X POST http://localhost:8080/orders \
         -H 'Content-Type: application/json' \
         -d '{"customerId":"c1","amount":99.50}'

## Contributing

TODO

## License

This project is licensed under the MIT License. See `LICENSE` for details.
