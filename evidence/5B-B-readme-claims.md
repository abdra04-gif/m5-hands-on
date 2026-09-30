# 5B Part B — grep check of every capability claimed in README.raw.md

File tree pasted into the prompt, together with the top-level files (Makefile, the starter README.md, .gitignore) and every file under src/:

```
.gitignore
Makefile
README.md
src/Order.java
src/OrderApi.java
src/OrderDto.java
src/OrderService.java
src/SpringStubs.java
src/TaxCalculator.java
test/TaxCalculatorTest.java
```

## Claim: thread-safe store

```
$ grep -rniE 'synchronized|Atomic|volatile|putIfAbsent|compute\(|Lock' src Makefile
(no matches)
```

## Claim: JSON serialization

```
$ grep -rniE 'json|jackson|serializ|ObjectMapper' src Makefile
(no matches)
```

## Claim: runnable server / curl :8080

```
$ grep -rniE 'void main|SpringApplication|8080|HttpServer' src Makefile
(no matches)
```

## Claim: old-regime slabs 5/20/30%

```
$ grep -rniE '0\.05|0\.20|0\.30' src Makefile
src/TaxCalculator.java:24:            return income.subtract(slab1).multiply(new BigDecimal("0.05"));
src/TaxCalculator.java:27:            BigDecimal upToSlab2 = slab2.subtract(slab1).multiply(new BigDecimal("0.05"));
src/TaxCalculator.java:28:            return upToSlab2.add(income.subtract(slab2).multiply(new BigDecimal("0.20")));
src/TaxCalculator.java:30:        BigDecimal upToSlab2 = slab2.subtract(slab1).multiply(new BigDecimal("0.05"));
src/TaxCalculator.java:31:        BigDecimal upToSlab3 = slab3.subtract(slab2).multiply(new BigDecimal("0.20"));
src/TaxCalculator.java:33:                .add(income.subtract(slab3).multiply(new BigDecimal("0.30")));
```

## Claim: GST rates 0/5/12/18/28 rounded to paise

```
$ grep -rniE 'gstRatePercent != |RoundingMode.HALF_UP' src Makefile
src/TaxCalculator.java:41:        if (gstRatePercent != 0 && gstRatePercent != 5 && gstRatePercent != 12
src/TaxCalculator.java:42:                && gstRatePercent != 18 && gstRatePercent != 28) {
src/TaxCalculator.java:46:                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
src/TaxCalculator.java:65:        return amount.setScale(2, RoundingMode.HALF_UP);
```

## Claim: 80C cap 1,50,000

```
$ grep -rniE '150000' src Makefile
src/TaxCalculator.java:54:        BigDecimal cap = new BigDecimal("150000");
```

## Claim: return eligibility 60+/80+

```
$ grep -rniE 'ageYears >= ' src Makefile
src/TaxCalculator.java:74:        if (ageYears >= 80) {
src/TaxCalculator.java:76:        } else if (ageYears >= 60) {
```

## Claim: 400 on invalid create

```
$ grep -rniE 'badRequest' src Makefile
src/OrderApi.java:54:            return ResponseEntity.badRequest().build();
src/SpringStubs.java:46:    static Builder badRequest() { return new Builder(400); }
```

## Claim: 4 endpoints

```
$ grep -rniE '@(Get|Post|Delete)Mapping' src Makefile
src/OrderApi.java:17:    @GetMapping
src/OrderApi.java:22:    @GetMapping("/{id}")
src/OrderApi.java:49:    @PostMapping
src/OrderApi.java:60:    @DeleteMapping("/{id}")
```

## Claim: make clean target

```
$ grep -rniE '^clean:' src Makefile
Makefile:89:clean:
```

## Claim: JDK 17

```
$ grep -rniE 'release 17' src Makefile
Makefile:44:	javac --release 17 -d build -cp $(JUNIT_JAR) $(SRCS) $(TESTS)
```

## Claim: LICENSE file

```
$ ls LICENSE*
(eval):1: no matches found: LICENSE*
```

## Verdict

| Claim in README.raw.md | grep result | Action in README.md |
|---|---|---|
| "thread-safe in-memory order store" | no synchronisation found. The map is a `ConcurrentHashMap`, but `OrderService.cancel` is a get-then-`setStatus` sequence on a mutable `Order` with no locking | **Invented → deleted.** Now "in-memory order store (a `ConcurrentHashMap`)" |
| "JSON request/response serialization of `OrderDto`" | no matches | **Invented → deleted** |
| "Start the service … `curl localhost:8080/orders`" | no `main`, no server | **Invented → deleted.** Quick example now calls `OrderApi` directly |
| "See `LICENSE` for details" | no such file | **Invented → replaced** with "MIT License — TODO: add a `LICENSE` file" |
| slabs, GST rates, 80C cap, eligibility limits, 400 on invalid create, 4 endpoints, `make clean`, JDK 17 | all found | kept |
