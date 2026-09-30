# PROMPTS.md — Module 5 hands-on (Sessions 5A and 5B)

## How this was done

- **AI used:** Claude (Opus 5.5) in Claude Code, for every "paste into chat"
  step. Each prompt below was issued as its own step, and the reply is
  recorded as returned. The same model also ran the self-check prompts, so the
  self-check counts are one model reviewing its own output (the lecture's
  "catches its own hallucinations ~70% of the time" setting).
- **Evidence** (all reproducible):
  - `evidence/5A-*-jacoco.csv` and `evidence/5A-*-pit.csv`: raw JaCoCo and
    PIT results at each 5A stage (baseline, after Part B, after Part D).
  - `evidence/5B-*`: the JavaDoc verification passes, the grep check of the
    README draft, and the Swagger UI render check with screenshots.
  - Git history: `main` is the unchanged starter. Branch `m5-handson` has one
    commit per step.
- **Starter deviations** (details in Session 5B → Setup):
  1. `OrderApi.java` is **not in `m5-pub.tgz`**, although the handout and the
     starter README both describe it. I reconstructed it; see 5B Setup.
  2. The Makefile writes the JaCoCo report to `coverage/index.html`, not to
     `build/reports/jacoco/test/html/index.html` as the handout says. The
     numbers below come from that report (per-class CSV in `evidence/`).
- Coverage numbers are for **`TaxCalculator`**, the class under test. JaCoCo's
  overall total also counts the test class and the untested `Order*` classes.

---

## Session 5A — AI-generated tests

### Part A — Baseline coverage (`make coverage`, 3 seed tests)

| Scope | Line | Branch |
|---|---|---|
| `TaxCalculator` | 20/41 = **48.8%** | 15/44 = **34.1%** |
| JaCoCo total (all classes in `build/`) | 33/83 = 39.8% | 15/48 = 31.2% |

| Method | Line | Branch |
|---|---|---|
| `computeIncomeTax` | 10/17 = 58.8% | 5/10 = 50.0% |
| `computeVAT` | 4/6 = 66.7% | 6/14 = **42.9%** |
| `applyExemption` | 5/6 = 83.3% | 4/8 = 50.0% |
| `roundToPaise` | 0/3 = 0% | 0/2 = **0%** |
| `isEligibleForReturn` | 0/8 = 0% | 0/10 = **0%** |

**Methods with < 50% branch coverage:** `computeVAT` (42.9%), `roundToPaise`
(0%), `isEligibleForReturn` (0%).

Baseline PIT (`make mutation`): **15/42 killed = 36%**, matching the starter
README's "15 of 42".

### Part B — Generate tests with constraints

**Chosen method: `isEligibleForReturn`.** It ties with `roundToPaise` for the
lowest branch coverage (0%), but has 10 branches instead of 2, so it is the
method with the most untested behaviour.

**Prompt:**

````
Generate JUnit 5 tests for the following method.
Rules:
- Use AAA style with explicit comments.
- One assertion per test.
- Test names of the form
  <method>_<condition>_<expected>.
- Cover: (a) null input, (b) empty input if applicable,
  (c) boundary values, (d) one representative happy path.
- Do NOT use mocks.
- Do NOT include implementation-copying assertions; each
  assertion must state a contract, not restate the code.

```java
public boolean isEligibleForReturn(BigDecimal grossIncome, int ageYears) {
    if (grossIncome == null || ageYears < 0) {
        return false;
    }
    BigDecimal threshold;
    if (ageYears >= 80) {
        threshold = new BigDecimal("500000");
    } else if (ageYears >= 60) {
        threshold = new BigDecimal("300000");
    } else {
        threshold = new BigDecimal("250000");
    }
    return grossIncome.compareTo(threshold) > 0;
}
```
````

**AI reply:**

> Contract: a return is required iff gross income strictly exceeds the basic
> exemption limit for the taxpayer's age band (below 60: ₹2,50,000; 60–79:
> ₹3,00,000; 80+: ₹5,00,000). Invalid input (null income, negative age) is
> reported as "not eligible". "Empty input" doesn't apply to `BigDecimal`/`int`,
> so zero income is used instead. Boundaries: each income limit, both at the
> limit and one paise above it, and each age-band edge (59/60, 79/80).

```java
@Test
public void isEligibleForReturn_nullIncome_returnsFalse() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(null, 30);
    // Assert
    assertFalse(eligible);
}

@Test
public void isEligibleForReturn_negativeAge_returnsFalse() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("1000000"), -1);
    // Assert
    assertFalse(eligible);
}

@Test
public void isEligibleForReturn_zeroIncome_returnsFalse() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(BigDecimal.ZERO, 30);
    // Assert
    assertFalse(eligible);
}

@Test
public void isEligibleForReturn_under60AtExemptionLimit_returnsFalse() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("250000"), 30);
    // Assert
    assertFalse(eligible);
}

@Test
public void isEligibleForReturn_under60OnePaiseAboveLimit_returnsTrue() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("250000.01"), 30);
    // Assert
    assertTrue(eligible);
}

@Test
public void isEligibleForReturn_age59At300000_returnsTrue() {
    // Arrange: 59 is still in the general band (limit 2,50,000)
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 59);
    // Assert
    assertTrue(eligible);
}

@Test
public void isEligibleForReturn_age60AtSeniorLimit_returnsFalse() {
    // Arrange: 60 enters the senior band (limit 3,00,000)
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 60);
    // Assert
    assertFalse(eligible);
}

@Test
public void isEligibleForReturn_age60AboveSeniorLimit_returnsTrue() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("300000.01"), 60);
    // Assert
    assertTrue(eligible);
}

@Test
public void isEligibleForReturn_age79At500000_returnsTrue() {
    // Arrange: 79 is still a senior (limit 3,00,000), not super-senior
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("500000"), 79);
    // Assert
    assertTrue(eligible);
}

@Test
public void isEligibleForReturn_age80AtSuperSeniorLimit_returnsFalse() {
    // Arrange: 80 enters the super-senior band (limit 5,00,000)
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("500000"), 80);
    // Assert
    assertFalse(eligible);
}

@Test
public void isEligibleForReturn_age80AboveSuperSeniorLimit_returnsTrue() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("500000.01"), 80);
    // Assert
    assertTrue(eligible);
}

@Test
public void isEligibleForReturn_salaried35With12Lakh_returnsTrue() {
    // Arrange: representative happy path
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("1200000"), 35);
    // Assert
    assertTrue(eligible);
}
```

Pasted into `test/TaxCalculatorTest.java`. `make test`: **15/15 passed**
(3 seed + 12 new).

The reply covers the income and age-band boundaries but not the lower edge of
the age check (age 0 vs −1). I noticed this while reading the reply and did
not add a test by hand, so that Part D could show whether PIT finds the gap
on its own. It did.

### Part C — Read every assertion

For each assertion I asked the handout's question: does it encode a contract,
or does it restate the code? The pitfall test is whether the expected value
is computed from the input the same way the method computes it. All 12
expected values are boolean literals, so none is a recomputation. That test
alone is too weak for a boolean method, though, so I also checked where each
expected value comes from: tax law (domain) or only the code (pinned
behaviour).

| # | Test | Input → expected | Source of the expected value | Verdict |
|---|---|---|---|---|
| 1 | `nullIncome_returnsFalse` | `null`, 30 → false | **Code only.** Tax law has no answer for "unknown income". | Keep, flagged (see below) |
| 2 | `negativeAge_returnsFalse` | ₹10L, −1 → false | **Code only.** A negative age is invalid input, not a tax rule. | Keep, flagged (see below) |
| 3 | `zeroIncome_returnsFalse` | 0, 30 → false | Domain: zero income never needs a return. | Keep |
| 4 | `under60AtExemptionLimit_returnsFalse` | ₹2.5L, 30 → false | Domain: required only if income *exceeds* the limit. | Keep |
| 5 | `under60OnePaiseAboveLimit_returnsTrue` | ₹2,50,000.01, 30 → true | Domain boundary. | Keep |
| 6 | `age59At300000_returnsTrue` | ₹3L, 59 → true | Domain: 59 is below the senior age. | Keep |
| 7 | `age60AtSeniorLimit_returnsFalse` | ₹3L, 60 → false | Domain: senior limit applies from 60. | Keep |
| 8 | `age60AboveSeniorLimit_returnsTrue` | ₹3,00,000.01, 60 → true | Domain boundary. | Keep |
| 9 | `age79At500000_returnsTrue` | ₹5L, 79 → true | Domain: 79 is below the super-senior age. | Keep |
| 10 | `age80AtSuperSeniorLimit_returnsFalse` | ₹5L, 80 → false | Domain: super-senior limit applies from 80. | Keep |
| 11 | `age80AboveSuperSeniorLimit_returnsTrue` | ₹5,00,000.01, 80 → true | Domain boundary. | Keep |
| 12 | `salaried35With12Lakh_returnsTrue` | ₹12L, 35 → true | Domain: well above every limit. | Keep |

**Deleted: 0. Rewritten: 0. Flagged: 2 (#1, #2).**

Why #1 and #2 are flagged: their expected values come from the code, not
from the domain. They pin a design choice that is inconsistent within the
same class. `computeIncomeTax`, `computeVAT`, `applyExemption` and
`roundToPaise` all **throw `IllegalArgumentException`** on `null` or invalid
input, while `isEligibleForReturn` silently returns `false`. The lecture warns
about exactly this ("tests that pin the current buggy behaviour and are
treated as ground truth"). I kept them because they are the only
specification of invalid-input handling this method has, and without them a
change to that behaviour would go unnoticed. If the spec is changed to throw,
these two tests should become `assertThrows` tests rather than be deleted.

### Part D — Mutation testing

`make mutation` after Part B (`evidence/5A-1-afterPartB-pit.csv`). For
`isEligibleForReturn`:

- **Mutation score: 10/11 killed** (baseline 0/11, all NO_COVERAGE).
- **Surviving mutants (1):**
  - Line 70, `ConditionalsBoundaryMutator` ("changed conditional boundary"):
    `ageYears < 0` → `ageYears <= 0`.

Whole class after Part B: 25/42 = 60%.

**Prompt:**

```
The mutant at line 70 (`ageYears < 0` -> `ageYears <= 0`) survived
the test suite. Write ONE new JUnit test that would kill this
mutant without changing any other test. Explain in one
sentence why this test kills the mutant.
```

**AI reply:**

```java
@Test
public void isEligibleForReturn_ageZeroAboveGeneralLimit_returnsTrue() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 0);
    // Assert
    assertTrue(eligible);
}
```

> Age 0 is the only input where `< 0` and `<= 0` disagree: the original code
> treats age 0 as valid and returns `true` for ₹3,00,000 (above the ₹2,50,000
> limit), while the mutant rejects it and returns `false`.

Added the test, then re-ran PIT (`evidence/5A-2-afterPartD-pit.csv`). The
line-70 mutant is now **KILLED**. `isEligibleForReturn`: **11/11**. Whole
class: **26/42 = 62%**. `make test`: 16/16 pass.

### Part E — Reflect

| Metric | Baseline | After Part B | After Part D |
|---|---|---|---|
| `TaxCalculator` line coverage | 48.8% | 68.3% | 68.3% |
| `TaxCalculator` branch coverage | 34.1% | 56.8% | 56.8% |
| `isEligibleForReturn` branch coverage | 0% | 100% | 100% |
| PIT, `isEligibleForReturn` | 0/11 | 10/11 | **11/11** |
| PIT, whole `TaxCalculator` | 15/42 (36%) | 25/42 (60%) | **26/42 (62%)** |

The targeted test did not change coverage at all (68.3% / 56.8% before and
after), but it raised the mutation score.

**More useful or less useful?** Mutation testing was more useful than
coverage: after Part B, coverage already reported 100% of the method's
branches, but PIT still found an untested boundary (`< 0` vs `<= 0`) where a
real off-by-one bug would have passed every test.

---

## Session 5B — Auto-documentation

### Setup — `OrderApi.java` is missing from the starter

The handout lists `OrderApi.java` ("Spring `@RestController` with four
endpoints; no JavaDoc, no README section") and the starter README says it
has "fake Spring annotations already in place so it compiles without Spring
on the classpath". Neither the class nor the annotations are in the archive.
What I checked:

| Where | Result |
|---|---|
| `m5-pub.tgz` downloaded from the course page (byte-identical to my copy) | 8 files (`Makefile`, `README.md`, `.gitignore`, `test/TaxCalculatorTest.java`, `src/{Order,OrderDto,OrderService,TaxCalculator}.java`); no `OrderApi.java`, no annotation stubs |
| Course site: `m5-pub/`, `OrderApi.java`, `m5-pub.tar.gz`/`.zip`, `m5.tgz` and similar | all 404 |
| Other starters `m2-pub.tgz`, `m3-pub.tgz`, `m4-pub.tgz` | no `OrderApi`. m3 has an unrelated plain-Java `OrderController` (item/qty) |
| `lecture5.pdf` | no `OrderApi` code |
| Local disk (Spotlight + `find`) | no other copy |

In the archive, the files in `src/` are dated 19:50 but the `src/` directory
itself is dated 19:58, which suggests a file was removed from `src/` just
before packaging.

**What I did** (commit `6e9bda2`): reconstructed `src/OrderApi.java` from the
description, with four endpoints that map one-to-one onto the four
`OrderService` methods (`listAll`, `findById`, `create`, `cancel`) and use
the existing `OrderDto.from`, plus `src/SpringStubs.java` (no-op annotations
and a minimal `ResponseEntity`). As the setup requires, it had **no JavaDoc**
at the start of Part A. Any details the handout does not fix are my choices:
paths, `OrderDto` as the POST body, 400 on invalid input, and 409 for
re-cancelling. All Session 5B artefacts document this reconstructed class. If
the original is published, Parts A–C should be redone against it.

Endpoints, checked against a scratch program that calls the controller:

```
POST valid: 201 OrderDto[id=85ef5f43-…, customerId=c1, amount=99.50, status=NEW]
POST amount 0: 400    POST amount -1: 400    POST blank cust: 400    POST null body: 400
GET list size: 1      GET found: 200         GET missing: 404
DELETE: 204           DELETE again: 409      DELETE missing: 404
```

### Part A — JavaDoc for one endpoint (`POST /orders`, `OrderApi#create`)

**Prompt:**

```
Generate JavaDoc for the following method. Rules:
- One-line summary in imperative mood.
- Describe the contract, not the implementation.
- @param for each parameter with type and constraints.
- @return with what is returned.
- @throws for each declared or unchecked exception the
  caller should be aware of.
- If any behaviour is not evident from the code, write
  "TODO" and skip.

@PostMapping
public ResponseEntity<OrderDto> create(@RequestBody OrderDto body) {
    if (body == null
            || body.customerId() == null || body.customerId().isBlank()
            || body.amount() == null || body.amount().signum() <= 0) {
        return ResponseEntity.badRequest().build();
    }
    Order o = service.create(body.customerId(), body.amount());
    return ResponseEntity.status(201).body(OrderDto.from(o));
}
```

**AI reply (pass 1)**, pasted into the source as-is (commit `6a76dbb`):

```java
/**
 * Create an order for a customer ({@code POST /orders}).
 *
 * <p>The new order is assigned a server-generated UUID and starts in status
 * {@code NEW}; any {@code id} or {@code status} in the request body is ignored.
 *
 * <p>Invalid input is reported through the response status, not by an exception.
 *
 * <p>TODO: currency, scale and upper bound of {@code amount} are not
 * constrained by the code.
 *
 * @param body the order to create, as an {@link OrderDto}; may be {@code null}
 *             (rejected). {@code customerId} must be non-null and not blank;
 *             {@code amount} must be non-null and strictly greater than zero
 * @return {@code 201 Created} with the created order as an {@link OrderDto};
 *         {@code 400 Bad Request} with an empty body if {@code body} is
 *         {@code null} or violates the constraints above
 */
```

There is no `@throws`: the method declares no exceptions and throws none
(every validation failure returns 400), so there is no exception the caller
needs to know about.

**Verification prompt (pass 1):**

```
Given the JavaDoc above and the method body below,
identify any statement in the JavaDoc that is inconsistent
with the code. Do not fix; just list.
```

**Reply (pass 1): 1 inconsistency** (full reply: `evidence/5B-A-verify-pass1.md`).

1. "The new order is assigned a server-generated UUID and starts in status
   `NEW`": the method body neither generates an id nor sets a status, so
   nothing in the method supports "UUID" or "`NEW`". The statement happens to
   be true (see `OrderService#create` and the `Order` constructor), but it
   describes other classes. Under the prompt's own TODO rule it should not be
   there.

**Fix** (commit `d052245`): replaced it with *"Only `customerId` and `amount`
are read from the request body; any `id` or `status` it carries is ignored.
The id and status of the returned order are those assigned by
`OrderService#create(String, BigDecimal)`."*

**Reply (pass 2): no inconsistencies** (`evidence/5B-A-verify-pass2.md`).

Extra checks by hand: `javadoc -Xdoclint:all` reports nothing on `create`,
and the scratch-program output above matches every `@param`/`@return` claim.

### Part B — Draft the README

**Prompt.** The file tree (below), the top-level files (`Makefile`, the
starter `README.md`, `.gitignore`) and every file under `src/` were pasted
with:

```
Draft a README.md for this repository with sections:
description, build, quick example, contributing, license.
Use MIT license placeholder.
Rules:
- Do NOT invent features not present in the code.
- If a section has no evidence in the code, write "TODO" and
  skip.
- The one-line description must be a factual summary of what
  the code does, not marketing copy.
```

```
.gitignore  Makefile  README.md
src/Order.java  src/OrderApi.java  src/OrderDto.java  src/OrderService.java
src/SpringStubs.java  src/TaxCalculator.java
test/TaxCalculatorTest.java
```

The reply is saved unedited as **`README.raw.md`** (commit `e1aee1e`).

**Editing.** I grepped the code for every capability the draft claims. The
commands and output are in `evidence/5B-B-readme-claims.md`.

**Invented features deleted: 4**

| # | Claim in `README.raw.md` | grep evidence | Action |
|---|---|---|---|
| 1 | "backed by a **thread-safe** in-memory order store" | No `synchronized`/`Atomic`/`volatile`/`Lock`. `ConcurrentHashMap` only makes single map operations atomic, and `OrderService.cancel` does get → check → `setStatus` on a mutable `Order` without locking. | Deleted. Now "in-memory order store (a `ConcurrentHashMap`)". |
| 2 | "**JSON request/response serialization** of `OrderDto`" | No `json`/`jackson`/`ObjectMapper`; the annotations are no-op stubs. | Deleted. |
| 3 | "**Start the service** and create an order: `curl -X POST http://localhost:8080/orders …`" | No `main`, `SpringApplication`, `8080` or `HttpServer`. Nothing can be started. | Deleted. The quick example now calls `OrderApi` from Java. |
| 4 | "See **`LICENSE`** for details" | `ls LICENSE*` finds nothing | Replaced with "MIT License — TODO: add a `LICENSE` file". |

Also reworded (overstated rather than invented): "a REST API for managing
orders" became "a Spring-style order controller that compiles against fake
annotations and is not served over HTTP".

Other hand edits, all in `README.md` (commit `703dd78`):

- **One-line description:** written in my own words.
- **Contributing:** the draft had TODO. I replaced it with real steps: the
  make targets to run, plus recording prompts in `PROMPTS.md`.
- **Build:** fixed the coverage report path to `coverage/index.html`, removed
  the redundant `make deps` step, and stated the JDK 17 requirement
  (`--release 17` in the Makefile).
- **Quick example:** checked every commented result against real output
  (`52500.00`, `180.00`, `false`, `201`).

### Part C — OpenAPI spec

**Prompt** (with the full `OrderApi.java` at commit `d052245`):

```
Read the following Spring @RestController and generate an
OpenAPI 3.0 YAML spec covering:
- Every endpoint (path, method, summary from JavaDoc).
- Request body schemas for POST/PUT.
- Response schemas for 2xx and 4xx.
- Referenced DTO schemas in the components section.
If any endpoint's behaviour is unclear, add a TODO comment
in the spec at that location.
```

The output was saved as **`openapi.yaml`** and committed unedited
(`d93a158`). TODO comments in the output:

- `amount`: currency, scale and upper bound are not constrained by the code.
- Three summaries (`list`, `get`, `cancel`): those methods have no JavaDoc
  (the handout asks for only one endpoint), so their summaries are derived
  from the method body.

**Render check:** Swagger UI 5.33.0, run locally in headless Chrome. Details
and screenshots: `evidence/5B-C-swagger-check.md`,
`evidence/5B-C-swagger-ui-raw.png`, `evidence/5B-C-swagger-ui-final.png`.

| Check | Result on the generated spec |
|---|---|
| Every endpoint appears with the correct path and method | ✓ `GET /orders`, `POST /orders`, `GET /orders/{id}`, `DELETE /orders/{id}` |
| Every referenced DTO is defined in components | ✓ `OrderDto` |
| Every method has ≥1 2xx and ≥1 4xx | ✗ **`GET /orders` had only `200`** |

**Missing 4xx added by hand: 1** (commit `a47a72b`). I added
`406 Not Acceptable` to `GET /orders`, with a comment in the spec saying it
comes from Spring's content negotiation, not from the method, which has no
error path. After the edit, all three checks pass in the rendered UI, and
`openapi-spec-validator` reports a valid OpenAPI 3.0.3 document.

### Part D — Reflect

- **How many JavaDoc inconsistencies did the AI's self-check catch on the
  first pass?** **1**: the claim about a UUID and status `NEW`. It was true
  of the system but not supported by the method body. The second pass found
  0.
- **How many invented features did the README pass produce?** **4**: thread
  safety, JSON serialization, a runnable server with a curl example, and a
  `LICENSE` file. There was also one overstated description ("REST API").
- **Which artefact needed the most hand editing, and why?** **The README**:
  4 deletions, a rewritten description, a filled-in Contributing section,
  and fixes to Build and Quick example. The JavaDoc needed one sentence, and
  the OpenAPI spec one response code. JavaDoc and OpenAPI are derived from
  code the model sees in full, so their claims can be checked line by line.
  A README has to cover things the code does not contain (how to run it,
  licensing, examples), and the model filled those gaps with what a typical
  Spring service has (JSON, a server on :8080, a LICENSE file) rather than
  what this repository has. Every claim needed a grep.

---

## Deliverables checklist

| Deliverable | Where |
|---|---|
| 5A: `PROMPTS.md` (baseline, prompt + reply, assertion review, mutation, reflection) | this file |
| 5A: generated tests + targeted test | `test/TaxCalculatorTest.java` (16 tests, all pass) |
| 5B: JavaDoc on the chosen endpoint | `src/OrderApi.java`, `create` |
| 5B: `README.raw.md` and `README.md` | repo root |
| 5B: `openapi.yaml` | repo root |
| 5B: updated `PROMPTS.md` covering both sessions | this file |
| Supporting evidence | `evidence/` |
| PR | branch `m5-handson` (one commit per step) on top of `main` (unchanged starter) |
